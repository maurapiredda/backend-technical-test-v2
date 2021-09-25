package com.tui.proof.error;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@ControllerAdvice
public class PilotesExceptionHandler extends ResponseEntityExceptionHandler
{
    @ExceptionHandler(PilotesException.class)
    protected ResponseEntity<Object> handlePilotesException(PilotesException ex, WebRequest request)
    {
        return getResponseEntity(ex.getPilotesErrorCode(), ex, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request)
    {
        return getResponseEntity(PilotesErrorCode.INVALID_INPUT, ex, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request)
    {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException)
        {
            InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
            String propertyPath = invalidFormatException
                    .getPath()
                    .stream()
                    .map(Reference::getFieldName)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("."));
            
            String details = invalidFormatException.getOriginalMessage();
            details = StringUtils.replace(details, "\"", "'") + " for property '" + propertyPath + "'";
            // example of details:
            // Cannot deserialize value of type 'java.lang.Double' from String 'wrong': not a valid Double value for property 'order.total'
            return getResponseEntity(details, PilotesErrorCode.INVALID_INPUT, ex, request);
        }
        return getResponseEntity(PilotesErrorCode.INVALID_INPUT, ex, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatus status, WebRequest request)
    {
        BindingResult bindingResult = ex.getBindingResult();
        String errorDetails = "The validation failed for one or more parameters" + getErrorDetails(bindingResult);

        return getResponseEntity(errorDetails, PilotesErrorCode.INVALID_INPUT, ex, request);
    }

    private String getErrorDetails(BindingResult bindingResult)
    {
        String dot = ".";

        List<ObjectError> errors = Optional.ofNullable(bindingResult).map(BindingResult::getAllErrors).orElse(null);
        if (errors == null)
        {
            return dot;
        }
        return errors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(";", ": ", dot));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request)
    {
        return getResponseEntity(PilotesErrorCode.UNEXPECTED_ERROR, ex, request);
    }

    private ResponseEntity<Object> getResponseEntity(String errorMessage, PilotesErrorCode errorCode, Exception ex,
            WebRequest request)
    {
        PilotesErrorResponse response = new PilotesErrorResponse(errorCode);
        response.setMessage(errorMessage);
        return getResponseEntity(response, ex, request);
    }

    private ResponseEntity<Object> getResponseEntity(PilotesErrorCode errorCode, Exception ex, WebRequest request)
    {
        PilotesErrorResponse response = new PilotesErrorResponse(errorCode);
        return getResponseEntity(response, ex, request);
    }

    private ResponseEntity<Object> getResponseEntity(PilotesErrorResponse response, Exception ex, WebRequest request)
    {
        HttpStatus status = HttpStatus.resolve(response.getStatus());
        logError(response, ex, request);
        return new ResponseEntity<Object>(response, status);
    }

    private void logError(PilotesErrorResponse error, Exception ex, WebRequest request)
    {
        String uri = getUri(request);
        log.error("Request [{}]:{}", uri, error, ex);
    }

    private String getUri(WebRequest webRequest)
    {
        return Optional.ofNullable(webRequest)
                .map(r -> (ServletWebRequest) r)
                .map(ServletWebRequest::getRequest)
                .map(HttpServletRequest::getRequestURI)
                .orElse(StringUtils.EMPTY);
    }

}
