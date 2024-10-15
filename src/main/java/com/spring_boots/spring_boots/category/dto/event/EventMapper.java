package com.spring_boots.spring_boots.category.dto.event;


import com.spring_boots.spring_boots.category.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface EventMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "isActive", constant = "true")
  Event eventRequestDtoToEvent(EventRequestDto eventRequestDto);

  EventDetailDto eventToEventDetailDto(Event event);

  EventDto eventToEventDto(Event event);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "isActive", ignore = true)
  void updateEventFromDto(EventRequestDto eventRequestDto, @MappingTarget Event event);

  // event -> eventAdminDto
  EventAdminDto eventToEventAdminDto(Event event);
}
