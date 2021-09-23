package com.tui.proof.model.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.tui.proof.dao.AddressDao;
import com.tui.proof.model.Address;

/**
 * A {@link Service} that holds the business logic for the addresses
 * @author maura.piredda
 */
@Service
@Transactional
public class AddressService
{
    @Autowired
    private AddressDao addressDao;

    public Address find(Address address)
    {
        if (address == null)
        {
            // TODO error?
            return null;
        }
        return addressDao.findOne(Example.of(address)).orElse(null);
    }

    public Address save(Address address)
    {
        if (address == null)
        {
            // TODO error
            return null;
        }
        return addressDao.save(address);
    }

}
