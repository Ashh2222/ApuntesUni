package com.university.website.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingDto {

    @NotNull(message = "Debes seleccionar una puntuación")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer value;

    @Size(max = 255, message = "La nota de opinión no puede superar 255 caracteres")
    private String feedbackNote;
}
