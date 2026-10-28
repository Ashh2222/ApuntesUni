package com.university.website.dto;

import com.university.website.model.enums.ProgramType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AcademicProgramDto {

    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 30, message = "El código no puede superar 30 caracteres")
    private String code;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String description;

    private String faculty;

    private Integer totalSemesters = 10;

    @NotNull(message = "El tipo de programa es obligatorio")
    private ProgramType type = ProgramType.DEGREE;

    private boolean active = true;

    private List<Long> relatedProgramIds = new ArrayList<>();
}
