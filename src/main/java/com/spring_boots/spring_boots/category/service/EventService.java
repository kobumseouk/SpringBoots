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
import java.util.ArrayList;
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
  public EventDetailDto createEvent(EventRequestDto eventRequestDto, MultipartFile thumbnailFile, List<MultipartFile> contentFiles) throws IOException {
    String thumbnailImageUrl = null;
    List<String> contentImageUrls = new ArrayList<>();
    Event event = eventMapper.eventRequestDtoToEvent(eventRequestDto);

    if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
      thumbnailImageUrl = s3BucketService.uploadFile(thumbnailFile);
    }
    if (contentFiles != null && !contentFiles.isEmpty()) {
      for (MultipartFile file : contentFiles) {
        contentImageUrls.add(s3BucketService.uploadFile(file));
      }
    }


    event.setThumbnailImageUrl(thumbnailImageUrl != null ? getFullImageUrl(thumbnailImageUrl) : null);
    event.setContentImageUrl(contentImageUrls.stream()
        .map(this::getFullImageUrl)
        .collect(Collectors.toList()));
    Event savedEvent = eventRepository.save(event);
    return eventMapper.eventToEventDetailDto(savedEvent);
  }

  private String getFullImageUrl(String imageKey) {
    return s3BucketService.getFileUrl(imageKey);
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
  public EventDetailDto updateEvent(Long eventId, EventRequestDto eventUpdateDto, MultipartFile thumbnailFile, List<MultipartFile> contentFiles) throws IOException {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("업데이트할 이벤트를 찾을 수 없습니다: " + eventId));

    if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
      String newThumbnailImageUrl = s3BucketService.uploadFile(thumbnailFile);
      if (event.getThumbnailImageUrl() != null) {
        s3BucketService.deleteFile(extractKeyFromUrl(event.getThumbnailImageUrl()));
      }
      event.setThumbnailImageUrl(newThumbnailImageUrl != null ? getFullImageUrl(newThumbnailImageUrl) : null);
    }

    if (contentFiles != null && !contentFiles.isEmpty()) {
      List<String> newContentImageUrls = new ArrayList<>();
      for (MultipartFile file : contentFiles) {
        newContentImageUrls.add(s3BucketService.uploadFile(file));
      }
      if (event.getContentImageUrl() != null) {
        event.getContentImageUrl().forEach(url -> s3BucketService.deleteFile(extractKeyFromUrl(url)));
      }
      event.setContentImageUrl(newContentImageUrls.stream()
          .map(this::getFullImageUrl)
          .collect(Collectors.toList()));
    }

    eventMapper.updateEventFromDto(eventUpdateDto, event);
    Event updatedEvent = eventRepository.save(event);
    return eventMapper.eventToEventDetailDto(updatedEvent);
  }

  // 특정 이벤트를 삭제하는 메서드
  @Transactional
  public void deleteEvent(Long eventId) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("삭제할 이벤트를 찾을 수 없습니다: " + eventId));

    if (event.getThumbnailImageUrl() != null) {
      s3BucketService.deleteFile(extractKeyFromUrl(event.getThumbnailImageUrl()));
    }

    if (event.getContentImageUrl() != null) {
      for (String url : event.getContentImageUrl()) {
        s3BucketService.deleteFile(extractKeyFromUrl(url));
      }
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
