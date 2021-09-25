package com.tui.proof.model.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.tui.proof.dao.AddressDao;
import com.tui.proof.error.PilotesErrorCode;
import com.tui.proof.error.PilotesException;
import com.tui.proof.model.Address;

/**
 * A {@link Service} that holds the business logic for {@link Address}
 * @author maura.piredda
 */
@Service
@Transactional
public class AddressService
{
    @Autowired
    private AddressDao addressDao;

    /**
     * Returns the Address that matches with given {@code filter}
     * @param filter the filter
     * @return the Address that matches with given {@code filter} or null if it doesn't exist
     * @throws PilotesException with {@link PilotesErrorCode#ADDRESS_NULL} if {@code filter} is null
     */
    public Address find(Address filter)
    {
        if (filter == null)
        {
            throw new PilotesException(PilotesErrorCode.ADDRESS_NULL);
        }
        return addressDao.findOne(Example.of(filter)).orElse(null);
    }

    /**
     * Saves the {@code address}
     * @param address the address to save
     * @return the saved address
     * @throws PilotesException with {@link PilotesErrorCode#ADDRESS_NULL} if {@code filter} is null
     */
    public Address save(Address address)
    {
        if (address == null)
        {
            throw new PilotesException(PilotesErrorCode.ADDRESS_NULL);
        }
        return addressDao.save(address);
    }

}
