package com.gimzazin.tiketflow.event.service;

import com.gimzazin.tiketflow.event.entity.Seat;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface SeatService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void reserveSeat(Long seatId);


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Seat reserveSeatPessimistic(Long seatId);
}
