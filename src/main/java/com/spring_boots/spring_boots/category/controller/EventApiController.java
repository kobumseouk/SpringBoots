package com.spring_boots.spring_boots.category.controller;

import com.spring_boots.spring_boots.category.dto.event.EventDetailDto;
import com.spring_boots.spring_boots.category.dto.event.EventDto;
import com.spring_boots.spring_boots.category.dto.event.EventRequestDto;
import com.spring_boots.spring_boots.category.service.EventService;
import com.spring_boots.spring_boots.common.config.error.BadRequestException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventApiController {

  private final EventService eventService;


  // 새로운 이벤트를 생성하는 엔드포인트
  @PostMapping("/admin/events")
  public ResponseEntity<EventDetailDto> createEvent(@Valid @RequestBody EventRequestDto eventRequestDto) {
    EventDetailDto createdEvent = eventService.createEvent(eventRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
  }

  // 모든 활성화된 이벤트를 조회하는 엔드포인트
  @GetMapping("/events")
  public ResponseEntity<List<EventDto>> getAllEvents() {
    List<EventDto> events = eventService.getAllActiveEvents();
    return ResponseEntity.ok(events);
  }

  // 특정 이벤트의 상세 정보를 조회하는 엔드포인트
  @GetMapping("/events/{event_id}")
  public ResponseEntity<EventDetailDto> getEventDetail(@PathVariable("event_id") Long eventId) {
    EventDetailDto eventDetail = eventService.getEventDetail(eventId);
    return ResponseEntity.ok(eventDetail);
  }

  // 특정 이벤트를 수정하는 엔드포인트
  @PutMapping("/admin/events/{event_id}")
  public ResponseEntity<EventDetailDto> updateEvent(@PathVariable("event_id") Long eventId, @Valid @RequestBody EventRequestDto eventUpdateDto) {
    EventDetailDto updatedEvent = eventService.updateEvent(eventId, eventUpdateDto);
    return ResponseEntity.ok(updatedEvent);
  }

  // 특정 이벤트를 삭제하는 엔드포인트
  @DeleteMapping("/admin/events/{event_id}")
  public ResponseEntity<Void> deleteEvent(@PathVariable("event_id") Long eventId) {
    eventService.deleteEvent(eventId);
    return ResponseEntity.noContent().build();
  }


}