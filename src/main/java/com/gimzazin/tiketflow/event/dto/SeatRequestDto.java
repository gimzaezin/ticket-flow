package com.gimzazin.tiketflow.event.dto;

import com.gimzazin.tiketflow.event.entity.Seat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequestDto {
    private Long seatNumber;
    private String seatGrade;
    private Long quantity;


    public Seat toEntity() {
        return Seat.builder()
                .seatNumber(seatNumber)
                .seatGrade(seatGrade)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
