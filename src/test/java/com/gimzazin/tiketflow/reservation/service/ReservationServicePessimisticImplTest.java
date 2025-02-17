package com.gimzazin.tiketflow.reservation.service;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

import com.gimzazin.tiketflow.event.entity.Seat;
import com.gimzazin.tiketflow.event.repository.SeatRepository;
import com.gimzazin.tiketflow.reservation.dto.ReservationRequestDto;
import com.gimzazin.tiketflow.reservation.repository.ReservationItemRepository;
import com.gimzazin.tiketflow.reservation.repository.ReservationRepository;
import com.gimzazin.tiketflow.users.entity.User;
import com.gimzazin.tiketflow.users.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ReservationServiceConcurrencyTest {

    @Autowired
    @Qualifier("reservationServiceOptimistic")
    private ReservationService optimisticService;

    @Autowired
    @Qualifier("reservationServicePessimistic")
    private ReservationService pessimisticService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationItemRepository reservationItemRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testSeatId;

    private Long testSeatId2;

    private Long userId;

    @BeforeEach
    @Transactional
    public void setup() {
        reservationItemRepository.deleteAll();
        reservationRepository.deleteAll();
        seatRepository.deleteAll();

        Seat seat = Seat.builder()
                .seatNumber(1L)
                .seatGrade("VIP")
                .quantity(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        seatRepository.save(seat);
        testSeatId = seat.getSeatId();


        Seat seat2 = Seat.builder()
                .seatNumber(2L)
                .seatGrade("VIP")
                .quantity(10L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        seatRepository.save(seat2);
        testSeatId2 = seat2.getSeatId();

        User user = User.builder()
                .name("name")
                .email("email")
                .phone("phone")
                .build();

        userRepository.save(user);
        userId = user.getUserId();

    }

    /**
     * 낙관적 락을 사용하는 서비스의 동시성 테스트 예시
     */
    @Test
    void testOptimisticLockConcurrency() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ReservationRequestDto requestDto = ReservationRequestDto.builder()
                            .description("Optimistic Test")
                            .seatIds(Collections.singletonList(testSeatId))
                            .userId(userId)
                            .build();
                    optimisticService.createReservation(requestDto);
                } catch (Exception e) {
                    // 예외는 로그 등으로 처리 (테스트에서는 무시)
                    System.err.println("OptimisticLock Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        Seat seat = seatRepository.findById(testSeatId)
                .orElseThrow(() -> new RuntimeException("Test seat not found"));
        assertThat(seat.getQuantity()).isZero();
    }

    /**
     * 비관적 락을 사용하는 서비스의 동시성 테스트 예시
     */
    @Test
    void testPessimisticLockConcurrency() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ReservationRequestDto requestDto = ReservationRequestDto.builder()
                            .description("Pessimistic Test")
                            .seatIds(Collections.singletonList(testSeatId))
                            .userId(userId)
                            .build();
                    pessimisticService.createReservation(requestDto);
                } catch (Exception e) {
                    System.err.println("PessimisticLock Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        Seat seat = seatRepository.findById(testSeatId)
                .orElseThrow(() -> new RuntimeException("Test seat not found"));
        assertThat(seat.getQuantity()).isZero();
    }

    /**
     * 하나의 좌석(quantity=10)에 대해 1000명의 사용자가 예약을 시도 (비관적 락)
     */
    @Test
    void testPessimisticLockConcurrencyLarge() throws InterruptedException {
        int userCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        CountDownLatch latch = new CountDownLatch(userCount);

        for (int i = 0; i < userCount; i++) {
            executor.submit(() -> {
                try {
                    ReservationRequestDto requestDto = ReservationRequestDto.builder()
                            .description("Pessimistic Test Large")
                            .seatIds(Collections.singletonList(testSeatId2))
                            .userId(userId)
                            .build();
                    pessimisticService.createReservation(requestDto);
                } catch (Exception e) {
                    System.err.println("PessimisticLock Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        Seat seat = seatRepository.findById(testSeatId2)
                .orElseThrow(() -> new RuntimeException("Test seat not found"));
        assertThat(seat.getQuantity()).isZero();

        long totalReservationItems = reservationItemRepository.count();
        assertThat(totalReservationItems).isEqualTo(10);
    }

}
