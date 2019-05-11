package com.app.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.model.jpa.OrderHistory;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/5/4.
 */
@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory,String> {
}
