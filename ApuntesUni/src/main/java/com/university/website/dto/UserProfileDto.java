package com.university.website.dto;

import com.university.website.model.enums.ProfileVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDto {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String fullName;

    @Size(max = 1000, message = "La biografía no puede superar los 1000 caracteres")
    private String bio;

    private ProfileVisibility profileVisibility;

    private String institutionalEmail;

    @Size(max = 500, message = "Los intereses no pueden superar los 500 caracteres")
    private String academicInterests;
}
