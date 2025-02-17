package com.gimzazin.tiketflow.event.repository;

import com.gimzazin.tiketflow.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
