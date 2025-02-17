package com.gimzazin.tiketflow.event.entity;

import com.gimzazin.tiketflow.event.dto.EventRequestDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "event_location")
    private String eventLocation;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    public void addSeat(Seat seat) {
        this.seats.add(seat);
        seat.assignEvent(this);
    }

    public void update(EventRequestDto eventRequestDto) {
        this.eventName = eventRequestDto.getEventName();
        this.eventDescription = eventRequestDto.getEventDescription();
        this.eventType = eventRequestDto.getEventType();
        this.eventLocation = eventRequestDto.getEventLocation();
        this.eventDate = eventRequestDto.getEventDate();
        this.updatedAt = LocalDateTime.now();
    }
}

