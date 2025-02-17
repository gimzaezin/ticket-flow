package com.gimzazin.tiketflow.event.dto;

import com.gimzazin.tiketflow.event.entity.Event;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {
    private Long eventId;
    private String eventName;
    private String eventDescription;
    private String eventType;
    private String eventLocation;
    private LocalDateTime eventDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SeatResponseDto> seats;

    public static EventResponseDto fromEntity(Event event) {
        List<SeatResponseDto> seatDtos = event.getSeats().stream()
                .map(SeatResponseDto::fromEntity)
                .toList();

        return EventResponseDto.builder()
            .eventId(event.getEventId())
            .eventName(event.getEventName())
            .eventDescription(event.getEventDescription())
            .eventType(event.getEventType())
            .eventLocation(event.getEventLocation())
            .eventDate(event.getEventDate())
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .seats(seatDtos)
            .build();
    }
}
