package com.gimzazin.tiketflow.reservation.service;

import com.gimzazin.tiketflow.event.entity.Seat;
import com.gimzazin.tiketflow.event.repository.SeatRepository;
import com.gimzazin.tiketflow.event.service.SeatService;
import com.gimzazin.tiketflow.exception.ResourceNotFoundException;
import com.gimzazin.tiketflow.reservation.dto.ReservationRequestDto;
import com.gimzazin.tiketflow.reservation.dto.ReservationResponseDto;
import com.gimzazin.tiketflow.reservation.entity.Reservation;
import com.gimzazin.tiketflow.reservation.entity.ReservationItem;
import com.gimzazin.tiketflow.reservation.repository.ReservationItemRepository;
import com.gimzazin.tiketflow.reservation.repository.ReservationRepository;
import com.gimzazin.tiketflow.users.entity.User;
import com.gimzazin.tiketflow.users.repository.UserRepository;
import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("reservationServiceRedis")
@RequiredArgsConstructor
public class ReservationRedisService implements ReservationService {

    @PersistenceContext
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final SeatService seatService;
    private final SeatRepository seatRepository;

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
            Long resultSeatId = seatService.reserveSeatRedis(seatId);
            Seat seat = seatRepository.findById(resultSeatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", resultSeatId));
            ReservationItem item = ReservationItem.builder()
                    .seat(seat)
                    .reservation(reservation)
                    .build();
            reservationItemRepository.save(item);
            reservation.addReservationItem(item);
        }

        reservationRepository.saveAndFlush(reservation);
        return ReservationResponseDto.fromEntity(reservation);
    }

    @Override
    public ReservationResponseDto cancelReservation(Long reservationId) {
        return null;
    }
}
