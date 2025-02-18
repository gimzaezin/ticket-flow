package com.gimzazin.tiketflow.outbox;

import com.gimzazin.tiketflow.outbox.entity.Outbox;
import com.gimzazin.tiketflow.outbox.repository.OutboxRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxMessagePublisher {

    private final OutboxRepository outboxRepository;

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;



    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxMessages() {
        List<Outbox> pendingMessages = outboxRepository.findAllByStatus("PENDING");
        for (Outbox message : pendingMessages) {
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, message.getPayload());
                message.setStatus("SEND_SUCCESS");
                outboxRepository.save(message);
                //log.info("Message sent successfully: id={}", message.getOutboxId());
            } catch (Exception e) {
                log.error("Failed to send message id {}: {}", message.getOutboxId(), e.getMessage(), e);
                message.setRetryCount(message.getRetryCount() + 1);
                outboxRepository.save(message);
            }
        }
    }

    // 재발행 처리: 생성 시각이 10분 이상 경과하고 상태가 "SEND_SUCCESS"가 아닌 메시지 재전송 (1분마다 실행)
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void republishOldOutboxMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        List<Outbox> oldMessages = outboxRepository.findAllByStatusNotAndCreatedAtBefore("SEND_SUCCESS", threshold);
        for (Outbox message : oldMessages) {
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, message.getPayload());
                message.setStatus("SEND_SUCCESS");
                outboxRepository.save(message);
                //log.info("Re-published old message successfully: id={}", message.getOutboxId());
            } catch (Exception e) {
                log.error("Failed to republish message id {}: {}", message.getOutboxId(), e.getMessage(), e);
                message.setRetryCount(message.getRetryCount() + 1);
                outboxRepository.save(message);
            }
        }
    }
}
