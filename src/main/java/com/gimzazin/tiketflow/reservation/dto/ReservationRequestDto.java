package com.gimzazin.tiketflow.reservation.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDto {
    private Long userId;
    private String description;
    private List<Long> seatIds;
}
