package com.tui.proof.error;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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
        return getResponseEntity(PilotesErrorCode.INVALID_INPUT, ex, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatus status, WebRequest request)
    {
        return getResponseEntity(PilotesErrorCode.UNEXPECTED_ERROR, ex, request);
    }

    private ResponseEntity<Object> getResponseEntity(PilotesErrorCode errorCode, Exception ex, WebRequest request)
    {
        PilotesErrorResponse response = new PilotesErrorResponse(errorCode);
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
