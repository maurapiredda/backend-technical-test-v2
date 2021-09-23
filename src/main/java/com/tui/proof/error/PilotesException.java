package com.tui.proof.error;

import lombok.Getter;

/**
 * A generic exception to throw when an error occurs
 * @author maura.piredda
 */
public class PilotesException extends RuntimeException
{

    private static final long serialVersionUID = 7921964441158472384L;

    private final transient @Getter
    PilotesErrorCode pilotesErrorCode;

    public PilotesException(PilotesErrorCode pilotesErrorCode)
    {
        super(pilotesErrorCode.getMessage());
        this.pilotesErrorCode = pilotesErrorCode;
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append(pilotesErrorCode).append(super.toString()).toString();
    }
}
