package com.university.website.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        model.addAttribute("status", 404);
        model.addAttribute("error", "No encontrado");
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbidden(UnauthorizedActionException ex, Model model) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        model.addAttribute("status", 403);
        model.addAttribute("error", "Acceso denegado");
        model.addAttribute("message", ex.getMessage());
        return "error/403";
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(BadRequestException ex, Model model) {
        log.warn("Petición incorrecta: {}", ex.getMessage());
        model.addAttribute("status", 400);
        model.addAttribute("error", "Solicitud incorrecta");
        model.addAttribute("message", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        log.warn("Archivo excede tamaño máximo permitido");
        redirectAttributes.addFlashAttribute("errorMessage", "El archivo excede el tamaño máximo permitido (máx. 25 MB).");
        return "redirect:/apuntes/subir";
    }

    @ExceptionHandler(FileStorageException.class)
    public String handleFileStorageException(FileStorageException ex, Model model) {
        log.error("Error en almacenamiento de archivo: {}", ex.getMessage());
        model.addAttribute("status", 500);
        model.addAttribute("error", "Error de almacenamiento");
        model.addAttribute("message", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Error no controlado en la aplicación: ", ex);
        model.addAttribute("status", 500);
        model.addAttribute("error", "Error interno del servidor");
        model.addAttribute("message", "Ocurrió un error inesperado. Por favor, intenta más tarde.");
        return "error/500";
    }
}
