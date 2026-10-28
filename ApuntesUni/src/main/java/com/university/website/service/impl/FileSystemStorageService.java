package com.university.website.service.impl;

import com.university.website.exception.BadRequestException;
import com.university.website.exception.FileStorageException;
import com.university.website.exception.ResourceNotFoundException;
import com.university.website.service.StorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "docx", "pptx", "txt", "png", "jpg", "jpeg"
    );

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "image/png",
            "image/jpeg"
    );

    private final Path rootLocation;

    public FileSystemStorageService(@Value("${app.storage.location:./uploads/apuntes}") String location) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("Directorio de almacenamiento de archivos inicializado en: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new FileStorageException("No se pudo inicializar el directorio de almacenamiento", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No se puede almacenar un archivo vacío.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento");

        // Sanitización contra path traversal
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Nombre de archivo inválido: contiene secuencia relativa.");
        }

        // Validación de extensión
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Formato de archivo no permitido. Solo se aceptan: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Validación de tipo MIME
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("MIME type {} no estrictamente en lista blanca, permitiendo por extensión {}", contentType, extension);
        }

        // Generar nombre único con UUID para almacenamiento físico seguro
        String storedFilename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        Path destinationFile = this.rootLocation.resolve(storedFilename).normalize();

        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new BadRequestException("No se puede almacenar el archivo fuera del directorio configurado.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archivo almacenado exitosamente: {} (nombre original: {})", storedFilename, originalFilename);
            return storedFilename;
        } catch (IOException e) {
            throw new FileStorageException("Error al guardar el archivo " + originalFilename, e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("No se pudo leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Error de ruta de archivo: " + filename);
        }
    }

    @Override
    public void delete(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path file = load(filename);
            Files.deleteIfExists(file);
            log.info("Archivo eliminado del almacenamiento: {}", filename);
        } catch (IOException e) {
            log.error("No se pudo eliminar el archivo físico: {}", filename, e);
        }
    }

    @Override
    public long getFileSize(String filename) {
        try {
            Path file = load(filename);
            return Files.size(file);
        } catch (IOException e) {
            return 0L;
        }
    }
}
