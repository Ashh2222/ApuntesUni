package com.university.website.controller;

import com.university.website.dto.*;
import com.university.website.model.Note;
import com.university.website.model.Rating;
import com.university.website.model.Subject;
import com.university.website.model.Topic;
import com.university.website.model.enums.FileFormat;
import com.university.website.security.CustomUserDetails;
import com.university.website.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final AcademicService academicService;
    private final RatingService ratingService;
    private final CommentService commentService;
    private final FavoriteService favoriteService;
    private final ReportService reportService;
    private final DownloadService downloadService;

    @GetMapping("/buscar")
    public String searchNotes(
            @ModelAttribute("filter") NoteFilterDto filter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        filter.setPage(page);
        filter.setSize(size);
        Page<Note> notes = noteService.searchNotes(filter);

        model.addAttribute("notes", notes);
        model.addAttribute("programs", academicService.findAllPrograms());
        model.addAttribute("subjects", academicService.findAllSubjects());

        if (filter.getSubjectId() != null) {
            model.addAttribute("topics", academicService.findTopicsBySubject(filter.getSubjectId()));
        } else {
            model.addAttribute("topics", Collections.emptyList());
        }

        model.addAttribute("formats", FileFormat.values());
        model.addAttribute("currentUser", currentUser);

        return "buscar";
    }

    @GetMapping({"/apuntes/{id}", "/archivo.html"})
    public String viewNote(
            @PathVariable(value = "id", required = false) Long id,
            @RequestParam(value = "id", required = false) Long paramId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Long noteId = (id != null) ? id : paramId;
        if (noteId == null) {
            return "redirect:/buscar";
        }

        Note note = noteService.findByIdWithIncrementViews(noteId);
        Long userId = (currentUser != null) ? currentUser.getId() : null;

        boolean isFav = false;
        Integer userRatingValue = null;

        if (userId != null) {
            isFav = favoriteService.isFavorite(noteId, userId);
            userRatingValue = ratingService.findUserRating(noteId, userId)
                    .map(Rating::getValue)
                    .orElse(null);
        }

        model.addAttribute("note", note);
        model.addAttribute("isFavorite", isFav);
        model.addAttribute("userRating", userRatingValue);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("comments", commentService.findActiveCommentsByNote(noteId));
        model.addAttribute("commentDto", new CommentCreateDto());
        model.addAttribute("ratingDto", new RatingDto());
        model.addAttribute("reportDto", new ReportCreateDto());

        return "archivo";
    }

    @GetMapping("/apuntes/{id}/descargar")
    public ResponseEntity<Resource> downloadNote(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request
    ) {
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        var downloadResult = downloadService.downloadNote(id, userId, ipAddress, userAgent);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadResult.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadResult.filename() + "\"")
                .body(downloadResult.resource());
    }

    @GetMapping("/apuntes/{id}/vista-previa")
    public ResponseEntity<Resource> previewNote(@PathVariable("id") Long id) {
        Note note = noteService.findById(id);
        var downloadResult = downloadService.downloadNote(id, null, "preview", "internal");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadResult.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + downloadResult.filename() + "\"")
                .body(downloadResult.resource());
    }

    @GetMapping("/subir")
    public String showUploadForm(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        if (!model.containsAttribute("uploadDto")) {
            model.addAttribute("uploadDto", new NoteUploadDto());
        }
        model.addAttribute("subjects", academicService.findAllSubjects());
        model.addAttribute("programs", academicService.findAllPrograms());
        model.addAttribute("currentUser", currentUser);

        return "subir";
    }

    @PostMapping("/subir")
    public String processUpload(
            @Valid @ModelAttribute("uploadDto") NoteUploadDto uploadDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (uploadDto.getFile() == null || uploadDto.getFile().isEmpty()) {
            bindingResult.rejectValue("file", "error.file", "Debes seleccionar un archivo para subir");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("subjects", academicService.findAllSubjects());
            model.addAttribute("programs", academicService.findAllPrograms());
            model.addAttribute("currentUser", currentUser);
            return "subir";
        }

        try {
            Note createdNote = noteService.uploadNote(uploadDto, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "¡Apunte subido exitosamente!");
            return "redirect:/apuntes/" + createdNote.getId();
        } catch (Exception e) {
            log.error("Error al subir archivo: ", e);
            model.addAttribute("errorMessage", "Error al procesar el archivo: " + e.getMessage());
            model.addAttribute("subjects", academicService.findAllSubjects());
            model.addAttribute("programs", academicService.findAllPrograms());
            model.addAttribute("currentUser", currentUser);
            return "subir";
        }
    }

    @PostMapping("/apuntes/{id}/valorar")
    public String rateNote(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("ratingDto") RatingDto ratingDto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ratingService.rateNote(id, currentUser.getId(), ratingDto);
            redirectAttributes.addFlashAttribute("successMessage", "¡Calificación guardada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/apuntes/" + id;
    }

    @PostMapping("/apuntes/{id}/comentar")
    public String commentNote(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("commentDto") CommentCreateDto commentDto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            commentService.addComment(id, currentUser.getId(), commentDto);
            redirectAttributes.addFlashAttribute("successMessage", "Comentario publicado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/apuntes/" + id;
    }

    @PostMapping("/apuntes/{id}/favorito")
    public String toggleFavorite(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        boolean fav = favoriteService.toggleFavorite(id, currentUser.getId());
        if (fav) {
            redirectAttributes.addFlashAttribute("successMessage", "Agregado a tus favoritos.");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage", "Eliminado de tus favoritos.");
        }
        return "redirect:/apuntes/" + id;
    }

    @PostMapping("/apuntes/{id}/reportar")
    public String reportNote(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("reportDto") ReportCreateDto reportDto,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reportService.createReport(id, currentUser.getId(), reportDto);
            redirectAttributes.addFlashAttribute("successMessage", "Reporte enviado. Los moderadores revisarán el contenido a la brevedad.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/apuntes/" + id;
    }

    @PostMapping("/apuntes/{id}/eliminar")
    public String deleteNote(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            noteService.deleteNote(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Apunte eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buscar";
    }

    @GetMapping("/api/materias/{subjectId}/temas")
    @ResponseBody
    public List<TopicDto> getTopicsForSubject(@PathVariable("subjectId") Long subjectId) {
        return academicService.findTopicsBySubject(subjectId).stream()
                .map(t -> {
                    TopicDto dto = new TopicDto();
                    dto.setId(t.getId());
                    dto.setCode(t.getCode());
                    dto.setName(t.getName());
                    dto.setDescription(t.getDescription());
                    dto.setOrderIndex(t.getOrderIndex());
                    dto.setLearningObjective(t.getLearningObjective());
                    dto.setSubjectId(t.getSubject().getId());
                    return dto;
                })
                .toList();
    }
}
