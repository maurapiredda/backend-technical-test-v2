package com.tui.proof.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * The errors that can occur calling the API. <br>
 * Each concrete implementation specifies the {@link HttpStatus} and a message.
 * @author maura.piredda
 */
public abstract class PilotesErrorCode
{
    // --- GENERIC ERRORS --------------------------------------------------------------------------------------------- 
    
    public static final PilotesErrorCode UNEXPECTED_ERROR = new PilotesErrorCode(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurs")
    {
    };

    public static final PilotesErrorCode UNAUTHORIZED = new PilotesErrorCode(HttpStatus.UNAUTHORIZED,
            "Unauthorized access")
    {
    };

    public static final PilotesErrorCode INVALID_INPUT = new PilotesErrorCode(HttpStatus.BAD_REQUEST,
            "One or more of the received parameter is not correct. Please check the input parameters and try again")
    {
    };

    // --- BUSINESS LOGIC ERRORS --------------------------------------------------------------------------------------

    public static final PilotesErrorCode ADDRESS_NULL = new PilotesErrorCode(HttpStatus.BAD_REQUEST,
            "The address cannot be null")
    {
    };

    public static final PilotesErrorCode CUSTOMER_NULL = new PilotesErrorCode(HttpStatus.BAD_REQUEST,
            "The customer cannot be null")
    {
    };

    public static final PilotesErrorCode CUSTOMER_EMAIL_EMPTY = new PilotesErrorCode(HttpStatus.BAD_REQUEST,
            "The customer email cannot be empty")
    {
    };

    public static final PilotesErrorCode CUSTOMER_NOT_FOUND = new PilotesErrorCode(HttpStatus.NOT_FOUND,
            "The requested customer does not exist")
    {
    };

    public static final PilotesErrorCode ORDER_NULL = new PilotesErrorCode(HttpStatus.BAD_REQUEST,
            "The order cannot be null")
    {
    };
    
    public static final PilotesErrorCode ORDER_NOT_FOUND = new PilotesErrorCode(HttpStatus.NOT_FOUND,
            "The requested order does not exist")
    {
    };

    public static final PilotesErrorCode ORDER_NUMBER_EMPTY = new PilotesErrorCode(HttpStatus.BAD_REQUEST,
            "The order number cannot be empty")
    {
    };

    public static final PilotesErrorCode ORDER_EXPIRED = new PilotesErrorCode(HttpStatus.UNPROCESSABLE_ENTITY,
            "The order has already been processed and cannot be updated anymore")
    {
    };

    public static final PilotesErrorCode ORDER_CUSTOMER_CANNOT_BE_CHANGED = new PilotesErrorCode(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "The order customer cannot be changed")
    {
    };

    private @Getter
    HttpStatus httpStatus;

    private @Getter
    String message;

    private PilotesErrorCode(HttpStatus httpStatus, String message)
    {
        this.httpStatus = httpStatus;
        this.message = message;
    }

}
