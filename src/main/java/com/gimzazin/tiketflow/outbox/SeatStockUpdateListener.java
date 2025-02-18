package com.gimzazin.tiketflow.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gimzazin.tiketflow.event.entity.Seat;
import com.gimzazin.tiketflow.event.repository.SeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SeatStockUpdateListener {

    private final SeatRepository seatRepository;
    private final ObjectMapper objectMapper;

    public SeatStockUpdateListener(SeatRepository seatRepository, ObjectMapper objectMapper) {
        this.seatRepository = seatRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleSeatStockUpdate(String messagePayload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messagePayload);
            if (jsonNode.has("seatId") && jsonNode.has("newStock")) {

                Long seatId = jsonNode.get("seatId").asLong();
                Long newStock = jsonNode.get("newStock").asLong();
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new RuntimeException("Seat not found: " + seatId));

                Long updatedQuantity = seat.getQuantity() - newStock;
                if (updatedQuantity < 0) {
                    throw new IllegalStateException("DB 재고 부족: seatId=" + seatId);
                }
                seat.setQuantity(updatedQuantity);
                seatRepository.save(seat);
                log.info("Seat stock updated: seatId={}, Stock={}", seatId, newStock);
            } else {
                log.info("Received non-seat stock update message: {}", messagePayload);
            }
        } catch (Exception e) {
            log.error("Failed to process seat stock update message: {}", messagePayload, e);
        }
    }
}
