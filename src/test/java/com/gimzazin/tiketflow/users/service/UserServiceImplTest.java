package com.gimzazin.tiketflow.users.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gimzazin.tiketflow.exception.ResourceNotFoundException;
import com.gimzazin.tiketflow.users.dto.UserCreateRequestDto;
import com.gimzazin.tiketflow.users.dto.UserResponseDto;
import com.gimzazin.tiketflow.users.entity.User;
import com.gimzazin.tiketflow.users.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .userId(1L)
                .name("kim")
                .email("kim@example.com")
                .phone("010-1234-5678")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        user2 = User.builder()
                .userId(2L)
                .name("lee")
                .email("lee@example.com")
                .phone("010-5678-1234")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllUsers 모든 사용자 반환")
    void testGetAllUsers() {
        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponseDto> responseDtos = userService.getAllUsers();

        assertNotNull(responseDtos);
        assertEquals(2, responseDtos.size());
        assertEquals(user1.getUserId(), responseDtos.get(0).getUserId());
        assertEquals(user2.getUserId(), responseDtos.get(1).getUserId());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getUserById 존재하는 사용자 반환")
    void testGetUserById_Success() {
        Long userId = user1.getUserId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

        UserResponseDto responseDto = userService.getUserById(userId);

        assertNotNull(responseDto);
        assertEquals(user1.getUserId(), responseDto.getUserId());
        assertEquals(user1.getName(), responseDto.getName());
        assertEquals(user1.getEmail(), responseDto.getEmail());
        assertEquals(user1.getPhone(), responseDto.getPhone());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("getUserById 존재하지 않는 사용자 -> ResourceNotFoundException 발생")
    void testGetUserById_NotFound() {
        Long nonExistUserId = 100L;
        when(userRepository.findById(nonExistUserId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(nonExistUserId));
        assertTrue(exception.getMessage().contains("User"));
        verify(userRepository, times(1)).findById(nonExistUserId);
    }

    @Test
    @DisplayName("createUser 새로운 사용자 생성")
    void testCreateUser() {
        UserCreateRequestDto createRequest = UserCreateRequestDto.builder()
                .name("park")
                .email("pakr@example.com")
                .phone("010-1111-1111")
                .build();

        User userCreate = createRequest.toEntity();

        User savedUser = User.builder()
                .userId(3L)
                .name(userCreate.getName())
                .email(userCreate.getEmail())
                .phone(userCreate.getPhone())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto responseDto = userService.createUser(createRequest);

        assertNotNull(responseDto);
        assertEquals(savedUser.getUserId(), responseDto.getUserId());
        assertEquals(savedUser.getName(), responseDto.getName());
        assertEquals(savedUser.getEmail(), responseDto.getEmail());
        assertEquals(savedUser.getPhone(), responseDto.getPhone());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("deleteUser 사용자 삭제 성공")
    void testDeleteUser_Success() {
        Long userId = user1.getUserId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user1));

        assertDoesNotThrow(() -> userService.deleteUser(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user1);
    }

    @Test
    @DisplayName("deleteUser 존재하지 않는 사용자 삭제 -> ResourceNotFoundException 발생")
    void testDeleteUser_NotFound() {
        Long nonExistingUserId = 100L;
        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(nonExistingUserId));
        assertTrue(exception.getMessage().contains("User"));
        verify(userRepository, times(1)).findById(nonExistingUserId);
        verify(userRepository, never()).delete(any(User.class));
    }
}