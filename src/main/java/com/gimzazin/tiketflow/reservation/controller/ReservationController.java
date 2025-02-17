
package com.gimzazin.tiketflow.reservation.controller;

import com.gimzazin.tiketflow.reservation.dto.ReservationRequestDto;
import com.gimzazin.tiketflow.reservation.dto.ReservationResponseDto;
import com.gimzazin.tiketflow.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/reservations")
public class ReservationController {

    private final ReservationService optimisticService;
    private final ReservationService pessimisticService;

    public ReservationController(
            @Qualifier("reservationServiceOptimistic") ReservationService optimisticService,
            @Qualifier("reservationServicePessimistic") ReservationService pessimisticService) {
        this.optimisticService = optimisticService;
        this.pessimisticService = pessimisticService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(
            @RequestBody ReservationRequestDto requestDto,
            @RequestParam(defaultValue = "optimistic") String lockType) {

        ReservationResponseDto responseDto;

        if ("pessimistic".equalsIgnoreCase(lockType)) {
            responseDto = pessimisticService.createReservation(requestDto);
        } else {
            responseDto = optimisticService.createReservation(requestDto);
        }
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationResponseDto> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam(defaultValue = "optimistic") String lockType) {

        ReservationResponseDto responseDto;
        if ("pessimistic".equalsIgnoreCase(lockType)) {
            responseDto = pessimisticService.cancelReservation(reservationId);
        } else {
            responseDto = optimisticService.cancelReservation(reservationId);
        }
        return ResponseEntity.ok(responseDto);
    }
}
