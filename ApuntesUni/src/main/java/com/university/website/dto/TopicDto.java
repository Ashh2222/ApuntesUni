package com.university.website.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopicDto {

    private Long id;

    @Size(max = 30, message = "El código no puede superar 30 caracteres")
    private String code;

    @NotBlank(message = "El nombre del tema es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String description;

    @NotNull(message = "El orden es obligatorio")
    private Integer orderIndex = 1;

    @Size(max = 500, message = "El objetivo de aprendizaje no puede superar 500 caracteres")
    private String learningObjective;

    @NotNull(message = "Debes indicar la materia")
    private Long subjectId;
}
