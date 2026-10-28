package com.university.website.controller;

import com.university.website.dto.AcademicProgramDto;
import com.university.website.dto.SubjectDto;
import com.university.website.model.User;
import com.university.website.model.enums.UserRole;
import com.university.website.security.CustomUserDetails;
import com.university.website.service.AcademicService;
import com.university.website.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
@RequiredArgsConstructor
public class AdminController {

    private final AcademicService academicService;
    private final UserService userService;

    @GetMapping("/materias")
    public String adminSubjects(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        model.addAttribute("subjects", academicService.findAllSubjects());
        model.addAttribute("programs", academicService.findAllPrograms());
        model.addAttribute("subjectDto", new SubjectDto());
        model.addAttribute("programDto", new AcademicProgramDto());
        model.addAttribute("currentUser", currentUser);

        return "admin-materias";
    }

    @PostMapping("/materias/crear")
    public String createSubject(
            @Valid @ModelAttribute("subjectDto") SubjectDto subjectDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear materia. Verifica el código y el nombre.");
            return "redirect:/admin/materias";
        }

        try {
            academicService.saveSubject(subjectDto);
            redirectAttributes.addFlashAttribute("successMessage", "Materia creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/materias";
    }

    @PostMapping("/materias/{id}/eliminar")
    public String deleteSubject(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            academicService.deleteSubject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Materia desactivada/eliminada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/materias";
    }

    @PostMapping("/carreras/crear")
    public String createProgram(
            @Valid @ModelAttribute("programDto") AcademicProgramDto programDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear carrera. Verifica los campos.");
            return "redirect:/admin/materias";
        }

        try {
            academicService.saveProgram(programDto);
            redirectAttributes.addFlashAttribute("successMessage", "Carrera creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/materias";
    }

    @GetMapping("/roles")
    public String adminRoles(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Page<User> users = userService.findAllUsers(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));

        model.addAttribute("users", users);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("currentUser", currentUser);

        return "admin-roles";
    }

    @PostMapping("/roles/{id}/cambiar")
    public String changeUserRole(
            @PathVariable("id") Long id,
            @RequestParam("role") UserRole role,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "Rol de usuario actualizado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/roles/{id}/toggle-activo")
    public String toggleUserActive(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.toggleUserActive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Estado de usuario cambiado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles";
    }
}
