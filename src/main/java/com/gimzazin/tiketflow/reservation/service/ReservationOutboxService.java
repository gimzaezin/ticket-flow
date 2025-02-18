package com.gimzazin.tiketflow.reservation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimzazin.tiketflow.event.entity.Seat;
import com.gimzazin.tiketflow.event.repository.SeatRepository;
import com.gimzazin.tiketflow.event.service.SeatService;
import com.gimzazin.tiketflow.exception.ResourceNotFoundException;
import com.gimzazin.tiketflow.outbox.entity.Outbox;

import com.gimzazin.tiketflow.outbox.repository.OutboxRepository;
import com.gimzazin.tiketflow.reservation.dto.ReservationRequestDto;
import com.gimzazin.tiketflow.reservation.dto.ReservationResponseDto;
import com.gimzazin.tiketflow.reservation.entity.Reservation;
import com.gimzazin.tiketflow.reservation.entity.ReservationItem;
import com.gimzazin.tiketflow.reservation.repository.ReservationItemRepository;
import com.gimzazin.tiketflow.reservation.repository.ReservationRepository;
import com.gimzazin.tiketflow.users.entity.User;
import com.gimzazin.tiketflow.users.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("reservationServiceOutbox")
@RequiredArgsConstructor
public class ReservationOutboxService implements ReservationService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final SeatService seatService;
    private final SeatRepository seatRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public ReservationResponseDto createReservation(ReservationRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", requestDto.getUserId()));

        Reservation reservation = Reservation.builder()
                .reservationState("CONFIRMED")
                .user(user)
                .description(requestDto.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        reservationRepository.save(reservation);

        for (Long seatId : requestDto.getSeatIds()) {
            Long newStock = seatService.reserveSeatRedis(seatId);


            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId));

            ReservationItem item = ReservationItem.builder()
                    .seat(seat)
                    .reservation(reservation)
                    .build();
            reservationItemRepository.save(item);
            reservation.addReservationItem(item);

            Outbox seatOutboxMessage = Outbox.builder()
                    .aggregateType("Seat")
                    .aggregateId(seatId)
                    .eventType("SeatStockUpdated")
                    .payload(convertSeatStockToJson(seat, newStock))
                    .createdAt(LocalDateTime.now())
                    .status("PENDING")
                    .retryCount(0)
                    .build();
            outboxRepository.save(seatOutboxMessage);
        }

        reservationRepository.saveAndFlush(reservation);

        return ReservationResponseDto.fromEntity(reservation);
    }


    private String convertSeatStockToJson(Seat seat, Long newStock) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("seatId", seat.getSeatId());
            payload.put("newStock", newStock);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert seat stock update to JSON", e);
        }
    }

    @Override
    @Transactional
    public ReservationResponseDto cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "reservationId", reservationId));

        if ("CANCELED".equalsIgnoreCase(reservation.getReservationState())) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        for (ReservationItem item : reservation.getReservationItems()) {
            Seat seat = seatRepository.findByIdWithLock(item.getSeat().getSeatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", item.getSeat().getSeatId()));

            seat.unreserve();
            seatRepository.save(seat);
        }

        reservation.cancel();
        reservationRepository.save(reservation);
        return ReservationResponseDto.fromEntity(reservation);
    }
}

