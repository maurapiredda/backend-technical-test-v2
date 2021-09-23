package com.tui.proof.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tui.proof.model.Customer;

@Repository
public interface CustomerDao extends JpaRepository<Customer, Long>
{

}
