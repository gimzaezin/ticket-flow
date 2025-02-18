
package com.gimzazin.tiketflow.reservation.controller;

import com.gimzazin.tiketflow.reservation.dto.ReservationRequestDto;
import com.gimzazin.tiketflow.reservation.dto.ReservationResponseDto;
import com.gimzazin.tiketflow.reservation.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/v1/reservations")
public class ReservationController {

    private final ReservationService optimisticService;
    private final ReservationService pessimisticService;
    private final ReservationService redisService;
    private final ReservationService outboxService;

    public ReservationController(
            @Qualifier("reservationServiceOptimistic") ReservationService optimisticService,
            @Qualifier("reservationServicePessimistic") ReservationService pessimisticService,
            @Qualifier("reservationServiceRedis") ReservationService redisService,
            @Qualifier("reservationServiceOutbox") ReservationService outboxService) {
        this.optimisticService = optimisticService;
        this.pessimisticService = pessimisticService;
        this.redisService = redisService;
        this.outboxService = outboxService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(
            @RequestBody ReservationRequestDto requestDto,
            @RequestParam String lockType) {

        ReservationResponseDto responseDto;
        log.info("lockType: " + lockType);
        if ("pessimistic".equalsIgnoreCase(lockType)) {
            responseDto = pessimisticService.createReservation(requestDto);
        } else if ("optimistic".equalsIgnoreCase(lockType)){
            responseDto = optimisticService.createReservation(requestDto);
        } else if ("redis".equalsIgnoreCase(lockType)) {
            responseDto = redisService.createReservation(requestDto);
        } else if ("outbox".equalsIgnoreCase(lockType)) {
            responseDto = outboxService.createReservation(requestDto);
        }
        else {
            return ResponseEntity.ok(null);
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
