package com.spring_boots.spring_boots.category.controller;


import com.spring_boots.spring_boots.category.dto.event.EventAdminDto;
import com.spring_boots.spring_boots.category.dto.event.EventDetailDto;
import com.spring_boots.spring_boots.category.dto.event.EventRequestDto;
import com.spring_boots.spring_boots.category.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventApiController {

  private final EventService eventService;


  // 새로운 이벤트를 생성하는 메서드
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<EventDetailDto> createEvent(
      @Valid @RequestPart("event") EventRequestDto eventRequestDto,
      @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
      @RequestPart(value = "contentFile", required = false) MultipartFile contentFile) throws IOException {
    EventDetailDto createdEvent = eventService.createEvent(eventRequestDto, thumbnailFile, contentFile);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
  }

  // 특정 이벤트를 수정하는 메서드
  @PreAuthorize("hasRole('ADMIN')")
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
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{event_id}")
  public ResponseEntity<Void> deleteEvent(@PathVariable("event_id") Long eventId) {
    eventService.deleteEvent(eventId);
    return ResponseEntity.noContent().build();
  }

  // 관리자 - 이벤트 전체 목록 조회 (페이지네이션)
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<EventAdminDto>> getAdminEvent (
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "limit", defaultValue = "10") int limit) {
    Page<EventAdminDto> adminEvents = eventService.getAdminEvents(page, limit);
    return ResponseEntity.ok(adminEvents);
  }

  // 관리자 - 개별 카테고리 조회
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{event_id}")
  public ResponseEntity<EventAdminDto> getAdminEvent(@PathVariable("event_id") Long eventId){
    EventAdminDto eventDto = eventService.getAdminEvent(eventId);
    return ResponseEntity.ok(eventDto);
  }


}
