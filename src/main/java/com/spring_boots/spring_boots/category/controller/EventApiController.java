package com.spring_boots.spring_boots.category.controller;

import com.spring_boots.spring_boots.category.dto.event.EventDetailDto;
import com.spring_boots.spring_boots.category.dto.event.EventDto;
import com.spring_boots.spring_boots.category.dto.event.EventRequestDto;
import com.spring_boots.spring_boots.category.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Validated
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventApiController {

  private final EventService eventService;


  // 새로운 이벤트를 생성하는 메서드
  @PostMapping
  public ResponseEntity<EventDetailDto> createEvent(
      @Valid @RequestPart("event") EventRequestDto eventRequestDto,
      @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
      @RequestPart(value = "contentFile", required = false) MultipartFile contentFile) throws IOException {
    EventDetailDto createdEvent = eventService.createEvent(eventRequestDto, thumbnailFile, contentFile);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
  }

  // 모든 활성화된 이벤트를 조회하는 메서드 (페이지네이션 적용)
  @GetMapping
  public ResponseEntity<Page<EventDto>> getActiveEvents(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
    Page<EventDto> events = eventService.getActiveEvents(pageable);

    return ResponseEntity.ok(events);
  }

  // 특정 이벤트의 상세 정보를 조회하는 메서드
  @GetMapping("/{event_id}")
  public ResponseEntity<EventDetailDto> getEventDetail(@PathVariable("event_id") Long eventId) {
    EventDetailDto eventDetail = eventService.getEventDetail(eventId);
    return ResponseEntity.ok(eventDetail);
  }

  // 특정 이벤트를 수정하는 메서드
  @PutMapping("/{event_id}")
  public ResponseEntity<EventDetailDto> updateEvent(
      @PathVariable("event_id") Long eventId,
      @Valid @RequestPart("event") EventRequestDto eventUpdateDto,
      @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
      @RequestPart(value = "contentFile", required = false) MultipartFile contentFile) throws IOException {
    EventDetailDto updatedEvent = eventService.updateEvent(eventId, eventUpdateDto, thumbnailFile, contentFile);
    return ResponseEntity.ok(updatedEvent);
  }

  // 특정 이벤트를 삭제하는 메서드
  @DeleteMapping("/{event_id}")
  public ResponseEntity<Void> deleteEvent(@PathVariable("event_id") Long eventId) {
    eventService.deleteEvent(eventId);
    return ResponseEntity.noContent().build();
  }


}