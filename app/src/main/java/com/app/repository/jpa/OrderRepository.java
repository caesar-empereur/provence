package com.app.repository.jpa;

import com.app.model.jpa.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/26.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order,String> {
}
