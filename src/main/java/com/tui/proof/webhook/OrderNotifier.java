package com.tui.proof.webhook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tui.proof.service.OrderService;

import lombok.extern.log4j.Log4j2;

/**
 * The order notifier scheduler. <br>
 * The {@link #sendOrder()} method checks every minute if there are new orders to notify. <br>
 * This component can be enabled/disabled with the application property {@code order.notifier.enabled}. The default
 * value is true.
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
