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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final CategoryRepository categoryRepository;
  private final EventMapper eventMapper;


  // 새로운 이벤트를 생성하는 메서드
  @Transactional
  public EventDetailDto createEvent(EventRequestDto eventRequestDto) {
    Event event = eventMapper.eventRequestDtoToEvent(eventRequestDto);
    // 카테고리 id가 있는 경우 id를 찾아 이벤트에 설정, 없으면 ResourceNotFoundException 발생
    if (eventRequestDto.getCategoryId() != null) {
        Category category = categoryRepository.findById(eventRequestDto.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + eventRequestDto.getCategoryId()));
        event.setCategory(category);
    }
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
  public EventDetailDto updateEvent(Long eventId, EventRequestDto eventUpdateDto) {
    // 이벤트를 찾아 수정, 없으면 ResourceNotFoundException 발생
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new ResourceNotFoundException("업데이트할 이벤트를 찾을 수 없습니다: " + eventId));
    eventMapper.updateEventFromDto(eventUpdateDto, event);
    // 카테고리 ID가 제공된 경우 카테고리 업데이트
    if (eventUpdateDto.getCategoryId() != null) {
      Category category = categoryRepository.findById(eventUpdateDto.getCategoryId())
          .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + eventUpdateDto.getCategoryId()));
      event.setCategory(category);
    } else {
      event.removeCategory();
    }

    Event updatedEvent = eventRepository.save(event);
    return eventMapper.eventToEventDetailDto(updatedEvent);
  }

  // 특정 이벤트를 삭제하는 메서드
  @Transactional
  public void deleteEvent(Long eventId) {
    // 이벤트가 존재하는지 확인 후 삭제, 없으면 ResourceNotFoundException 발생
    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("삭제할 이벤트를 찾을 수 없습니다: " + eventId));
    eventRepository.deleteById(eventId);
  }

}
