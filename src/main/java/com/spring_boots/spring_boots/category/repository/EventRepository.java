package com.spring_boots.spring_boots.category.repository;

import com.spring_boots.spring_boots.category.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;


public interface EventRepository extends JpaRepository<Event, Long> {
  @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.endDate >= :currentDate")
  Page<Event> findActiveEvents(LocalDate currentDate, Pageable pageable);

}
