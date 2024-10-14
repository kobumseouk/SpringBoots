package com.spring_boots.spring_boots.category.service;

import com.spring_boots.spring_boots.category.dto.event.EventDetailDto;
import com.spring_boots.spring_boots.category.dto.event.EventDto;
import com.spring_boots.spring_boots.category.dto.event.EventMapper;
import com.spring_boots.spring_boots.category.dto.event.EventRequestDto;
import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.entity.Event;
import com.spring_boots.spring_boots.category.repository.CategoryRepository;
import com.spring_boots.spring_boots.category.repository.EventRepository;
import com.spring_boots.spring_boots.common.config.error.ResourceNotFoundException;
import com.spring_boots.spring_boots.s3Bucket.service.S3BucketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

  @Transactional
  public Page<EventDto> getActiveEvents(Pageable pageable) {
    // 모든 이벤트를 조회하고 상태를 업데이트
    Page<Event> allEvents = eventRepository.findAll(pageable);
    allEvents.forEach(Event::updateActiveStatus);

    // 활성 상태인 이벤트만 필터링
    List<EventDto> activeEventDtos = allEvents.getContent().stream()
        .filter(Event::getIsActive)
        .map(eventMapper::eventToEventDto)
        .collect(Collectors.toList());

    // 새로운 Page 객체 생성
    return new PageImpl<>(activeEventDtos, pageable, allEvents.getTotalElements());
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

}
