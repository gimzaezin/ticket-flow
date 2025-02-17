package com.gimzazin.tiketflow.event.service;

import com.gimzazin.tiketflow.event.dto.EventRequestDto;
import com.gimzazin.tiketflow.event.dto.EventResponseDto;
import com.gimzazin.tiketflow.event.dto.SeatRequestDto;
import com.gimzazin.tiketflow.event.dto.SeatResponseDto;
import com.gimzazin.tiketflow.event.entity.Event;
import com.gimzazin.tiketflow.event.entity.Seat;
import com.gimzazin.tiketflow.event.repository.EventRepository;
import com.gimzazin.tiketflow.event.repository.SeatRepository;
import com.gimzazin.tiketflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public EventResponseDto createEvent(EventRequestDto eventRequestDto) {
        Event event = eventRequestDto.toEntity();
        Event savedEvent = eventRepository.save(event);
        return EventResponseDto.fromEntity(savedEvent);
    }

    @Transactional
    public EventResponseDto addSeatToEvent(Long eventId, SeatRequestDto seatRequestDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "eventId", eventId));

        Seat seat = seatRequestDto.toEntity();
        event.addSeat(seat);

        Event updatedEvent = eventRepository.save(event);
        return EventResponseDto.fromEntity(updatedEvent);
    }

    @Transactional
    public SeatResponseDto reserveSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));
        seat.reserve();
        Seat updatedSeat = seatRepository.save(seat);
        return SeatResponseDto.fromEntity(updatedSeat);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "eventId", eventId));
        eventRepository.delete(event);
    }

    @Transactional
    public void deleteSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));
        seatRepository.delete(seat);
    }

    @Transactional
    public EventResponseDto updateEvent(Long eventId, EventRequestDto eventRequestDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "eventId", eventId));

        event.update(eventRequestDto);  // Event 엔티티에 update 메서드 필요
        Event updatedEvent = eventRepository.save(event);
        return EventResponseDto.fromEntity(updatedEvent);
    }

    @Transactional
    public SeatResponseDto updateSeat(Long seatId, SeatRequestDto seatRequestDto) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));

        seat.update(seatRequestDto);  // Seat 엔티티에 update 메서드 필요
        Seat updatedSeat = seatRepository.save(seat);
        return SeatResponseDto.fromEntity(updatedSeat);
    }
}
