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


  // 모든 활성화된 이벤트를 조회하는 메서드 (페이지네이션 적용)
  @GetMapping
  public ResponseEntity<Page<EventDto>> getActiveEvents(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit) {

    Page<EventDto> events = eventService.getActiveEvents(page, limit);

    return ResponseEntity.ok(events);
  }

  // 특정 이벤트의 상세 정보를 조회하는 메서드
  @GetMapping("/{event_id}")
  public ResponseEntity<EventDetailDto> getEventDetail(@PathVariable("event_id") Long eventId) {
    EventDetailDto eventDetail = eventService.getEventDetail(eventId);
    return ResponseEntity.ok(eventDetail);
  }

}