package com.gimzazin.tiketflow.reservation.repository;

import com.gimzazin.tiketflow.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
