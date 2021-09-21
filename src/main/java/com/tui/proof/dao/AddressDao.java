package com.tui.proof.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tui.proof.model.Address;

@Repository
public interface AddressDao extends JpaRepository<Address, Long>
{

}

