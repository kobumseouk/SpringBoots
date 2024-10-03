package com.spring_boots.spring_boots.orders.repository;

import com.spring_boots.spring_boots.orders.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;



public interface OrdersRepository extends JpaRepository<Orders, Long> {

}
