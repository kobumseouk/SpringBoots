package com.spring_boots.spring_boots.category.dto.event;

import com.spring_boots.spring_boots.category.entity.Category;
import com.spring_boots.spring_boots.category.entity.Event;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-08T13:31:43+0900",
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

        EventDetailDto.EventDetailDtoBuilder eventDetailDto = EventDetailDto.builder();

        eventDetailDto.categoryName( eventCategoryCategoryName( event ) );
        eventDetailDto.id( event.getId() );
        eventDetailDto.eventTitle( event.getEventTitle() );
        eventDetailDto.eventContent( event.getEventContent() );
        eventDetailDto.thumbnailImageUrl( event.getThumbnailImageUrl() );
        eventDetailDto.contentImageUrl( event.getContentImageUrl() );
        eventDetailDto.startDate( event.getStartDate() );
        eventDetailDto.endDate( event.getEndDate() );
        eventDetailDto.isActive( event.getIsActive() );

        return eventDetailDto.build();
    }

    @Override
    public EventDto eventToEventDto(Event event) {
        if ( event == null ) {
            return null;
        }

        EventDto.EventDtoBuilder eventDto = EventDto.builder();

        eventDto.id( event.getId() );
        eventDto.eventTitle( event.getEventTitle() );
        eventDto.thumbnailImageUrl( event.getThumbnailImageUrl() );
        eventDto.startDate( event.getStartDate() );
        eventDto.endDate( event.getEndDate() );

        return eventDto.build();
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
