package com.gimzazin.tiketflow.users.service;


import com.gimzazin.tiketflow.exception.ResourceNotFoundException;
import com.gimzazin.tiketflow.users.dto.UserCreateRequestDto;
import com.gimzazin.tiketflow.users.dto.UserResponseDto;
import com.gimzazin.tiketflow.users.entity.User;
import com.gimzazin.tiketflow.users.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        return UserResponseDto.fromEntity(user);
    }

    @Transactional
    @Override
    public UserResponseDto createUser(UserCreateRequestDto userRequestDto) {
        User user = userRequestDto.toEntity();
        User savedUser = userRepository.save(user);
        return UserResponseDto.fromEntity(savedUser);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        userRepository.delete(user);
    }
}
