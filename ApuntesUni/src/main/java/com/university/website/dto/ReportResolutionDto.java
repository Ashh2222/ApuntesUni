package com.university.website.dto;

import com.university.website.model.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportResolutionDto {

    @NotNull(message = "Debes indicar la resolución (RESOLVED o DISMISSED)")
    private ReportStatus status;

    @NotBlank(message = "Debes ingresar las notas de la resolución")
    @Size(max = 1000, message = "Las notas no pueden superar 1000 caracteres")
    private String resolutionNotes;
}
