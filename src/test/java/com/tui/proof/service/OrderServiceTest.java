package com.tui.proof.service;

import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.PropertySpecifiers;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.tui.proof.dao.OrderDao;
import com.tui.proof.error.PilotesErrorCode;
import com.tui.proof.error.PilotesException;
import com.tui.proof.model.Address;
import com.tui.proof.model.Customer;
import com.tui.proof.model.Order;
import com.tui.proof.model.PilotesNumber;

@SuppressWarnings("unchecked")
@ExtendWith(EasyMockExtension.class)
public class OrderServiceTest extends EasyMockSupport
{
    private static final ZoneId UTC = ZoneId.of("UTC");

    @Mock
    private OrderDao orderDao;

    @Mock
    private AddressService addressService;

    @Mock
    private CustomerService customerService;

    @Mock
    private RestTemplate restTemplate;

    @TestSubject
    private OrderService orderService = OrderService.getInstanceForTesting(true);

    @BeforeEach
    public void resetMocks()
    {
        resetAll();
    }

    @Test
    public void testGet()
    {
        EasyMock.expect(orderDao.findOne((Example<Order>) EasyMock.anyObject())).andReturn(Optional.ofNullable(null));
        replayAll();

        Order order = orderService.get("100").orElse(null);
        Assert.assertNull(order);

        verifyAll();
    }

    @Test
    public void testGetEmptyNumber()
    {
        try
        {
            orderService.get(null);
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(PilotesErrorCode.ORDER_NUMBER_EMPTY, e.getPilotesErrorCode());
        }

        try
        {
            orderService.get("");
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(PilotesErrorCode.ORDER_NUMBER_EMPTY, e.getPilotesErrorCode());
        }
    }

    @Test
    public void testSave()
    {
        String expectedOrderNumber = "100";

        Customer customer = new Customer();
        customer.setEmail("email");
        Address deliveryAddress = new Address();

        Order savedOrder = new Order();
        savedOrder.setOrderNumber(expectedOrderNumber);

        EasyMock.expect(orderDao.getNextOrderSequenceValue()).andReturn(BigInteger.valueOf(150L));
        EasyMock.expect(customerService.getByEmail(EasyMock.anyString())).andReturn(Optional.of(customer));
        EasyMock.expect(addressService.find(EasyMock.anyObject(Address.class))).andReturn(Optional.of(deliveryAddress));
        EasyMock.expect(orderDao.save(EasyMock.anyObject(Order.class))).andReturn(savedOrder);
        replayAll();

        Order order = new Order();
        order.setPilotesNumber(PilotesNumber.FIFTEEN);
        order.setCustomer(customer);
        order.setDeliveryAddress(deliveryAddress);

        String number = orderService.save(order);
        Assert.assertEquals(expectedOrderNumber, number);

        verifyAll();
    }

    @Test
    public void testSaveNull()
    {
        saveWithException(null, PilotesErrorCode.ORDER_NULL);
    }

    @Test
    public void testSaveCustomerNull()
    {
        EasyMock.expect(orderDao.getNextOrderSequenceValue()).andReturn(BigInteger.valueOf(150L));
        saveWithException(new Order(), PilotesErrorCode.CUSTOMER_NULL);
    }

    @Test
    public void testSaveCustomerEmailEmpty()
    {
        Order order = new Order();
        order.setCustomer(new Customer());
        EasyMock.expect(orderDao.getNextOrderSequenceValue()).andReturn(BigInteger.valueOf(150L));
        saveWithException(order, PilotesErrorCode.CUSTOMER_EMAIL_EMPTY);
    }

    @Test
    public void testSaveCustomerNotFound()
    {
        Customer customer = new Customer();
        customer.setEmail("email");
        Order order = new Order();
        order.setCustomer(customer);
        EasyMock.expect(orderDao.getNextOrderSequenceValue()).andReturn(BigInteger.valueOf(150L));
        EasyMock.expect(customerService.getByEmail(EasyMock.anyString())).andReturn(Optional.ofNullable(null));
        saveWithException(order, PilotesErrorCode.CUSTOMER_NOT_FOUND);
    }

    @Test
    public void testSaveAddressNull()
    {
        Customer customer = new Customer();
        customer.setEmail("email");

        EasyMock.expect(orderDao.getNextOrderSequenceValue()).andReturn(BigInteger.valueOf(150L));
        EasyMock.expect(customerService.getByEmail(EasyMock.anyString())).andReturn(Optional.of(new Customer()));

        Order order = new Order();
        order.setCustomer(customer);
        saveWithException(order, PilotesErrorCode.ADDRESS_NULL);
    }

    @Test
    public void testSaveAddressNotFound()
    {
        String expectedOrderNumber = "100";

        Customer customer = new Customer();
        customer.setEmail("email");
        Address deliveryAddress = new Address();

        Order savedOrder = new Order();
        savedOrder.setOrderNumber(expectedOrderNumber);

        EasyMock.expect(orderDao.getNextOrderSequenceValue()).andReturn(BigInteger.valueOf(150L));
        EasyMock.expect(customerService.getByEmail(EasyMock.anyString())).andReturn(Optional.of(new Customer()));
        EasyMock.expect(addressService.find(EasyMock.anyObject(Address.class))).andReturn(Optional.ofNullable(null));
        EasyMock.expect(addressService.save(EasyMock.anyObject(Address.class))).andReturn(deliveryAddress);
        EasyMock.expect(orderDao.save(EasyMock.anyObject(Order.class))).andReturn(savedOrder);
        replayAll();

        Order order = new Order();
        order.setPilotesNumber(PilotesNumber.FIFTEEN);
        order.setCustomer(customer);
        order.setDeliveryAddress(deliveryAddress);

        String number = orderService.save(order);
        Assert.assertEquals(expectedOrderNumber, number);

        verifyAll();
    }

    @Test
    public void testUpdate()
    {
        Customer customer = new Customer();
        customer.setEmail("email");

        String orderNumber = "order number";

        Order orderToUpdate = new Order();
        orderToUpdate.setOrderNumber(orderNumber);
        orderToUpdate.setCustomer(customer);
        orderToUpdate.setDeliveryAddress(new Address());
        orderToUpdate.setPilotesNumber(PilotesNumber.FIFTEEN);

        Order existingOrder = new Order();
        existingOrder.setCustomer(customer);
        existingOrder.setCreationDate(ZonedDateTime.now(UTC));
        existingOrder.setNotified(false);

        Capture<Order> orderToSaveCapture = Capture.newInstance();

        EasyMock.expect(orderDao.findOne((Example<Order>) EasyMock.anyObject()))
                .andReturn(Optional.ofNullable(existingOrder));
        EasyMock.expect(customerService.getByEmail(EasyMock.anyString())).andReturn(Optional.of(customer));
        EasyMock.expect(addressService.find(EasyMock.anyObject(Address.class))).andReturn(Optional.of(new Address()));
        EasyMock.expect(orderDao.save(EasyMock.capture(orderToSaveCapture))).andAnswer(() -> EasyMock
                .getCurrentArgument(0));
        replayAll();

        String updatedOrderNumber = orderService.update(orderToUpdate);
        Assert.assertEquals(orderNumber, updatedOrderNumber);

        Order orderToSave = orderToSaveCapture.getValue();
        Assert.assertEquals(orderToUpdate.getPilotesNumber(), orderToSave.getPilotesNumber());
        Assert.assertEquals(orderToUpdate.getDeliveryAddress(), orderToSave.getDeliveryAddress());

        verifyAll();
    }

    @Test
    public void testUpdateNull()
    {
        updateWithException(null, PilotesErrorCode.ORDER_NULL);
    }

    @Test
    public void testUpdateNotExistingOrder()
    {
        Order order = new Order();
        order.setOrderNumber("order number");

        EasyMock.expect(orderDao.findOne((Example<Order>) EasyMock.anyObject())).andReturn(Optional.ofNullable(null));
        updateWithException(order, PilotesErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    public void testUpdateDifferentCustomer()
    {
        Customer customer1 = new Customer();
        customer1.setEmail("email1");

        Customer customer2 = new Customer();
        customer2.setEmail("email2");

        Order order = new Order();
        order.setOrderNumber("order number");
        order.setCustomer(customer1);

        Order existingOrder = new Order();
        existingOrder.setCustomer(customer2);

        EasyMock.expect(orderDao.findOne((Example<Order>) EasyMock.anyObject())).andReturn(Optional.of(existingOrder));
        updateWithException(order, PilotesErrorCode.ORDER_CUSTOMER_CANNOT_BE_CHANGED);
    }

    @Test
    public void testUpdateExpiredOrder()
    {
        ZonedDateTime creationDate = ZonedDateTime.now(UTC).minusMinutes(10);

        Customer customer = new Customer();
        customer.setEmail("email");

        Order order = new Order();
        order.setOrderNumber("order number");
        order.setCustomer(customer);
        order.setCreationDate(creationDate);

        EasyMock.expect(orderDao.findOne((Example<Order>) EasyMock.anyObject())).andReturn(Optional.of(order));
        updateWithException(order, PilotesErrorCode.ORDER_EXPIRED);
    }

    @Test
    public void testUpdateNotifiedOrder()
    {
        Customer customer = new Customer();
        customer.setEmail("email");

        Order order = new Order();
        order.setOrderNumber("order number");
        order.setCustomer(customer);
        order.setCreationDate(ZonedDateTime.now(UTC));
        order.setNotified(true);

        EasyMock.expect(orderDao.findOne((Example<Order>) EasyMock.anyObject())).andReturn(Optional.of(order));
        updateWithException(order, PilotesErrorCode.ORDER_NOTIFIED_ALREDY);
    }

    @Test
    public void testSearchByCustomer()
    {
        Customer filter = new Customer();
        filter.setEmail(".com");

        Capture<Example<Order>> matcherCapture = Capture.newInstance();

        EasyMock.expect(orderDao.findAll(EasyMock.capture(matcherCapture))).andReturn(new ArrayList<>());
        replayAll();

        List<Order> orders = orderService.searchByCustomer(filter);

        Assert.assertEquals(0, orders.size());

        Example<Order> example = matcherCapture.getValue();
        Order probe = example.getProbe();
        Assert.assertEquals(filter.getEmail(), probe.getCustomer().getEmail());

        ExampleMatcher matcher = example.getMatcher();
        PropertySpecifiers specifiers = matcher.getPropertySpecifiers();
        Assert.assertEquals(4, specifiers.getSpecifiers().size());

        Assert.assertEquals(StringMatcher.CONTAINING, specifiers.getForPath("customer.firstName").getStringMatcher());
        Assert.assertEquals(StringMatcher.CONTAINING, specifiers.getForPath("customer.lastName").getStringMatcher());
        Assert.assertEquals(StringMatcher.CONTAINING, specifiers.getForPath("customer.telephone").getStringMatcher());
        Assert.assertEquals(StringMatcher.CONTAINING, specifiers.getForPath("customer.email").getStringMatcher());

        verifyAll();
    }

    @Test
    public void testSearchByCustomerNull()
    {
        try
        {
            orderService.searchByCustomer(null);
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(PilotesErrorCode.CUSTOMER_NULL, e.getPilotesErrorCode());
        }
    }

    @Test
    public void testNotifyOrders()
    {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order());

        Capture<List<Order>> listCapture = Capture.newInstance();

        EasyMock.expect(orderDao.findByCreationDateBeforeAndNotifiedFalse(EasyMock.anyObject(ZonedDateTime.class)))
                .andReturn(orders);
        EasyMock.expect(orderDao.saveAll(EasyMock.capture(listCapture))).andReturn(new ArrayList<Order>());

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.OK);
        EasyMock.expect(restTemplate.postForEntity(EasyMock.anyString(), EasyMock.anyObject(Class.class), EasyMock
                .anyObject(Class.class))).andReturn(responseEntity);

        replayAll();

        orderService.notifyOrders();

        List<Order> savedOrders = listCapture.getValue();
        Assert.assertTrue(savedOrders.get(0).getNotified());

        verifyAll();
    }

    @Test
    public void testNotifyOrdersNone()
    {
        EasyMock.expect(orderDao.findByCreationDateBeforeAndNotifiedFalse(EasyMock.anyObject(ZonedDateTime.class)))
                .andReturn(new ArrayList<>());

        replayAll();
        orderService.notifyOrders();
        verifyAll();
    }

    @Test
    public void testNotifyOrdersResponseKo()
    {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order());

        EasyMock.expect(orderDao.findByCreationDateBeforeAndNotifiedFalse(EasyMock.anyObject(ZonedDateTime.class)))
                .andReturn(orders);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(StringUtils.EMPTY,
                HttpStatus.INTERNAL_SERVER_ERROR);
        EasyMock.expect(restTemplate.postForEntity(EasyMock.anyString(), EasyMock.anyObject(Class.class), EasyMock
                .anyObject(Class.class))).andReturn(responseEntity);

        replayAll();
        orderService.notifyOrders();
        verifyAll();
    }

    private void saveWithException(Order order, PilotesErrorCode errorCode)
    {
        replayAll();
        try
        {
            orderService.save(order);
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(errorCode, e.getPilotesErrorCode());
        }
        verifyAll();
    }

    private void updateWithException(Order order, PilotesErrorCode errorCode)
    {
        replayAll();
        try
        {
            orderService.update(order);
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(errorCode, e.getPilotesErrorCode());
        }
        verifyAll();
    }

}
