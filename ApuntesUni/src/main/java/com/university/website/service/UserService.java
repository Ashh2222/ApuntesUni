package com.university.website.service;

import com.university.website.dto.PasswordChangeDto;
import com.university.website.dto.UserProfileDto;
import com.university.website.dto.UserRegistrationDto;
import com.university.website.model.Student;
import com.university.website.model.User;
import com.university.website.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    User registerStudent(UserRegistrationDto dto);

    User findByEmail(String email);

    User findById(Long id);

    Student findStudentById(Long id);

    void updateProfile(Long userId, UserProfileDto dto);

    void changePassword(Long userId, PasswordChangeDto dto);

    Page<User> findAllUsers(Pageable pageable);

    void toggleUserActive(Long userId);

    void updateUserRole(Long userId, UserRole role);

    String createPasswordResetToken(String email);

    void resetPasswordWithToken(String token, String newPassword);
}
