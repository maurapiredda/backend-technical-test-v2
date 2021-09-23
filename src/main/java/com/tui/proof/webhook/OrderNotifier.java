package com.tui.proof.webhook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tui.proof.model.service.OrderService;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author maura.piredda
 */
@Log4j2
@Component
@ConditionalOnProperty(name = "order.notifier.enabled", havingValue = "true", matchIfMissing = true)
public class OrderNotifier
{
    @Autowired
    private OrderService orderService;

    @Scheduled(fixedRate = 60000)
    public void sendOrder()
    {
        log.trace("Starting sending orders...");
        orderService.notifyOrders();
        log.trace("Orders sent");
    }

}
