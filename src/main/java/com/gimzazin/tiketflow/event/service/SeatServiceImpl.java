package com.gimzazin.tiketflow.event.service;

import com.gimzazin.tiketflow.event.entity.Seat;
import com.gimzazin.tiketflow.event.repository.SeatRepository;
import com.gimzazin.tiketflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));
        seat.reserve();
        seatRepository.saveAndFlush(seat);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Seat reserveSeatPessimistic(Long seatId) {
        int updatedCount = seatRepository.updateSeatForReservation(seatId);
        if (updatedCount == 0) {
            throw new IllegalStateException("예약이 불가능합니다.");
        }
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));
    }

}
