package com.tui.proof.model.service;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

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
            // TODO error
        }

        if (!UrlValidator.getInstance().isValid(notifierWebhookUrl))
        {
            // TODO error
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
            // TODO error
        }

        order.setCreationDate(ZonedDateTime.now(UTC));
        Order savedOrder = internalSave(order);
        return savedOrder.getOrderNumber();
    }

    public String update(Order order)
    {
        String orderNumber = order.getOrderNumber();
        if (orderNumber == null)
        {
            // TODO error
        }

        Order existingOrder = get(orderNumber);
        if (existingOrder == null)
        {
            // TODO error 404
        }

        existingOrder.setPilotesNumber(order.getPilotesNumber());
        existingOrder.setDeliveryAddress(order.getDeliveryAddress());
        checkSameCustomerOnUpdate(order, existingOrder);
        checkCreationDate(existingOrder);
        internalSave(existingOrder);
        return orderNumber;
    }

    private Order internalSave(Order order)
    {
        setSavedCustomer(order);
        setSavedAddress(order);

        PilotesNumber pilotesNumber = order.getPilotesNumber();
        Double totalAmount = pilotesNumber.getNumber() * pilotesUnitaryPrice;
        order.setTotal(totalAmount);

        return orderDao.save(order);
    }

    private void setSavedAddress(Order order)
    {
        Address deliveryAddress = order.getDeliveryAddress();

        if (deliveryAddress == null)
        {
            // TODO error
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
            // TODO error
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

    private void setSavedCustomer(Order order)
    {
        Customer customer = order.getCustomer();
        if (customer == null)
        {
            // TODO error
        }

        String email = customer.getEmail();
        if (email == null)
        {
            // TODO error
        }
        Customer existingCustomer = customerService.getByEmail(email);
        if (existingCustomer == null)
        {
            // TODO error
        }
        order.setCustomer(existingCustomer);
    }

    private void checkSameCustomerOnUpdate(Order incomingOrder, Order existingOrder)
    {
        // TODO errors
        String incomingEmail = Optional.ofNullable(incomingOrder.getCustomer()).map(Customer::getEmail).orElse(null);
        String existingEmail = Optional.ofNullable(existingOrder.getCustomer()).map(Customer::getEmail).orElse(null);
        if (!existingEmail.equals(incomingEmail))
        {
            // TODO errors
        }
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
}
