package com.tui.proof.service;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.tui.proof.dao.CustomerDao;
import com.tui.proof.error.PilotesErrorCode;
import com.tui.proof.error.PilotesException;
import com.tui.proof.model.Customer;

/**
 * A {@link Service} that holds the business logic for {@link Customer}
 * @author maura.piredda
 */
@Service
@Transactional
public class CustomerService
{
    @Autowired
    private CustomerDao customerDao;

    /**
     * Returns the customer identified by the given {@code email}
     * @param email the customer's identifier
     * @return the customer identified by the given {@code email} or null if it doesn't exist any customer with given
     *         {@code email}
     * @throws PilotesException with {@link PilotesErrorCode#CUSTOMER_EMAIL_EMPTY} if {@code email} is blank
     */
    public Customer getByEmail(String email)
    {
        if (StringUtils.isEmpty(email))
        {
            throw new PilotesException(PilotesErrorCode.CUSTOMER_EMAIL_EMPTY);
        }
        Customer customer = new Customer();
        customer.setEmail(email);
        return customerDao.findOne(Example.of(customer)).orElse(null);
    }
}
