package com.gimzazin.tiketflow.event.repository;

import com.gimzazin.tiketflow.event.entity.Seat;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.seatId = :seatId")
    Optional<Seat> findByIdWithLock(@Param("seatId") Long seatId);


    @Modifying
    @Query("update Seat s " +
            "set s.quantity = s.quantity - 1, " +
            "    s.reservable = case when s.quantity - 1 = 0 then false else s.reservable end, " +
            "    s.reservedAt = CURRENT_TIMESTAMP, " +
            "    s.updatedAt = CURRENT_TIMESTAMP " +
            "where s.seatId = :seatId and s.quantity > 0")
    int updateSeatForReservation(@Param("seatId") Long seatId);
}
