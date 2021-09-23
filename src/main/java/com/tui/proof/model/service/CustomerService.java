package com.tui.proof.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.tui.proof.dao.CustomerDao;
import com.tui.proof.model.Customer;

@Service
public class CustomerService
{
   @Autowired
   private CustomerDao customerDao;

   public Customer getByEmail(String email)
   {
       Customer customer = new Customer();
       customer.setEmail(email);
       return customerDao.findOne(Example.of(customer)).orElse(null);
   }
}
