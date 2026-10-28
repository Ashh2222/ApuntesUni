package com.university.website.dto;

import com.university.website.model.enums.ReportReason;
import com.university.website.model.enums.ReportSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportCreateDto {

    @NotNull(message = "Debes indicar el motivo del reporte")
    private ReportReason reason;

    @NotNull(message = "Debes indicar la severidad")
    private ReportSeverity severity = ReportSeverity.MEDIUM;

    @NotBlank(message = "Describe el problema encontrado")
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String description;
}
