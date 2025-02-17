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
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("reservationServiceOptimistic")
@RequiredArgsConstructor
public class ReservationServiceOptimisticImpl implements ReservationService {

    @PersistenceContext
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final SeatService seatService;
    private final SeatRepository seatRepository;

    private static final int MAX_RETRY = 3;

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
            boolean success = false;
            int attempt = 0;
            while (!success && attempt < MAX_RETRY) {
                attempt++;
                try {
                    seatService.reserveSeat(seatId);

                    Seat updatedSeat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));

                    ReservationItem item = ReservationItem.builder()
                            .seat(updatedSeat)
                            .reservation(reservation)
                            .build();
                    reservationItemRepository.save(item);
                    reservation.addReservationItem(item);

                    success = true;
                } catch (OptimisticLockException | org.hibernate.exception.LockAcquisitionException ex) {
                    if (attempt >= MAX_RETRY) {
                        throw ex;
                    }
                    try {
                        Thread.sleep(100L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                } catch (IllegalStateException ise) {
                    if ("예약이 불가능합니다.".equals(ise.getMessage())) {
                        log.error("IllegalStateException: {}", ise.getMessage());
                        throw ise;
                    } else {
                        throw ise;
                    }
                }
            }
        }

        reservationRepository.saveAndFlush(reservation);
        return ReservationResponseDto.fromEntity(reservation);
    }

    @Transactional
    @Override
    public ReservationResponseDto cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "reservationId", reservationId));

        if ("CANCELED".equalsIgnoreCase(reservation.getReservationState())) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        for (ReservationItem item : reservation.getReservationItems()) {
            boolean success = false;
            int attempt = 0;
            Long seatId = item.getSeat().getSeatId();

            while (!success) {
                attempt++;
                try {
                    Seat seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));
                    // 수량 복원 (내부 로직 구현)
                    seat.unreserve();
                    seatRepository.save(seat);

                    success = true;
                } catch (OptimisticLockException ole) {
                    if (attempt >= MAX_RETRY) {
                        throw ole;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                    }
                }
            }
        }

        reservation.cancel();
        reservationRepository.save(reservation);
        return ReservationResponseDto.fromEntity(reservation);
    }
}
