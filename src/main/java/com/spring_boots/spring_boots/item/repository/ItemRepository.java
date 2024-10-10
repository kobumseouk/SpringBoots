package com.spring_boots.spring_boots.item.repository;

import com.spring_boots.spring_boots.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
  List<Item> findAllByCategoryId(Long categoryId);
}
