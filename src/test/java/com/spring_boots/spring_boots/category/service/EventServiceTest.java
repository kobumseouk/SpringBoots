package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.event.*;
import com.spring_boots.spring_boots.category.entity.Event;
import com.spring_boots.spring_boots.category.repository.EventRepository;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class EventServiceTest {

  @Mock
  private EventRepository eventRepository;

  @Mock
  private EventMapper eventMapper;

  @InjectMocks
  private EventService eventService;

  private Event mockEvent;
  private EventDto mockEventDto;
  private EventDetailDto mockEventDetailDto;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    mockEvent = Event.builder()
        .id(1L)
        .eventTitle("Test Event")
        .eventContent("Test Content")
        .isActive(true)
        .build();

    mockEventDto = EventDto.builder()
        .id(1L)
        .eventTitle("Test Event")
        .build();

    mockEventDetailDto = EventDetailDto.builder()
        .id(1L)
        .eventTitle("Test Event")
        .eventContent("Test Content")
        .build();
  }


  private final Long INVALID_EVENT_ID = 99999L;


  @Test
  @DisplayName("이벤트 생성 확인 테스트")
  void createEvent() {
    // given
    EventRequestDto requestDto = EventRequestDto.builder()
        .eventTitle("Test Event")
        .eventContent("Test Content")
        .build();

    when(eventMapper.eventRequestDtoToEvent(any(EventRequestDto.class))).thenReturn(mockEvent);
    when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
    when(eventMapper.eventToEventDetailDto(any(Event.class))).thenReturn(mockEventDetailDto);

    // when
    EventDetailDto result = eventService.createEvent(requestDto);

    // then
    assertNotNull(result);
    assertEquals("Test Event", result.getEventTitle());
    verify(eventRepository).save(any(Event.class));
  }

  @Test
  @DisplayName("진행 중인 이벤트 목록 확인 테스트")
  void getActiveEvents() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    Page<Event> eventPage = new PageImpl<>(Arrays.asList(mockEvent, mockEvent));

    when(eventRepository.findAll(any(Pageable.class))).thenReturn(eventPage);
    when(eventMapper.eventToEventDto(any(Event.class))).thenReturn(mockEventDto);

    // when
    Page<EventDto> result = eventService.getActiveEvents(pageable);

    // then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(eventRepository).findAll(any(Pageable.class));
  }

  @Test
  @DisplayName("이벤트 상세 조회 확인 테스트")
  void getEventDetail() {
    // given
    Long eventId = 1L;
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
    when(eventMapper.eventToEventDetailDto(mockEvent)).thenReturn(mockEventDetailDto);

    // when
    EventDetailDto result = eventService.getEventDetail(eventId);

    // then
    assertNotNull(result);
    assertEquals("Test Event", result.getEventTitle());
    verify(eventRepository).findById(eventId);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 이벤트 상세 조회 시 예외 발생 확인 테스트")
  void getEventDetail_WithInvalidId_ShouldThrowResourceNotFoundException() {
    // given
    when(eventRepository.findById(INVALID_EVENT_ID)).thenReturn(Optional.empty());

    // when & then
    assertThrows(ResourceNotFoundException.class, () -> eventService.getEventDetail(INVALID_EVENT_ID));
    verify(eventRepository).findById(INVALID_EVENT_ID);
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

    when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
    when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
    when(eventMapper.eventToEventDetailDto(any(Event.class))).thenReturn(mockEventDetailDto);

    // when
    EventDetailDto result = eventService.updateEvent(eventId, updateDto);

    // then
    assertNotNull(result);
    assertEquals("Test Event", result.getEventTitle());
    verify(eventRepository).findById(eventId);
    verify(eventRepository).save(any(Event.class));
  }

  @Test
  @DisplayName("존재하지 않는 ID로 이벤트 업데이트 시 예외 발생 확인 테스트")
  void updateEvent_WithInvalidId_ShouldThrowResourceNotFoundException() {
    // given
    EventRequestDto updateDto = EventRequestDto.builder()
        .eventTitle("Updated Event")
        .eventContent("Updated Content")
        .build();

    when(eventRepository.findById(INVALID_EVENT_ID)).thenReturn(Optional.empty());

    // when & then
    assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(INVALID_EVENT_ID, updateDto));
    verify(eventRepository).findById(INVALID_EVENT_ID);
    verify(eventRepository, never()).save(any(Event.class));
  }

  @Test
  @DisplayName("이벤트 삭제 확인 테스트")
  void deleteEvent() {
    // given
    Long eventId = 1L;
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));

    // when
    eventService.deleteEvent(eventId);

    // then
    verify(eventRepository).findById(eventId);
    verify(eventRepository).deleteById(eventId);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 이벤트 삭제 시 예외 발생 확인 테스트")
  void deleteEvent_WithInvalidId_ShouldThrowResourceNotFoundException() {
    // given
    when(eventRepository.findById(INVALID_EVENT_ID)).thenReturn(Optional.empty());

    // when & then
    assertThrows(ResourceNotFoundException.class, () -> eventService.deleteEvent(INVALID_EVENT_ID));
    verify(eventRepository).findById(INVALID_EVENT_ID);
    verify(eventRepository, never()).deleteById(INVALID_EVENT_ID);
  }

}