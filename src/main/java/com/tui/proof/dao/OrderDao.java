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
    /**
     * Returns the list of the orders that can be notified, that are the orders that are not yet notified and that are
     * older than {@code nowMinusExpirationMinutes}, i.e. that cannot be updated anymore.
     * @param nowMinusExpirationMinutes the date to compare with the order's creation date. An order cannot be updated
     *                                  anymore if it is older than this date, thus it can be notified.
     * @return the list of the orders that can be notified
     */
    List<Order> findByCreationDateBeforeAndNotifiedFalse(ZonedDateTime nowMinusExpirationMinutes);

    /**
     * @return the next value of the order's sequence
     */
    // this query is database dependent, it must be customized if the database changes
    @Query(value = "call nextval('order_id_seq')", nativeQuery = true)
    BigInteger getNextOrderSequenceValue();
}
