package com.university.website.controller;

import com.university.website.dto.UserRegistrationDto;
import com.university.website.security.CustomUserDetails;
import com.university.website.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        if (currentUser != null) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Correo o contraseña incorrectos. Por favor, verifica tus datos.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Has cerrado sesión correctamente.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "¡Registro exitoso! Ya puedes iniciar sesión con tu correo.");
        }
        return "login";
    }

    @GetMapping("/registro")
    public String showRegistrationForm(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        if (currentUser != null) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new UserRegistrationDto());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String registerUser(
            @Valid @ModelAttribute("registrationDto") UserRegistrationDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Las contraseñas no coinciden");
        }

        if (bindingResult.hasErrors()) {
            return "registro";
        }

        try {
            userService.registerStudent(dto);
            redirectAttributes.addFlashAttribute("successMessage", "¡Cuenta creada exitosamente! Inicia sesión para continuar.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Error durante el registro: ", e);
            model.addAttribute("errorMessage", e.getMessage());
            return "registro";
        }
    }

    @GetMapping("/recuperar-password")
    public String showForgotPasswordForm() {
        return "recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String token = userService.createPasswordResetToken(email);
            log.info("Token de recuperación generado para {}: {}", email, token);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Si el correo se encuentra registrado, recibirás un enlace para restablecer tu contraseña.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Si el correo se encuentra registrado, recibirás un enlace para restablecer tu contraseña.");
        }
        return "redirect:/recuperar-password";
    }

    @GetMapping("/restablecer-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "restablecer-password";
    }

    @PostMapping("/restablecer-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Las contraseñas no coinciden.");
            return "redirect:/restablecer-password?token=" + token;
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/restablecer-password?token=" + token;
        }
        try {
            userService.resetPasswordWithToken(token, password);
            redirectAttributes.addFlashAttribute("successMessage", "Contraseña restablecida exitosamente. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/recuperar-password";
        }
    }
}
