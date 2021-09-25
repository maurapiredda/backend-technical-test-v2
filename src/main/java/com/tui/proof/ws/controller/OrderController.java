package com.tui.proof.ws.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tui.proof.PilotesApplication;
import com.tui.proof.error.PilotesErrorResponse;
import com.tui.proof.model.Customer;
import com.tui.proof.model.Order;
import com.tui.proof.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping(path = PilotesApplication.VERSION_1_0 + "/orders")
@Tag(name = "Order")
public class OrderController
{

    @Autowired
    private OrderService orderService;

    public OrderController()
    {
        log.info("Initializing {}", getClass().getSimpleName());
    }

    @Operation(tags = "Order", summary = "Returns an order.")
    @ApiResponse(responseCode = "200", description = "Order found.")

    @GetMapping("/{number}")
    public Order get(
            @Parameter(description = "The order to save.")
            @PathVariable
            String number)
    {
        return orderService.get(number);
    }

    @Operation(tags = "Order", summary = "Saves a new order and returns the order numeber.")
    @ApiResponse(responseCode = "200", description = "Order saved with success.")

    @PostMapping
    public String save(
            @Parameter(description = "The order to save.")
            @Valid
            @RequestBody
            Order order)
    {
        return orderService.save(order);
    }

    @Operation(tags = "Order", summary = "Updates a new order and returns the order numeber.")
    @ApiResponse(responseCode = "200", description = "Order updated with success.")

    @PutMapping
    public String update(
            @Parameter(description = "The order to update.")
            @Valid
            @RequestBody
            Order order)
    {
        return orderService.update(order);
    }

    @Operation(tags = "Order",
            summary = "Returns the list of orders that belongs to the customer that match the given filter. "
                    + "The properties of the passed customer are not required to strictly match with the existing customers, evan a partial match will return the orders."
                    + "For example, the filter {\"email\": \".com\"} will return all orders which the customer's email contains the string '.com'")
    @ApiResponse(responseCode = "200", description = "The list of the orders of the given customer.")

    @ApiResponse(responseCode = "401",
            description = "Unauthorized access",
            content = @Content(schema = @Schema(implementation = PilotesErrorResponse.class)))

    @PostMapping("/searchByCustomer")
    public List<Order> searchByCustomer(
            @RequestBody
            Customer customer)
    {
        return orderService.searchByCustomer(customer);
    }

}
