package com.university.website.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class NoteUploadDto {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String title;

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String description;

    @NotNull(message = "Debes seleccionar una materia")
    private Long subjectId;

    private Long topicId;

    @Size(max = 255, message = "Las etiquetas no pueden superar los 255 caracteres")
    private String tags;

    private MultipartFile file;
}
