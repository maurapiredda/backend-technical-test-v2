package com.tui.proof.service;

import java.util.Optional;

import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Example;

import com.tui.proof.dao.CustomerDao;
import com.tui.proof.error.PilotesErrorCode;
import com.tui.proof.error.PilotesException;
import com.tui.proof.model.Customer;

@ExtendWith(EasyMockExtension.class)
public class CustomerServiceTest
{
    @Mock
    private CustomerDao customerDao;

    @TestSubject
    private CustomerService customerService;

    @Test
    @SuppressWarnings("unchecked")
    public void testGetByEmail()
    {
        String email = "email";
        Customer expectedCustomer = new Customer();
        expectedCustomer.setEmail(email);

        EasyMock.reset(customerDao);
        EasyMock.expect(customerDao.findOne((Example<Customer>) EasyMock.anyObject()))
                .andReturn(Optional.of(expectedCustomer));
        EasyMock.replay(customerDao);

        Customer customer = customerService.getByEmail(email).orElse(null);
        Assert.assertEquals(email, customer.getEmail());
        EasyMock.verify(customerDao);
    }

    @Test
    public void testGetByEmailEmptyEmail()
    {
        try
        {
            customerService.getByEmail(null);
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(PilotesErrorCode.CUSTOMER_EMAIL_EMPTY, e.getPilotesErrorCode());
        }

        try
        {
            customerService.getByEmail("");
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(PilotesErrorCode.CUSTOMER_EMAIL_EMPTY, e.getPilotesErrorCode());
        }
    }

}
