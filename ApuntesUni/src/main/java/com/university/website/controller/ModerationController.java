package com.university.website.controller;

import com.university.website.dto.ReportResolutionDto;
import com.university.website.model.Report;
import com.university.website.model.enums.ReportStatus;
import com.university.website.security.CustomUserDetails;
import com.university.website.service.NoteService;
import com.university.website.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/moderacion")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMINISTRATOR')")
@RequiredArgsConstructor
public class ModerationController {

    private final ReportService reportService;
    private final NoteService noteService;

    @GetMapping("/reportes")
    public String viewReports(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Page<Report> reports = reportService.findPendingReports(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        model.addAttribute("reports", reports);
        model.addAttribute("pendingCount", reportService.countPendingReports());
        model.addAttribute("resolutionDto", new ReportResolutionDto());
        model.addAttribute("currentUser", currentUser);

        return "admin-reportes";
    }

    @PostMapping("/reportes/{id}/resolver")
    public String resolveReport(
            @PathVariable("id") Long id,
            @RequestParam("status") ReportStatus status,
            @RequestParam(value = "resolutionNotes", defaultValue = "Revisado por moderación") String notes,
            @RequestParam(value = "hideNote", defaultValue = "false") boolean hideNote,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ReportResolutionDto dto = new ReportResolutionDto();
            dto.setStatus(status);
            dto.setResolutionNotes(notes);

            if (ReportStatus.DISMISSED.equals(status)) {
                reportService.dismissReport(id, currentUser.getId(), dto);
                redirectAttributes.addFlashAttribute("successMessage", "Reporte desestimado.");
            } else {
                reportService.resolveReport(id, currentUser.getId(), dto, hideNote);
                redirectAttributes.addFlashAttribute("successMessage", "Reporte resuelto exitosamente.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/moderacion/reportes";
    }

    @PostMapping("/aprobar/{id}")
    public String approveNote(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            noteService.approveNote(id);
            redirectAttributes.addFlashAttribute("successMessage", "Apunte aprobado y publicado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/moderacion/reportes";
    }

    @PostMapping("/rechazar/{id}")
    public String rejectNote(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            noteService.rejectNote(id);
            redirectAttributes.addFlashAttribute("successMessage", "Apunte rechazado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/moderacion/reportes";
    }
}
