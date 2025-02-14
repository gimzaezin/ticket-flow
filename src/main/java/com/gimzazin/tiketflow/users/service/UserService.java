package com.gimzazin.tiketflow.users.service;

import com.gimzazin.tiketflow.users.dto.UserCreateRequestDto;
import com.gimzazin.tiketflow.users.dto.UserResponseDto;
import java.util.List;

public interface UserService {
    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long userId);


    UserResponseDto createUser(UserCreateRequestDto userRequestDto);

    void deleteUser(Long userId);
}
