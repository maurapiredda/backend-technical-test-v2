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

import com.tui.proof.dao.AddressDao;
import com.tui.proof.error.PilotesErrorCode;
import com.tui.proof.error.PilotesException;
import com.tui.proof.model.Address;

@ExtendWith(EasyMockExtension.class)
public class AddressServiceTest
{
    @Mock
    private AddressDao addressDao;

    @TestSubject
    private AddressService addressService;

    @Test
    @SuppressWarnings("unchecked")
    public void testFind()
    {
        String country = "Italy";
        Address expectedAddress = new Address();
        expectedAddress.setCountry(country);
        
        EasyMock.reset(addressDao);
        EasyMock.expect(addressDao.findOne((Example<Address>) EasyMock.anyObject()))
                .andReturn(Optional.of(expectedAddress));
        EasyMock.replay(addressDao);

        Address address = addressService.find(new Address()).orElse(null);
        Assert.assertEquals(country, address.getCountry());
        EasyMock.verify(addressDao);
    }
    
    @Test
    public void testFindNullFilter()
    {
        try
        {
            addressService.find(null);
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(PilotesErrorCode.ADDRESS_NULL, e.getPilotesErrorCode());
        }
    }
    
    @Test
    public void testSave()
    {
        String country = "Italy";
        Address expectedAddress = new Address();
        expectedAddress.setCountry(country);
        
        EasyMock.reset(addressDao);
        EasyMock.expect(addressDao.save(EasyMock.anyObject(Address.class)))
                .andReturn(expectedAddress);
        EasyMock.replay(addressDao);
        
        Address address = addressService.save(new Address());
        Assert.assertEquals(country, address.getCountry());
        EasyMock.verify(addressDao);
    }
    
    @Test
    public void testSaveNull()
    {
        try
        {
            addressService.save(null);
            Assert.fail("Expected PilotesException");
        }
        catch (PilotesException e)
        {
            Assert.assertEquals(PilotesErrorCode.ADDRESS_NULL, e.getPilotesErrorCode());
        }
    }
}
