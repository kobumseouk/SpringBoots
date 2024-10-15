package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.event.*;
import com.spring_boots.spring_boots.category.entity.Event;
import com.spring_boots.spring_boots.category.repository.EventRepository;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

  @Mock
  private S3BucketService s3BucketService;

  @InjectMocks
  private EventService eventService;

  private Event mockEvent;
  private EventDto mockEventDto;
  private EventDetailDto mockEventDetailDto;
  private EventAdminDto mockEventAdminDto;
  private MockMultipartFile mockThumbnailFile;
  private MockMultipartFile mockContentFile;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    String thumbnailUrl = "http://test-url.com/thumbnail.jpg";
    String contentUrl = "http://test-url.com/content.jpg";

    mockEvent = Event.builder()
        .id(1L)
        .eventTitle("Test Event")
        .eventContent("Test Content")
        .isActive(true)
        .thumbnailImageUrl(thumbnailUrl)
        .contentImageUrl(contentUrl)
        .build();

    mockEventDto = EventDto.builder()
        .id(1L)
        .eventTitle("Test Event")
        .thumbnailImageUrl(thumbnailUrl)
        .build();

    mockEventDetailDto = EventDetailDto.builder()
        .id(1L)
        .eventTitle("Test Event")
        .eventContent("Test Content")
        .thumbnailImageUrl(thumbnailUrl)
        .contentImageUrl(contentUrl)
        .build();

    mockEventAdminDto = EventAdminDto.builder()
        .id(1L)
        .eventTitle("Test Event")
        .eventContent("Test Content")
        .isActive(true)
        .build();

    mockThumbnailFile = new MockMultipartFile("thumbnailFile", "thumbnail.jpg", "image/jpeg", "thumbnail content".getBytes());
    mockContentFile = new MockMultipartFile("contentFile", "content.jpg", "image/jpeg", "content image content".getBytes());
  }


  private final Long INVALID_EVENT_ID = 99999L;


  @Test
  @DisplayName("이벤트 생성 확인 테스트")
  void createEvent() throws IOException {
    // given
    EventRequestDto requestDto = EventRequestDto.builder()
        .eventTitle(mockEvent.getEventTitle())
        .eventContent(mockEvent.getEventContent())
        .build();

    when(s3BucketService.uploadFile(any(MultipartFile.class)))
        .thenReturn(mockEvent.getThumbnailImageUrl())
        .thenReturn(mockEvent.getContentImageUrl());
    when(eventMapper.eventRequestDtoToEvent(any(EventRequestDto.class))).thenReturn(mockEvent);
    when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
    when(eventMapper.eventToEventDetailDto(any(Event.class))).thenReturn(mockEventDetailDto);

    // when
    EventDetailDto result = eventService.createEvent(requestDto, mockThumbnailFile, mockContentFile);

    // then
    assertNotNull(result);
    assertEquals(mockEventDetailDto.getEventTitle(), result.getEventTitle());
    assertEquals(mockEventDetailDto.getThumbnailImageUrl(), result.getThumbnailImageUrl());
    assertEquals(mockEventDetailDto.getContentImageUrl(), result.getContentImageUrl());
    verify(s3BucketService, times(2)).uploadFile(any(MultipartFile.class));
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
    assertEquals(mockEventDto.getEventTitle(), result.getContent().get(0).getEventTitle());
    verify(eventRepository).findAll(any(Pageable.class));
  }

  @Test
  @DisplayName("이벤트 상세 조회 확인 테스트")
  void getEventDetail() {
    // given
    Long eventId = mockEvent.getId();
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
    when(eventMapper.eventToEventDetailDto(mockEvent)).thenReturn(mockEventDetailDto);

    // when
    EventDetailDto result = eventService.getEventDetail(eventId);

    // then
    assertNotNull(result);
    assertEquals(mockEventDetailDto.getEventTitle(), result.getEventTitle());
    assertEquals(mockEventDetailDto.getEventContent(), result.getEventContent());
    verify(eventRepository).findById(eventId);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 이벤트 상세 조회 시 예외 발생 확인 테스트")
  void getEventDetail_유효하지않은ID_예외발생() {
    // given
    when(eventRepository.findById(INVALID_EVENT_ID)).thenReturn(Optional.empty());

    // when & then
    assertThrows(ResourceNotFoundException.class, () -> eventService.getEventDetail(INVALID_EVENT_ID));
    verify(eventRepository).findById(INVALID_EVENT_ID);
  }

  @Test
  @DisplayName("이벤트 업데이트 확인 테스트")
  void updateEvent() throws IOException {
    // given
    Long eventId = mockEvent.getId();
    EventRequestDto updateDto = EventRequestDto.builder()
        .eventTitle("Updated Event")
        .eventContent("Updated Content")
        .build();

    Event updatedEvent = Event.builder()
        .id(eventId)
        .eventTitle("Updated Event")
        .eventContent("Updated Content")
        .thumbnailImageUrl("http://test-url.com/updated-thumbnail.jpg")
        .contentImageUrl("http://test-url.com/updated-content.jpg")
        .build();

    when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
    when(s3BucketService.uploadFile(any(MultipartFile.class)))
        .thenReturn(updatedEvent.getThumbnailImageUrl())
        .thenReturn(updatedEvent.getContentImageUrl());
    when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);
    when(eventMapper.eventToEventDetailDto(updatedEvent)).thenReturn(
        EventDetailDto.builder()
            .id(eventId)
            .eventTitle(updateDto.getEventTitle())
            .eventContent(updateDto.getEventContent())
            .thumbnailImageUrl(updatedEvent.getThumbnailImageUrl())
            .contentImageUrl(updatedEvent.getContentImageUrl())
            .build()
    );

    // when
    EventDetailDto result = eventService.updateEvent(eventId, updateDto, mockThumbnailFile, mockContentFile);

    // then
    assertNotNull(result);
    assertEquals(updateDto.getEventTitle(), result.getEventTitle());
    assertEquals(updateDto.getEventContent(), result.getEventContent());
    assertEquals(updatedEvent.getThumbnailImageUrl(), result.getThumbnailImageUrl());
    assertEquals(updatedEvent.getContentImageUrl(), result.getContentImageUrl());
    verify(s3BucketService, times(2)).deleteFile(anyString());
    verify(s3BucketService, times(2)).uploadFile(any(MultipartFile.class));
    verify(eventRepository).save(any(Event.class));
  }

  @Test
  @DisplayName("존재하지 않는 ID로 이벤트 업데이트 시 예외 발생 확인 테스트")
  void updateEvent_유효하지않은ID_예외발생() throws IOException {
    // given
    EventRequestDto updateDto = EventRequestDto.builder()
        .eventTitle("Updated Event")
        .eventContent("Updated Content")
        .build();

    when(eventRepository.findById(INVALID_EVENT_ID)).thenReturn(Optional.empty());

    // when & then
    assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(INVALID_EVENT_ID, updateDto, mockThumbnailFile, mockContentFile));
    verify(eventRepository).findById(INVALID_EVENT_ID);
    verify(eventRepository, never()).save(any(Event.class));
  }

  @Test
  @DisplayName("이벤트 삭제 확인 테스트")
  void deleteEvent() {
    // given
    Long eventId = mockEvent.getId();
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));

    // when
    eventService.deleteEvent(eventId);

    // then
    verify(eventRepository).findById(eventId);
    verify(s3BucketService, times(2)).deleteFile(anyString());  // 썸네일과 컨텐츠 이미지
    verify(eventRepository).deleteById(eventId);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 이벤트 삭제 시 예외 발생 확인 테스트")
  void deleteEvent_유효하지않은ID_예외발생() {
    // given
    when(eventRepository.findById(INVALID_EVENT_ID)).thenReturn(Optional.empty());

    // when & then
    assertThrows(ResourceNotFoundException.class, () -> eventService.deleteEvent(INVALID_EVENT_ID));
    verify(eventRepository).findById(INVALID_EVENT_ID);
    verify(eventRepository, never()).deleteById(INVALID_EVENT_ID);
  }

  @Test
  @DisplayName("관리자용 이벤트 목록 조회 확인 테스트")
  void getAdminEvents() {
    // given
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<Event> eventPage = new PageImpl<>(Arrays.asList(mockEvent, mockEvent));
    when(eventRepository.findAll(pageRequest)).thenReturn(eventPage);
    when(eventMapper.eventToEventAdminDto(any(Event.class))).thenReturn(mockEventAdminDto);

    // when
    Page<EventAdminDto> result = eventService.getAdminEvents(0, 10);

    // then
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertEquals(mockEventAdminDto.getEventTitle(), result.getContent().get(0).getEventTitle());
    assertEquals(0, result.getNumber());
    assertEquals(2, result.getSize());   // mockEvent 항목 2개 반환
    verify(eventRepository).findAll(pageRequest);
    verify(eventMapper, times(2)).eventToEventAdminDto(any(Event.class));
  }

  @Test
  @DisplayName("관리자용 개별 이벤트 조회 확인 테스트")
  void getAdminEvent() {
    // given
    Long eventId = mockEvent.getId();
    when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
    when(eventMapper.eventToEventAdminDto(mockEvent)).thenReturn(mockEventAdminDto);

    // when
    EventAdminDto result = eventService.getAdminEvent(eventId);

    // then
    assertNotNull(result);
    assertEquals(mockEventAdminDto.getId(), result.getId());
    assertEquals(mockEventAdminDto.getEventTitle(), result.getEventTitle());
    verify(eventRepository).findById(eventId);
    verify(eventMapper).eventToEventAdminDto(mockEvent);
  }


  @Test
  @DisplayName("존재하지 않는 ID로 관리자용 개별 이벤트 조회 시 예외 발생 확인 테스트")
  void getAdminEvent_유효하지않은ID_예외발생() {
    // given
    when(eventRepository.findById(INVALID_EVENT_ID)).thenReturn(Optional.empty());

    // when & then
    assertThrows(ResourceNotFoundException.class, () -> eventService.getAdminEvent(INVALID_EVENT_ID));
    verify(eventRepository).findById(INVALID_EVENT_ID);
  }

}