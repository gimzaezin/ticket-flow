package com.gimzazin.tiketflow.outbox.repository;

import com.gimzazin.tiketflow.outbox.entity.Outbox;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findAllByStatus(String status);
    List<Outbox> findAllByStatusNotAndCreatedAtBefore(String status, LocalDateTime createdAt);
}
