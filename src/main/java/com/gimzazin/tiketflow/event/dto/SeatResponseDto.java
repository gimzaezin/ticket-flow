package com.gimzazin.tiketflow.event.dto;

import com.gimzazin.tiketflow.event.entity.Seat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatResponseDto {
    private Long seatId;
    private Long seatNumber;
    private String seatGrade;
    private Long quantity;
    private LocalDateTime reservedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SeatResponseDto fromEntity(Seat seat) {
        return SeatResponseDto.builder()
            .seatId(seat.getSeatId())
            .seatNumber(seat.getSeatNumber())
            .seatGrade(seat.getSeatGrade())
            .quantity(seat.getQuantity())
            .reservedAt(seat.getReservedAt())
            .createdAt(seat.getCreatedAt())
            .updatedAt(seat.getUpdatedAt())
            .build();
    }
}
