package com.opencode.alumxbackend.users.service;

import com.opencode.alumxbackend.users.dto.UserProfileDTO;
import com.opencode.alumxbackend.users.dto.UserProfileUpdateRequestDto;
import com.opencode.alumxbackend.users.dto.UserRequest;
import com.opencode.alumxbackend.users.dto.UserResponseDto;
import com.opencode.alumxbackend.users.model.User;

import java.util.List;

public interface UserService {
    User createUser(UserRequest request);
    UserProfileDTO getUserProfile(Long id);
    List<UserResponseDto> getAllUsers();
    UserProfileDTO updateUserProfile(Long userId, UserProfileUpdateRequestDto request);
}
