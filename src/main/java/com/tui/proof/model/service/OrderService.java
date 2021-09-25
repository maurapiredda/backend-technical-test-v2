package com.tui.proof.model.service;

import java.math.BigInteger;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.GenericPropertyMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tui.proof.dao.OrderDao;
import com.tui.proof.error.PilotesErrorCode;
import com.tui.proof.error.PilotesException;
import com.tui.proof.model.Address;
import com.tui.proof.model.Customer;
import com.tui.proof.model.Order;
import com.tui.proof.model.PilotesNumber;

import lombok.extern.log4j.Log4j2;

/**
 * A {@link Service} that holds the business logic for the orders
 * @author maura.piredda
 */
@Log4j2
@Service
@Transactional
public class OrderService
{
    private static final ZoneId UTC = ZoneId.of("UTC");

    /** The pilotes unitary price */
    @Value("${order.pilotes.unitaryPrice}")
    private double pilotesUnitaryPrice = 1;

    /** The minutes from the creation of an order within it can be updated */
    @Value("${order.pilotes.expirationMinutes}")
    private int expirationMinutes = 5;

    /** Enables/disables the orders notification */
    @Value("${order.notifier.enabled}")
    private boolean notifierEnabled;

    /** The service that receives the orders */
    @Value("${order.notifier.webhook.url}")
    private String notifierWebhookUrl;

    /** The API-KEY of the service that receives the orders */
    @Value("${order.notifier.webhook.apyKey}")
    private String notifierWebhookApiKey;

    /** The timeout for the notification request in seconds */
    @Value("${order.notifier.webhook.timeout}")
    private long notifierWebhookTimeout;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerService customerService;

    private RestTemplate notifierRestTemplate;

    /**
     * Init the RestTemplate that will be used to notify the orders.
     * @throws IllegalArgumentException if {@link #notifierWebhookApiKey} is empty or if {@link #notifierWebhookUrl} is
     *                                  not a valid URL.
     */
    @PostConstruct
    public void init()
    {
        if (!notifierEnabled)
        {
            log.debug("The orders notifier is disabled");
        }

        if (StringUtils.isBlank(notifierWebhookApiKey))
        {
            throw new IllegalArgumentException(
                    "The parameter order.notifier.webhook.apyKey cannot be blank. Please check the application.properties file.");
        }

        if (!UrlValidator.getInstance().isValid(notifierWebhookUrl))
        {
            throw new IllegalArgumentException(
                    "The parameter order.notifier.webhook.url must be a valid URL. Please check the application.properties file.");
        }

        RestTemplateBuilder restBuilder = new RestTemplateBuilder();

        Duration timeout = Duration.ofSeconds(notifierWebhookTimeout);
        notifierRestTemplate = restBuilder.setConnectTimeout(timeout).setReadTimeout(timeout).build();
    }

    /**
     * @param number the order's number
     * @return the order identified by the {@code number} or null if it doesn't exist
     * @throws PilotesException with {@link PilotesErrorCode#ORDER_NUMBER_EMPTY} if the {@code number} is blank
     */
    public Order get(String number)
    {
        if (StringUtils.isBlank(number))
        {
            throw ex(PilotesErrorCode.ORDER_NUMBER_EMPTY);
        }
        Order filter = new Order();
        filter.setOrderNumber(number);
        return orderDao.findOne(Example.of(filter)).orElse(null);
    }

    /**
     * Saves a new order. <br>
     * Even if passed, the fields {@code orderNumber}, {@code creationDate} and {@code notified} are overwritten with
     * the default values. <br>
     * The incoming {@code order} must have a completely filled {@link Address}, so that it can be retrieved if it
     * exists or created if it doesn't exist, and a {@link Customer} with the email field filled. <br>
     * The email must identify an existing Customer, otherwise the order will not be created.
     * @param order the order to save
     * @return the generated order number.
     * @throws PilotesException with {@link PilotesErrorCode#ORDER_NULL} if the given order is null.
     * @see #setDefaultFields(Order)
     * @see #internalSave(Order)
     */
    public String save(Order order)
    {
        if (order == null)
        {
            throw ex(PilotesErrorCode.ORDER_NULL);
        }
        setDefaultFields(order);
        Order savedOrder = internalSave(order);
        String orderNumber = savedOrder.getOrderNumber();
        log.info("Saved a new order with id {} and number {}", savedOrder.getId(), orderNumber);
        return orderNumber;
    }

    /**
     * Updates an existing order <br>
     * 
     * @param order the order to update
     * @return the order number
     * 
     * @throws PilotesException with {@link PilotesErrorCode#ORDER_NUMBER_EMPTY} it the order number is blank
     * @throws PilotesException with {@link PilotesErrorCode#ORDER_NOT_FOUND} if the order identified by the number
     *                          doesn't
     *                          exist
     * @throws PilotesException with {@link PilotesErrorCode#ORDER_CUSTOMER_CANNOT_BE_CHANGED} if the incoming order
     *                          refers to a customer different from the customer of the existing order
     * @throws PilotesException with {@link PilotesErrorCode#ORDER_EXPIRED} if the incoming order is too old and cannot
     *                          be updated anymore
     * @see #internalSave(Order)
     */
    public String update(Order order)
    {
        String orderNumber = order.getOrderNumber();
        Order existingOrder = get(orderNumber);
        if (existingOrder == null)
        {
            throw ex(PilotesErrorCode.ORDER_NOT_FOUND);
        }

        checkSameCustomerOnUpdate(order, existingOrder);
        checkCreationDate(existingOrder);

        existingOrder.setPilotesNumber(order.getPilotesNumber());
        existingOrder.setDeliveryAddress(order.getDeliveryAddress());
        internalSave(existingOrder);
        log.info("Updated order with id {} and number {}", existingOrder.getId(), orderNumber);
        return orderNumber;
    }

    /**
     * Set the default values to a new order:
     * <li>the {@code creationDate} is set to {@link ZonedDateTime#now(ZoneId)} where the ZondId is UTC</li>
     * <li>the {@code notified} flag is set to false</li>
     * <li>the {@code orderNumber} is generated padding left with 0 the value returned by
     * {@link OrderDao#getNextOrderSequenceValue()}</li>
     * @param newOrder the order to save
     */
    private void setDefaultFields(Order newOrder)
    {
        newOrder.setCreationDate(ZonedDateTime.now(UTC));
        newOrder.setNotified(false);
        BigInteger sequenceValue = orderDao.getNextOrderSequenceValue();
        String orderNumber = String.format("%010d", sequenceValue.longValue());
        newOrder.setOrderNumber(orderNumber);
    }

    /**
     * Saves or updates the order
     * @param order the order to save or update
     * @return the saved or updated order
     * @throws PilotesException with {@link PilotesErrorCode#ADDRESS_NULL} if the order's address is null.
     * @throws PilotesException with {@link PilotesErrorCode#CUSTOMER_NULL} if the order's customer is null.
     * @throws PilotesException with {@link PilotesErrorCode#CUSTOMER_EMAIL_EMPTY} if the customer's email is empty.
     * @throws PilotesException with {@link PilotesErrorCode#CUSTOMER_NOT_FOUND} if the customer identified by the given
     *                          email doesn't exist.
     */
    private Order internalSave(Order order)
    {
        setCustomer(order);
        setAddress(order);

        PilotesNumber pilotesNumber = order.getPilotesNumber();
        Double totalAmount = pilotesNumber.getNumber() * pilotesUnitaryPrice;
        order.setTotal(totalAmount);

        return orderDao.save(order);
    }

    private void setAddress(Order order)
    {
        Address deliveryAddress = order.getDeliveryAddress();
        if (deliveryAddress == null)
        {
            throw ex(PilotesErrorCode.ADDRESS_NULL);
        }

        Address existingAddress = addressService.find(deliveryAddress);
        if (existingAddress == null)
        {
            existingAddress = addressService.save(deliveryAddress);
            log.debug("Saved new address: {}", existingAddress);
        }
        order.setDeliveryAddress(existingAddress);
    }

    private void checkCreationDate(Order order)
    {
        ZonedDateTime creationDate = order.getCreationDate();

        ZonedDateTime creationDateUtc = creationDate.withZoneSameInstant(UTC);
        ZonedDateTime expirationTimeUtc = creationDateUtc.plusMinutes(expirationMinutes);
        ZonedDateTime nowUtc = ZonedDateTime.now(UTC);

        log.debug("CreationDate {}", creationDate);
        log.debug("CreationDate UTC {}", creationDateUtc);
        log.debug("Expiration time UTC {}", expirationTimeUtc);
        log.debug("Now UTC {}", nowUtc);

        if (nowUtc.isAfter(expirationTimeUtc))
        {
            throw ex(PilotesErrorCode.ORDER_EXPIRED);
        }
        log.debug("The order is recent enough and can be updated");
    }

    /**
     * Returns the list of orders that belongs to the customer that match the given {@code filter}. <br>
     * The properties of the passed {@code filter} are not required to strictly match with the existing customers, even
     * a
     * partial match will return the orders.<br>
     * For example, a {@code filter} with email set to {@code ".com"} will return all orders which the customer's email
     * contains the string {@code ".com"}
     * @param filter the customer filter
     * @return the orders that belong to the customer that match the given {@code filter}
     */
    public List<Order> searchByCustomer(Customer filter)
    {
        log.debug("Searching orders for customer {}", filter);
        GenericPropertyMatcher containingPropertyMatcher = GenericPropertyMatcher.of(StringMatcher.CONTAINING);
        ExampleMatcher orderMatcher = ExampleMatcher.matching()
                .withMatcher("customer.firstName", containingPropertyMatcher)
                .withMatcher("customer.lastName", containingPropertyMatcher)
                .withMatcher("customer.telephone", containingPropertyMatcher)
                .withMatcher("customer.email", containingPropertyMatcher);

        Order orderFilter = new Order();
        orderFilter.setCustomer(filter);
        List<Order> orders = orderDao.findAll(Example.of(orderFilter, orderMatcher));
        log.debug("Found {} orders", orders.size());
        return orders;
    }

    /**
     * Send to orders ready to be notified to the notifier service <br>
     * An order is considered ready to be notified if it has not been notified yet (i.e. its notified flag is set to
     * false) and it its older than {@code now - #expirationMinutes}. <br>
     * If the notification succeed, the notified flags of the sent orders is set to true.
     * @see #notifierWebhookUrl
     */
    public void notifyOrders()
    {
        if (!notifierEnabled)
        {
            log.debug("The orders notifier is disabled");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(UTC);
        ZonedDateTime nowMinusExpirationMinutes = now.minusMinutes(expirationMinutes);

        List<Order> ordersToNotify = orderDao.findByCreationDateBeforeAndNotifiedFalse(nowMinusExpirationMinutes);

        if (CollectionUtils.isEmpty(ordersToNotify))
        {
            log.info("No orders to notify found");
            return;
        }

        log.info("Found {} orders to notify", ordersToNotify.size());

        boolean ordersSent = sendOrdersToWebhook(ordersToNotify);

        if (ordersSent)
        {
            if (log.isDebugEnabled())
            {
                String orderNumbers = ordersToNotify.stream()
                        .map(Order::getOrderNumber)
                        .reduce(StringUtils.EMPTY, (prev, next) -> prev + ", " + next);
                log.debug("Setting notified flag to true for {}", orderNumbers);
            }
            ordersToNotify.forEach(order -> order.setNotified(true));
            orderDao.saveAll(ordersToNotify);
        }
    }

    private void setCustomer(Order order)
    {
        String email = getCustomerEmail(order);

        Customer existingCustomer = customerService.getByEmail(email);
        if (existingCustomer == null)
        {
            throw ex(PilotesErrorCode.CUSTOMER_NOT_FOUND);
        }
        order.setCustomer(existingCustomer);
    }

    private void checkSameCustomerOnUpdate(Order incomingOrder, Order existingOrder)
    {
        String incomingEmail = getCustomerEmail(incomingOrder);
        String existingEmail = getCustomerEmail(existingOrder);
        if (!existingEmail.equals(incomingEmail))
        {
            throw ex(PilotesErrorCode.ORDER_CUSTOMER_CANNOT_BE_CHANGED);
        }
    }

    private String getCustomerEmail(Order order)
    {
        Customer customer = order.getCustomer();
        if (customer == null)
        {
            throw ex(PilotesErrorCode.CUSTOMER_NULL);
        }

        String email = customer.getEmail();
        if (StringUtils.isEmpty(email))
        {
            throw ex(PilotesErrorCode.CUSTOMER_EMAIL_EMPTY);
        }
        return email;
    }

    /**
     * Sends the {@code ordersToNotify} to the notifier service
     * @param ordersToNotify the orders ready to be notified
     * @return true if the notifier service responds with a {@link HttpStatus#OK}
     * @see #notifierWebhookUrl
     * @see #notifierWebhookApiKey
     * @see #notifierWebhookTimeout
     */
    private boolean sendOrdersToWebhook(List<Order> ordersToNotify)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, notifierWebhookApiKey);

        HttpEntity<List<Order>> entity = new HttpEntity<List<Order>>(ordersToNotify, headers);
        ResponseEntity<String> response = notifierRestTemplate.postForEntity(notifierWebhookUrl, entity, String.class);

        HttpStatus statusCode = response.getStatusCode();
        boolean isResponseOk = statusCode.equals(HttpStatus.OK);
        if (isResponseOk)
        {
            log.info("Sent {} orders with success", ordersToNotify.size());
        }
        else
        {
            String message = response.getBody();
            log.info("Orders has not been sent: http status {} - message {}", statusCode, message);
        }
        return isResponseOk;
    }

    private PilotesException ex(PilotesErrorCode errorCode)
    {
        return new PilotesException(errorCode);
    }
}
