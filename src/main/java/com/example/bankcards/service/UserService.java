package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.UserRole;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    List<UserDto> getUsersByRole(UserRole role);
    UserDto getUserById(Long id);
    UserDto createUser(User user);
    UserDto updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    void deactivateUser(Long id);
}
