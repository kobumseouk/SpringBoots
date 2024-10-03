package com.spring_boots.spring_boots.category.repository;

import com.spring_boots.spring_boots.category.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
  List<Event> findByIsActiveTrue();
}
