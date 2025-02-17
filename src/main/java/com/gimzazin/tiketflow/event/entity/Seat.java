package com.gimzazin.tiketflow.event.entity;


import com.gimzazin.tiketflow.event.dto.SeatRequestDto;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "event_seats")
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_seat_id")
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "seat_number")
    private Long seatNumber;

    @Column(name = "seat_grade")
    private String seatGrade;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "reservable")
    private Boolean reservable = true;

    @Version
    @Column(name = "version")
    private Long version;

    void assignEvent(Event event) {
        this.event = event;
    }

    public void reserve() {
        if (Boolean.FALSE.equals(this.reservable) || this.quantity <= 0) {
            throw new IllegalStateException("예약이 불가능합니다.");
        }
        this.quantity = this.quantity - 1;
        if (this.quantity == 0) {
            this.reservable = false;
        }
        this.reservedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void unreserve() {
        this.quantity = this.quantity + 1;
        this.reservable = true;
        this.updatedAt = LocalDateTime.now();
    }


    public void update(SeatRequestDto seatRequestDto) {
        this.seatNumber = seatRequestDto.getSeatNumber();
        this.seatGrade = seatRequestDto.getSeatGrade();
        this.quantity = seatRequestDto.getQuantity();
        this.updatedAt = LocalDateTime.now();
    }
}