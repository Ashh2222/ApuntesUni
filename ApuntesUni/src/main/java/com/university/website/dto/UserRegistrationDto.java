package com.university.website.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationDto {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String fullName;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ser un correo electrónico válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 50, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "Confirma tu contraseña")
    private String confirmPassword;

    private String studentCode;

    private Integer entryYear;

    private Long academicProgramId;

    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
