package com.gimzazin.tiketflow.reservation.service;

import com.gimzazin.tiketflow.reservation.dto.ReservationRequestDto;
import com.gimzazin.tiketflow.reservation.dto.ReservationResponseDto;
import org.springframework.transaction.annotation.Transactional;

public interface ReservationService {
    @Transactional
    ReservationResponseDto createReservation(ReservationRequestDto requestDto);

    @Transactional
    ReservationResponseDto cancelReservation(Long reservationId);
}
