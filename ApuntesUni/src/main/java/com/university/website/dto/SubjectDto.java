package com.university.website.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SubjectDto {

    private Long id;

    @NotBlank(message = "El código de la materia es obligatorio")
    @Size(max = 30, message = "El código no puede superar 30 caracteres")
    private String code;

    @NotBlank(message = "El nombre de la materia es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String description;

    @NotNull(message = "Los créditos son obligatorios")
    private Integer credits = 3;

    private boolean active = true;

    private List<Long> academicProgramIds = new ArrayList<>();

    private List<Long> prerequisiteIds = new ArrayList<>();
}
