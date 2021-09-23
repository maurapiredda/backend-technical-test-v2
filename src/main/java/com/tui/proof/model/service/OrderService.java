package com.tui.proof.model.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.tui.proof.dao.OrderDao;
import com.tui.proof.model.Address;
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

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AddressService addressService;

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

        checkCreationDate(existingOrder);
        internalSave(existingOrder);
        return orderNumber;
    }

    private Order internalSave(Order order)
    {
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

}
