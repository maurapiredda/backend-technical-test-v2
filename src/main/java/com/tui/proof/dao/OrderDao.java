package com.tui.proof.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tui.proof.model.Order;

@Repository
public interface OrderDao extends JpaRepository<Order, Long>
{
    
}
