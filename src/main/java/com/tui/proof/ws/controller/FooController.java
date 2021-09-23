package com.tui.proof.ws.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.tui.proof.model.Order;
import com.tui.proof.model.service.OrderService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class FooController
{

    @Autowired
    private OrderService orderService;

    @GetMapping("/")
    void test()
    {
        log.info("Foo controller");
    }

    @GetMapping("/notify")
    void testNotify()
    {
        orderService.notifyOrders();
    }

    @PostMapping("/notify")
    void testNotified(
            @RequestBody
            List<Order> orders)
    {
        log.info("Notified {} orders", orders.size());
    }
}
