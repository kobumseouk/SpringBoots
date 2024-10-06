package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.event.EventMapper;
import com.spring_boots.spring_boots.category.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private EventMapper eventMapper;

  @InjectMocks
  private EventService eventService;

  @Test
  void createEvent() {
    // given


    // when


    // then


  }

  @Test
  void getActiveEvents() {
    // given


    // when


    // then


  }

  @Test
  void getEventDetail() {
    // given


    // when


    // then


  }

  @Test
  void updateEvent() {
    // given


    // when


    // then


  }

  @Test
  void deleteEvent() {
    // given


    // when


    // then


  }

}