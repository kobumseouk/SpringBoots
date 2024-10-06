package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.event.*;
import com.spring_boots.spring_boots.category.entity.Event;
import com.spring_boots.spring_boots.category.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private EventMapper eventMapper;

  @InjectMocks
  private EventService eventService;


  @Test
  @DisplayName("이벤트 생성 확인 테스트")
  void createEvent() {
    // given
    EventRequestDto requestDto = EventRequestDto.builder()
        .eventTitle("Test Event")
        .eventContent("Test Content")
        .build();

    Event event = Event.builder()
        .id(1L)
        .eventTitle("Test Event")
        .build();

    EventDetailDto responseDto = EventDetailDto.builder()
        .id(1L)
        .eventTitle("Test Event")
        .build();
    
    when(eventMapper.eventRequestDtoToEvent(requestDto)).thenReturn(event);
    when(eventRepository.save(any(Event.class))).thenReturn(event);
    when(eventMapper.eventToEventDetailDto(event)).thenReturn(responseDto);

    // when
    EventDetailDto result = eventService.createEvent(requestDto);

    // then
    assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);

    verify(eventRepository).save(any(Event.class));

  }


  @Test
  @DisplayName("진행 중인 이벤트 목록 확인 테스트")
  void getActiveEvents() {
    // given
    Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
    Event event1 = Event.builder().id(1L).isActive(true).build();
    Event event2 = Event.builder().id(2L).isActive(true).build();
    Page<Event> eventPage = new PageImpl<>(Arrays.asList(event1, event2), pageable, 2);

    when(eventRepository.findAll(pageable)).thenReturn(eventPage);
    when(eventMapper.eventToEventDto(any(Event.class))).thenReturn(new EventDto());

    // when
    Page<EventDto> result = eventService.getActiveEvents(pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);

    verify(eventRepository).findAll(pageable);
    verify(eventMapper, times(2)).eventToEventDto(any(Event.class));

  }


  @Test
  @DisplayName("이벤트 상세 조회 확인 테스트")
  void getEventDetail() {
    // given
    Long eventId = 1L;
    Event event = Event.builder().id(eventId).build();
    EventDetailDto detailDto = EventDetailDto.builder()
        .id(eventId)
        .eventTitle("테스트 이벤트")
        .build();

    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
    when(eventMapper.eventToEventDetailDto(event)).thenReturn(detailDto);

    // when
    EventDetailDto result = eventService.getEventDetail(eventId);

    // then
    assertThat(result).usingRecursiveComparison().isEqualTo(detailDto);

    verify(eventRepository).findById(eventId);

  }


  @Test
  @DisplayName("이벤트 업데이트 확인 테스트")
  void updateEvent() {
    // given
    Long eventId = 1L;
    EventRequestDto updateDto = EventRequestDto.builder()
        .eventTitle("Updated Event")
        .eventContent("Updated Content")
        .build();

    Event existingEvent = Event.builder().id(eventId).build();

    EventDetailDto responseDto = EventDetailDto.builder()
        .id(eventId)
        .eventTitle("Updated Event")
        .build();
    
    
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
    when(eventRepository.save(any(Event.class))).thenReturn(existingEvent);
    when(eventMapper.eventToEventDetailDto(existingEvent)).thenReturn(responseDto);

    // when
    EventDetailDto result = eventService.updateEvent(eventId, updateDto);

    // then
    assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);

    verify(eventRepository).findById(eventId);
    verify(eventRepository).save(any(Event.class));

  }


  @Test
  @DisplayName("이벤트 삭제 확인 테스트")
  void deleteEvent() {
    // given
    Long eventId = 1L;
    Event event = Event.builder().id(eventId).build();
    
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

    // when
    eventService.deleteEvent(eventId);

    // then
    verify(eventRepository).findById(eventId);
    verify(eventRepository).deleteById(eventId);

  }

}