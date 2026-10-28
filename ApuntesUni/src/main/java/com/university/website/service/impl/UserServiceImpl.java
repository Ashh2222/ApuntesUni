package com.university.website.service.impl;

import com.university.website.dto.PasswordChangeDto;
import com.university.website.dto.UserProfileDto;
import com.university.website.dto.UserRegistrationDto;
import com.university.website.exception.BadRequestException;
import com.university.website.exception.ResourceNotFoundException;
import com.university.website.model.*;
import com.university.website.model.enums.ProfileVisibility;
import com.university.website.model.enums.UserRole;
import com.university.website.repository.*;
import com.university.website.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AcademicProgramRepository academicProgramRepository;
    private final StudentProgramRepository studentProgramRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final RecoveryTokenRepository recoveryTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            StudentRepository studentRepository,
            AcademicProgramRepository academicProgramRepository,
            StudentProgramRepository studentProgramRepository,
            NotificationSettingsRepository notificationSettingsRepository,
            RecoveryTokenRepository recoveryTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.academicProgramRepository = academicProgramRepository;
        this.studentProgramRepository = studentProgramRepository;
        this.notificationSettingsRepository = notificationSettingsRepository;
        this.recoveryTokenRepository = recoveryTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerStudent(UserRegistrationDto dto) {
        if (!dto.isPasswordMatching()) {
            throw new BadRequestException("Las contraseñas no coinciden.");
        }

        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new BadRequestException("Ya existe una cuenta registrada con el correo: " + dto.getEmail());
        }

        Student student = new Student();
        student.setFullName(dto.getFullName().trim());
        student.setEmail(dto.getEmail().trim().toLowerCase());
        student.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        student.setUserRole(UserRole.STUDENT);
        student.setProfileVisibility(ProfileVisibility.PUBLIC);
        student.setActive(true);
        student.setJoinDate(LocalDateTime.now());
        student.setStudentCode(dto.getStudentCode() != null && !dto.getStudentCode().isBlank() ? dto.getStudentCode().trim() : null);
        student.setEntryYear(dto.getEntryYear() != null ? dto.getEntryYear() : LocalDateTime.now().getYear());
        student.setReputationPoints(0);
        student.setVerifiedStudent(false);

        Student savedStudent = studentRepository.save(student);

        // Crear configuración de notificaciones por defecto
        NotificationSettings settings = new NotificationSettings(savedStudent);
        notificationSettingsRepository.save(settings);

        // Asignar carrera si se especificó
        if (dto.getAcademicProgramId() != null) {
            academicProgramRepository.findById(dto.getAcademicProgramId()).ifPresent(program -> {
                StudentProgram sp = new StudentProgram(savedStudent, program, true, 1);
                studentProgramRepository.save(sp);
            });
        }

        log.info("Nuevo estudiante registrado con éxito: {}", savedStudent.getEmail());
        return savedStudent;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con ID: " + id));
    }

    @Override
    public void updateProfile(Long userId, UserProfileDto dto) {
        User user = findById(userId);
        user.updateProfile(dto.getFullName(), dto.getBio(), dto.getProfileVisibility());

        if (user instanceof Student student) {
            student.setInstitutionalEmail(dto.getInstitutionalEmail());
            student.setAcademicInterests(dto.getAcademicInterests());
            studentRepository.save(student);
        } else {
            userRepository.save(user);
        }
        log.info("Perfil actualizado para el usuario: {}", user.getEmail());
    }

    @Override
    public void changePassword(Long userId, PasswordChangeDto dto) {
        if (!dto.isPasswordMatching()) {
            throw new BadRequestException("La nueva contraseña y su confirmación no coinciden.");
        }

        User user = findById(userId);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual es incorrecta.");
        }

        user.changePassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        log.info("Contraseña actualizada exitosamente para el usuario: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public void toggleUserActive(Long userId) {
        User user = findById(userId);
        if (user.isActive()) {
            user.deactivate();
        } else {
            user.activate();
        }
        userRepository.save(user);
        log.info("Estado de usuario {} cambiado a active={}", user.getEmail(), user.isActive());
    }

    @Override
    public void updateUserRole(Long userId, UserRole role) {
        User user = findById(userId);
        user.setUserRole(role);
        userRepository.save(user);
        log.info("Rol de usuario {} actualizado a {}", user.getEmail(), role);
    }

    @Override
    public String createPasswordResetToken(String email) {
        User user = findByEmail(email);
        recoveryTokenRepository.deleteByUser_Id(user.getId());

        String token = UUID.randomUUID().toString();
        RecoveryToken recoveryToken = new RecoveryToken(token, user, 60); // 60 minutos
        recoveryTokenRepository.save(recoveryToken);

        log.info("Token de recuperación creado para {}: {}", email, token);
        return token;
    }

    @Override
    public void resetPasswordWithToken(String token, String newPassword) {
        RecoveryToken recoveryToken = recoveryTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("El token de recuperación no es válido o ha expirado."));

        if (!recoveryToken.validate()) {
            throw new BadRequestException("El token de recuperación ya fue utilizado o ha expirado.");
        }

        User user = recoveryToken.getUser();
        user.changePassword(passwordEncoder.encode(newPassword));
        recoveryToken.markAsUsed();

        userRepository.save(user);
        recoveryTokenRepository.save(recoveryToken);
        log.info("Contraseña restablecida exitosamente para {}", user.getEmail());
    }
}
