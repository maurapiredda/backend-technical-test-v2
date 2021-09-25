package com.tui.proof.error;

import java.text.MessageFormat;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * The information to return in case of error <br>
 * <li><b>message: </b>a human readable explanation of the error</li>
 * <li><b>status: </b>the HTTP response code</li>
 * <li><b>error: </b>the description of the HTTP response code</li>
 * <li><b>timestamp: </b>the instant when the error occurs</li>
 * @author maura.piredda
 */
@Data
@Schema(description = "The error info")
public class PilotesErrorResponse
{
    @Schema(description = "A human readable explanation of the error.")
    private String message;

    @Schema(description = "The HTTP response code.")
    private int status;

    @Schema(description = "The description of the HTTP response code.")
    private String error;

    @Schema(description = "The instant when the error occurs")
    private ZonedDateTime timestamp;

    public PilotesErrorResponse(PilotesErrorCode errorCode)
    {
        HttpStatus httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
        this.status = httpStatus.value();
        this.error = httpStatus.getReasonPhrase();
        this.timestamp = ZonedDateTime.now();
    }

    @Override
    public String toString()
    {
        String line = "\n\t{0}: {1}";
        StringBuilder builder = new StringBuilder();
        builder.append("\n").append(PilotesErrorCode.class.getSimpleName())
        .append(MessageFormat.format(line, "message", message))
        .append(MessageFormat.format(line, "status", status))
        .append(MessageFormat.format(line, "error", error))
        .append(MessageFormat.format(line, "timestamp", timestamp));
        return builder.toString();
    }
}
