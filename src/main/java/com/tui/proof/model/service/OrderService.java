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

    @Value("${order.pilotes.unitaryPrice}")
    private double pilotesUnitaryPrice = 1;

    @Value("${order.pilotes.expirationMinutes}")
    private int expirationMinutes = 5;

    @Value("${order.notifier.enabled}")
    private boolean notifierEnabled;

    @Value("${order.notifier.webhook.url}")
    private String notifierWebhookUrl;

    @Value("${order.notifier.webhook.apyKey}")
    private String notifierWebhookApiKey;

    @Value("${order.notifier.webhook.timeout}")
    private long notifierWebhookTimeout;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerService customerService;

    private RestTemplate notifierRestTemplate;

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

    public Order get(String number)
    {
        Order filter = new Order();
        filter.setOrderNumber(number);
        return orderDao.findOne(Example.of(filter)).orElse(null);
    }

    public String save(Order order)
    {
        if (order == null)
        {
            throw ex(PilotesErrorCode.ORDER_NULL);
        }
        
        setDefaultFields(order);
        Order savedOrder = internalSave(order);
        return savedOrder.getOrderNumber();
    }

    public String update(Order order)
    {
        String orderNumber = order.getOrderNumber();
        if (orderNumber == null)
        {
            throw ex(PilotesErrorCode.ORDER_NUMBER_NULL);
        }

        Order existingOrder = get(orderNumber);
        if (existingOrder == null)
        {
            throw ex(PilotesErrorCode.ORDER_NULL);
        }

        checkSameCustomerOnUpdate(order, existingOrder);
        checkCreationDate(existingOrder);
        
        existingOrder.setPilotesNumber(order.getPilotesNumber());
        existingOrder.setDeliveryAddress(order.getDeliveryAddress());
        internalSave(existingOrder);
        return orderNumber;
    }
    
    private void setDefaultFields(Order newOrder)
    {
        newOrder.setCreationDate(ZonedDateTime.now(UTC));
        newOrder.setNotified(false);
        BigInteger sequenceValue = orderDao.getNextOrderSequenceValue();
        String orderNumber = String.format("%010d", sequenceValue.longValue());
        newOrder.setOrderNumber(orderNumber);
    }

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
        log.debug("CreationDate utc {}", creationDateUtc);
        log.debug("Expiration time utc {}", expirationTimeUtc);
        log.debug("Now utc {}", nowUtc);

        if (nowUtc.isAfter(expirationTimeUtc))
        {
            throw ex(PilotesErrorCode.ORDER_EXPIRED);
        }
    }

    public List<Order> searchByCustomer(Customer customer)
    {
        GenericPropertyMatcher containingPropertyMatcher = GenericPropertyMatcher.of(StringMatcher.CONTAINING);
        ExampleMatcher orderMatcher = ExampleMatcher.matching()
                .withMatcher("customer.firstName", containingPropertyMatcher)
                .withMatcher("customer.lastName", containingPropertyMatcher)
                .withMatcher("customer.telephone", containingPropertyMatcher)
                .withMatcher("customer.email", containingPropertyMatcher);

        Order filter = new Order();
        filter.setCustomer(customer);
        return orderDao.findAll(Example.of(filter, orderMatcher));
    }

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
        if (email == null)
        {
            throw ex(PilotesErrorCode.CUSTOMER_EMAIL_NULL);
        }
        return email;
    }

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
