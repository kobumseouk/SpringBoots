package com.spring_boots.spring_boots.category.repository;

import com.spring_boots.spring_boots.category.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;


public interface EventRepository extends JpaRepository<Event, Long> {
  Page<Event> findByEndDateGreaterThanEqual(LocalDate date, Pageable pageable);
}
