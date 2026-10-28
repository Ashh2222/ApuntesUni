package com.university.website.service;

import com.university.website.dto.PasswordChangeDto;
import com.university.website.dto.UserRegistrationDto;
import com.university.website.exception.BadRequestException;
import com.university.website.model.Student;
import com.university.website.model.User;
import com.university.website.model.enums.UserRole;
import com.university.website.repository.*;
import com.university.website.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AcademicProgramRepository programRepository;

    @Mock
    private StudentProgramRepository studentProgramRepository;

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @Mock
    private RecoveryTokenRepository recoveryTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setFullName("Juan Perez");
        registrationDto.setEmail("jperez@uni.edu.pe");
        registrationDto.setPassword("Password123!");
        registrationDto.setConfirmPassword("Password123!");
    }

    @Test
    @DisplayName("Debe registrar un nuevo estudiante correctamente con contraseña cifrada")
    void testRegisterStudentSuccess() {
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed_password");
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student s = invocation.getArgument(0);
            s.setId(10L);
            return s;
        });

        User result = userService.registerStudent(registrationDto);

        assertNotNull(result);
        assertEquals("Juan Perez", result.getFullName());
        assertEquals("jperez@uni.edu.pe", result.getEmail());
        assertEquals(UserRole.STUDENT, result.getUserRole());
        verify(passwordEncoder).encode("Password123!");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el correo ya está registrado")
    void testRegisterStudentDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("jperez@uni.edu.pe")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.registerStudent(registrationDto));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Debe cambiar la contraseña si la actual es correcta")
    void testChangePasswordSuccess() {
        Student student = new Student();
        student.setId(1L);
        student.setPasswordHash("current_hash");

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.matches("OldPassword123!", "current_hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("new_hash");

        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setCurrentPassword("OldPassword123!");
        dto.setNewPassword("NewPassword123!");
        dto.setConfirmPassword("NewPassword123!");

        userService.changePassword(1L, dto);

        assertEquals("new_hash", student.getPasswordHash());
        verify(userRepository).save(student);
    }
}
