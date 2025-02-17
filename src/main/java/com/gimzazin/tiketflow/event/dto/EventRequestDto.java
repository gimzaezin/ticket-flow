package com.gimzazin.tiketflow.event.dto;

import com.gimzazin.tiketflow.event.entity.Event;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto {
    private String eventName;
    private String eventDescription;
    private String eventType;
    private String eventLocation;
    private LocalDateTime eventDate;


    public Event toEntity() {
        return Event.builder()
                .eventName(eventName)
                .eventDescription(eventDescription)
                .eventType(eventType)
                .eventLocation(eventLocation)
                .eventDate(eventDate)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
