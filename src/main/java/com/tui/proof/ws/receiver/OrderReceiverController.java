package com.tui.proof.ws.receiver;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tui.proof.model.Order;

import lombok.extern.log4j.Log4j2;

/**
 * This controller simulates the service that must process the orders
 * @author maura.piredda
 */
@Log4j2
@RestController
public class OrderReceiverController
{

    public OrderReceiverController()
    {
        log.info("Initializing {}", getClass().getSimpleName());
    }

    @PostMapping("/prepareOrders")
    void prepareOrders(
            @RequestBody
            List<Order> orders)
    {
        log.info("Received {} orders", orders.size());
    }
}
