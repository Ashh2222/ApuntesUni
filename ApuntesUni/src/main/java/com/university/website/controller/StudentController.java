package com.university.website.controller;

import com.university.website.dto.PasswordChangeDto;
import com.university.website.dto.UserProfileDto;
import com.university.website.model.*;
import com.university.website.repository.StudentProgramRepository;
import com.university.website.security.CustomUserDetails;
import com.university.website.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StudentController {

    private final UserService userService;
    private final AcademicService academicService;
    private final EnrollmentService enrollmentService;
    private final NoteService noteService;
    private final FavoriteService favoriteService;
    private final NotificationService notificationService;
    private final StudentProgramRepository studentProgramRepository;

    @GetMapping("/onboarding")
    public String showOnboarding(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        model.addAttribute("programs", academicService.findAllPrograms());
        model.addAttribute("subjects", academicService.findAllSubjects());
        model.addAttribute("currentUser", currentUser);
        return "onboarding";
    }

    @PostMapping("/onboarding")
    public String processOnboarding(
            @RequestParam("programId") Long programId,
            @RequestParam(value = "semester", defaultValue = "1") int semester,
            @RequestParam(value = "subjectIds", required = false) List<Long> subjectIds,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Student student = userService.findStudentById(currentUser.getId());
            AcademicProgram program = academicService.findProgramById(programId);

            // Guardar carrera principal
            var existingProgram = studentProgramRepository.findByStudent_IdAndPrimaryProgramTrue(student.getId());
            if (existingProgram.isPresent()) {
                StudentProgram sp = existingProgram.get();
                sp.setAcademicProgram(program);
                sp.setCurrentSemester(semester);
                studentProgramRepository.save(sp);
            } else {
                StudentProgram sp = new StudentProgram(student, program, true, semester);
                studentProgramRepository.save(sp);
            }

            // Inscribir en materias seleccionadas
            if (subjectIds != null) {
                for (Long subId : subjectIds) {
                    try {
                        enrollmentService.enroll(student.getId(), subId, "2025-2");
                    } catch (Exception ignored) {}
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "¡Bienvenido a Apuntes UNI! Tu perfil ha sido configurado.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            log.error("Error en onboarding: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar preferencias: " + e.getMessage());
            return "redirect:/onboarding";
        }
    }

    @GetMapping("/mis-materias")
    public String showMySubjects(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Long studentId = currentUser.getId();
        List<Subject> enrolledSubjects = enrollmentService.findActiveSubjectsByStudent(studentId);
        List<Subject> allSubjects = academicService.findAllSubjects();

        List<Subject> availableSubjects = allSubjects.stream()
                .filter(s -> enrolledSubjects.stream().noneMatch(e -> e.getId().equals(s.getId())))
                .toList();

        model.addAttribute("enrolledSubjects", enrolledSubjects);
        model.addAttribute("availableSubjects", availableSubjects);
        model.addAttribute("currentUser", currentUser);

        return "mis-materias";
    }

    @PostMapping("/mis-materias/inscribir")
    public String enrollSubject(
            @RequestParam("subjectId") Long subjectId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            enrollmentService.enroll(currentUser.getId(), subjectId, "2025-2");
            redirectAttributes.addFlashAttribute("successMessage", "Materia inscrita exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/mis-materias";
    }

    @PostMapping("/mis-materias/baja")
    public String dropSubject(
            @RequestParam("subjectId") Long subjectId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            enrollmentService.drop(currentUser.getId(), subjectId);
            redirectAttributes.addFlashAttribute("successMessage", "Materia dada de baja.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/mis-materias";
    }

    @GetMapping("/perfil")
    public String showProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Long userId = currentUser.getId();
        User user = userService.findById(userId);
        Student student = (user instanceof Student s) ? s : null;

        var myNotes = noteService.findUserNotes(userId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "uploadDate")));
        var myFavorites = favoriteService.findUserFavorites(userId, PageRequest.of(0, 10));

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("myNotes", myNotes.getContent());
        model.addAttribute("myFavorites", myFavorites.getContent());
        model.addAttribute("currentUser", currentUser);

        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setFullName(user.getFullName());
        profileDto.setBio(user.getBio());
        profileDto.setProfileVisibility(user.getProfileVisibility());
        model.addAttribute("profileDto", profileDto);

        return "perfil";
    }

    @PostMapping("/perfil")
    public String updateProfile(
            @Valid @ModelAttribute("profileDto") UserProfileDto profileDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar perfil. Verifica los campos.");
            return "redirect:/perfil";
        }

        try {
            userService.updateProfile(currentUser.getId(), profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/perfil";
    }

    @GetMapping("/configuracion")
    public String showSettings(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Long userId = currentUser.getId();
        NotificationSettings settings = notificationService.getSettings(userId);

        model.addAttribute("settings", settings);
        model.addAttribute("passwordDto", new PasswordChangeDto());
        model.addAttribute("currentUser", currentUser);

        return "configuracion";
    }

    @PostMapping("/configuracion/notificaciones")
    public String updateNotificationSettings(
            @RequestParam(value = "emailNotifications", defaultValue = "false") boolean email,
            @RequestParam(value = "commentAlerts", defaultValue = "false") boolean comments,
            @RequestParam(value = "ratingAlerts", defaultValue = "false") boolean ratings,
            @RequestParam(value = "reportAlerts", defaultValue = "false") boolean reports,
            @RequestParam(value = "subjectAlerts", defaultValue = "false") boolean subjects,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            notificationService.updateSettings(currentUser.getId(), email, comments, ratings, reports, subjects);
            redirectAttributes.addFlashAttribute("successMessage", "Preferencias de notificaciones guardadas.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/configuracion";
    }

    @PostMapping("/configuracion/password")
    public String changePassword(
            @Valid @ModelAttribute("passwordDto") PasswordChangeDto passwordDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "La confirmación de la nueva contraseña no coincide.");
            return "redirect:/configuracion";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "La contraseña debe tener mínimo 8 caracteres.");
            return "redirect:/configuracion";
        }

        try {
            userService.changePassword(currentUser.getId(), passwordDto);
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña cambiada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/configuracion";
    }

    @GetMapping("/notificaciones")
    public String showNotifications(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationService.getUserNotifications(currentUser.getId(), pageable);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.getUnreadCount(currentUser.getId()));
        model.addAttribute("currentUser", currentUser);

        return "notificaciones";
    }

    @PostMapping("/notificaciones/{id}/leer")
    public String markNotificationRead(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        notificationService.markAsRead(id, currentUser.getId());
        return "redirect:/notificaciones";
    }

    @PostMapping("/notificaciones/leer-todas")
    public String markAllNotificationsRead(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        notificationService.markAllAsRead(currentUser.getId());
        redirectAttributes.addFlashAttribute("successMessage", "Todas las notificaciones marcadas como leídas.");
        return "redirect:/notificaciones";
    }
}
