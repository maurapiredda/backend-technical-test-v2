package com.tui.proof.model;

/**
 * The allowed number of pilotes per order
 * @author maura.piredda
 */
public enum PilotesNumber
{
    FIVE(5), TEN(10), FIFTEEN(15);

    int number;

    private PilotesNumber(int number)
    {
        this.number = number;
    }

    public int getNumber()
    {
        return number;
    }

}
