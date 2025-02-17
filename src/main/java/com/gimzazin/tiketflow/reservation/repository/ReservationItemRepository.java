package com.gimzazin.tiketflow.reservation.repository;

import com.gimzazin.tiketflow.reservation.entity.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {
}
