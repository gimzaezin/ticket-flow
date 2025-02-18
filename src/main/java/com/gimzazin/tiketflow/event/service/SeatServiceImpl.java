package com.gimzazin.tiketflow.event.service;

import com.gimzazin.tiketflow.event.entity.Seat;
import com.gimzazin.tiketflow.event.repository.SeatRepository;
import com.gimzazin.tiketflow.exception.ResourceNotFoundException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String SEAT_STOCK_KEY_PREFIX = "seat_stock:";

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveSeat(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));
        seat.reserve();
        seatRepository.saveAndFlush(seat);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Seat reserveSeatPessimistic(Long seatId) {
        int updatedCount = seatRepository.updateSeatForReservation(seatId);
        if (updatedCount == 0) {
            throw new IllegalStateException("예약이 불가능합니다.");
        }
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long reserveSeatRedis(Long seatId) {
        String key = SEAT_STOCK_KEY_PREFIX + seatId;

        String script =
                "local current = redis.call('get', KEYS[1]) " +
                        "if not current then " +
                        "  redis.call('set', KEYS[1], ARGV[1]) " +
                        "  current = ARGV[1] " +
                        "end " +
                        "if tonumber(current) <= 0 then " +
                        "  return -1 " +
                        "end " +
                        "local newStock = redis.call('decr', KEYS[1]) " +
                        "return newStock";

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "seatId", seatId));
        String initialStock = String.valueOf(seat.getQuantity());

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        Long newStock = redisTemplate.execute(redisScript, Collections.singletonList(key), initialStock);

        if (newStock == null || newStock < 0) {
            throw new IllegalStateException("예약이 불가능합니다.");
        }
        return 1L;
    }
}
