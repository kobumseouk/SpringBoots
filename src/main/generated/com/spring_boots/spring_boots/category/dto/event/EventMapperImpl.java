package com.spring_boots.spring_boots.category.dto.event;

import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.entity.Event;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-04T14:56:55+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.9 (GraalVM Community)"
)
@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public Event eventRequestDtoToEvent(EventRequestDto eventRequestDto) {
        if ( eventRequestDto == null ) {
            return null;
        }

        Event.EventBuilder event = Event.builder();

        event.eventTitle( eventRequestDto.getEventTitle() );
        event.eventContent( eventRequestDto.getEventContent() );
        event.thumbnailImageUrl( eventRequestDto.getThumbnailImageUrl() );
        event.contentImageUrl( eventRequestDto.getContentImageUrl() );
        event.startDate( eventRequestDto.getStartDate() );
        event.endDate( eventRequestDto.getEndDate() );

        event.isActive( true );

        return event.build();
    }

    @Override
    public EventDetailDto eventToEventDetailDto(Event event) {
        if ( event == null ) {
            return null;
        }

        EventDetailDto eventDetailDto = new EventDetailDto();

        eventDetailDto.setCategoryName( eventCategoryCategoryName( event ) );
        eventDetailDto.setId( event.getId() );
        eventDetailDto.setEventTitle( event.getEventTitle() );
        eventDetailDto.setEventContent( event.getEventContent() );
        eventDetailDto.setThumbnailImageUrl( event.getThumbnailImageUrl() );
        eventDetailDto.setContentImageUrl( event.getContentImageUrl() );
        eventDetailDto.setStartDate( event.getStartDate() );
        eventDetailDto.setEndDate( event.getEndDate() );
        eventDetailDto.setIsActive( event.getIsActive() );

        return eventDetailDto;
    }

    @Override
    public EventDto eventToEventDto(Event event) {
        if ( event == null ) {
            return null;
        }

        EventDto eventDto = new EventDto();

        eventDto.setId( event.getId() );
        eventDto.setEventTitle( event.getEventTitle() );
        eventDto.setThumbnailImageUrl( event.getThumbnailImageUrl() );
        eventDto.setStartDate( event.getStartDate() );
        eventDto.setEndDate( event.getEndDate() );

        return eventDto;
    }

    @Override
    public void updateEventFromDto(EventRequestDto eventRequestDto, Event event) {
        if ( eventRequestDto == null ) {
            return;
        }

        event.setEndDate( eventRequestDto.getEndDate() );
        event.setEventTitle( eventRequestDto.getEventTitle() );
        event.setEventContent( eventRequestDto.getEventContent() );
        event.setThumbnailImageUrl( eventRequestDto.getThumbnailImageUrl() );
        event.setContentImageUrl( eventRequestDto.getContentImageUrl() );
        event.setStartDate( eventRequestDto.getStartDate() );
    }

    private String eventCategoryCategoryName(Event event) {
        if ( event == null ) {
            return null;
        }
        Category category = event.getCategory();
        if ( category == null ) {
            return null;
        }
        String categoryName = category.getCategoryName();
        if ( categoryName == null ) {
            return null;
        }
        return categoryName;
    }
}
