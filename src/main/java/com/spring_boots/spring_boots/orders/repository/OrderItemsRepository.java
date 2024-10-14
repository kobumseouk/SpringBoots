package com.spring_boots.spring_boots.orders.repository;

import com.spring_boots.spring_boots.orders.entity.OrderItems;
import com.spring_boots.spring_boots.orders.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
    List<OrderItems> findByOrders(Orders orders);

    void deleteAllByItem_ItemId(Long itemId);
}
