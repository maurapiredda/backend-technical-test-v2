package com.tui.proof.dao;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tui.proof.model.Order;

@Repository
public interface OrderDao extends JpaRepository<Order, Long>
{
    List<Order> findByCreationDateBeforeAndNotifiedFalse(ZonedDateTime nowMinusExpirationMinutes);
    
    @Query(value="call nextval('order_id_seq')", nativeQuery = true)
    BigInteger getNextOrderSequenceValue();
}
