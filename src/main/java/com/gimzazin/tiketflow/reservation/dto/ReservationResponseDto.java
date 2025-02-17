package com.gimzazin.tiketflow.reservation.dto;

import com.gimzazin.tiketflow.event.dto.SeatResponseDto;
import com.gimzazin.tiketflow.reservation.entity.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDto {
    private Long reservationId;
    private String reservationState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private List<SeatResponseDto> seats;

    public static ReservationResponseDto fromEntity(Reservation reservation) {
        return ReservationResponseDto.builder()
                .reservationId(reservation.getReservationId())
                .reservationState(reservation.getReservationState())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .description(reservation.getDescription())
                .seats(
                    reservation.getReservationItems().stream()
                            .map(item -> SeatResponseDto.fromEntity(item.getSeat()))
                            .toList()
                )
                .build();
    }
}
