package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.event.*;
import com.spring_boots.spring_boots.category.entity.Event;
import com.spring_boots.spring_boots.category.repository.EventRepository;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final S3BucketService s3BucketService;
  private final EventMapper eventMapper;


  // 새로운 이벤트를 생성하는 메서드
  @Transactional
  public EventDetailDto createEvent(EventRequestDto eventRequestDto, MultipartFile thumbnailFile, MultipartFile contentFile) throws IOException {
    String thumbnailImageUrl = null;
    String contentImageUrl = null;

    if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
      thumbnailImageUrl = s3BucketService.uploadFile(thumbnailFile);
    }
    if (contentFile != null && !contentFile.isEmpty()) {
      contentImageUrl = s3BucketService.uploadFile(contentFile);
    }

    Event event = eventMapper.eventRequestDtoToEvent(eventRequestDto);
    event.setThumbnailImageUrl(thumbnailImageUrl);
    event.setContentImageUrl(contentImageUrl);
    Event savedEvent = eventRepository.save(event);
    return eventMapper.eventToEventDetailDto(savedEvent);
  }

  // 모든 활성화된 이벤트를 조회하는 메서드
  /*public List<EventDto> getAllActiveEvents() {
    List<Event> events = eventRepository.findAll();
    events.forEach(Event::updateActiveStatus);
    return events.stream()
        .filter(Event::getIsActive)
        .map(eventMapper::eventToEventDto)
        .collect(Collectors.toList());
  }*/

  // 진행 중인 이벤트인지 확인 후 진행 중인 목록 조회 (페이지네이션)
  @Transactional
  public Page<EventDto> getActiveEvents(int page, int limit) {
    PageRequest pageRequest = PageRequest.of(page, limit);
    Page<Event> activePage = eventRepository.findByIsActiveTrue(pageRequest);
    activePage.forEach(Event::updateActiveStatus);

    return activePage.map(eventMapper::eventToEventDto);
  }

  // 특정 이벤트의 상세 정보를 조회하는 메서드
  public EventDetailDto getEventDetail(Long eventId) {
    // 이벤트를 찾아 반환, 없으면 ResourceNotFoundException 발생
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("조회할 이벤트를 찾을 수 없습니다: " + eventId));
    return eventMapper.eventToEventDetailDto(event);
  }

  // 특정 이벤트를 수정하는 메서드
  @Transactional
  public EventDetailDto updateEvent(Long eventId, EventRequestDto eventUpdateDto, MultipartFile thumbnailFile, MultipartFile contentFile) throws IOException {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("업데이트할 이벤트를 찾을 수 없습니다: " + eventId));

    if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
      String newThumbnailImageUrl = s3BucketService.uploadFile(thumbnailFile);
      if (event.getThumbnailImageUrl() != null) {
        s3BucketService.deleteFile(event.getThumbnailImageUrl().substring(event.getThumbnailImageUrl().lastIndexOf("/") + 1));
      }
      event.setThumbnailImageUrl(newThumbnailImageUrl);
    }

    if (contentFile != null && !contentFile.isEmpty()) {
      String newContentImageUrl = s3BucketService.uploadFile(contentFile);
      if (event.getContentImageUrl() != null) {
        s3BucketService.deleteFile(event.getContentImageUrl().substring(event.getContentImageUrl().lastIndexOf("/") + 1));
      }
      event.setContentImageUrl(newContentImageUrl);
    }

    eventMapper.updateEventFromDto(eventUpdateDto, event);
    Event updatedEvent = eventRepository.save(event);
    return eventMapper.eventToEventDetailDto(updatedEvent);
  }

  // 특정 이벤트를 삭제하는 메서드
  @Transactional
  public void deleteEvent(Long eventId) {
    // 이벤트가 존재하는지 확인 후 삭제, 없으면 ResourceNotFoundException 발생
    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("삭제할 이벤트를 찾을 수 없습니다: " + eventId));

    // S3에서 썸네일 이미지 삭제
    if (event.getThumbnailImageUrl() != null) {
      String thumbnailKey = extractKeyFromUrl(event.getThumbnailImageUrl());
      s3BucketService.deleteFile(thumbnailKey);
    }

    // S3에서 컨텐츠 이미지 삭제
    if (event.getContentImageUrl() != null) {
      String contentKey = extractKeyFromUrl(event.getContentImageUrl());
      s3BucketService.deleteFile(contentKey);
    }

    eventRepository.deleteById(eventId);
  }

  // URL에서 S3 키를 추출
  private String extractKeyFromUrl(String url) {
    return url.substring(url.lastIndexOf("/") + 1);
  }

  // 관리자용 이벤트 목록 페이지네이션 적용하여 조회
  public Page<EventAdminDto> getAdminEvents (int page, int limit) {
    PageRequest pageRequest = PageRequest.of(page, limit);
    Page<Event> eventPage = eventRepository.findAll(pageRequest);
    eventPage.forEach(Event::updateActiveStatus);

    return eventPage.map(eventMapper::eventToEventAdminDto);
  }

  // 관리자용 카테고리 개별 조회 - 카테고리 수정 시 사용
  public EventAdminDto getAdminEvent(Long eventId) {
    return eventRepository.findById(eventId)
        .map(eventMapper::eventToEventAdminDto)
        .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + eventId));
  }

}
