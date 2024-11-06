package com.spring_boots.spring_boots.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring_boots.spring_boots.orders.entity.ShippingInfo;

public interface ShippingInfoRepository extends JpaRepository<ShippingInfo, Long> {

}
