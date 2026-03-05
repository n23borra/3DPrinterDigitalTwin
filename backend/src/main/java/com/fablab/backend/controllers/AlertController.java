package com.fablab.backend.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fablab.backend.dto.AlertDTO;
import com.fablab.backend.models.Alert;
import com.fablab.backend.models.User;
import com.fablab.backend.models.printer.Printer;
import com.fablab.backend.repositories.AlertRepository;
import com.fablab.backend.repositories.UserRepository;
import com.fablab.backend.repositories.printer.PrinterRepository;
import com.fablab.backend.services.AlertModuleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;
    private final AlertModuleService alertService;
    private final PrinterRepository printerRepository;

    /**
     * Checks if the current authenticated user has admin or superadmin role.
     *
     * @return true if user is admin or superadmin, false otherwise
     */
    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERADMIN") || authority.equals("ROLE_SUPER_ADMIN"));
    }

    /**
     * Request body for creating or updating an alert.
     */
    public static record CreateAlertRequest(
        Long userId,
        String title,
        UUID printerId,
        String details,
        String severity,
        String priority,
        String category,
        Long assignedTo
    ) {}

    /**
     * Retrieves all alerts.
     *
     * @return list of all alerts
     */
    @GetMapping
    public List<AlertDTO> getAllAlerts() {
        Map<UUID, String> printerNames = printerRepository.findAll().stream()
                .collect(Collectors.toMap(Printer::getId, Printer::getName));
        return alertRepository.findAll()
                .stream()
                .map(a -> AlertDTO.from(a, printerNames.get(a.getPrinterId())))
                .toList();
    }

    /**
     * Retrieves all alerts for a specific user.
     *
     * @param userId id of the user
     * @return list of alerts for that user
     */
    @GetMapping("/user/{userId}")
    public List<AlertDTO> getUserAlerts(@PathVariable Long userId) {
        return alertRepository.findAllByUserId(userId)
                .stream()
                .map(AlertDTO::from)
                .toList();
    }

    /**
     * Retrieves all unresolved alerts.
     *
     * @return list of all unresolved alerts
     */
    @GetMapping("/unresolved")
    public List<AlertDTO> getAllUnresolvedAlerts() {
        Map<UUID, String> printerNames = printerRepository.findAll().stream()
                .collect(Collectors.toMap(Printer::getId, Printer::getName));
        return alertRepository.findByStatus(Alert.Status.UNRESOLVED)
                .stream()
                .map(a -> AlertDTO.from(a, printerNames.get(a.getPrinterId())))
                .toList();
    }

    @GetMapping("/in_progress")
    public List<AlertDTO> getAllInProgressAlerts(){
        return alertRepository.findByStatus(Alert.Status.IN_PROGRESS)
                .stream()
                .map(AlertDTO::from)
                .toList();
    }

    @GetMapping("/resolved")
    public List<AlertDTO> getAllResolvedAlerts(){
        return alertRepository.findByStatus(Alert.Status.RESOLVED)
                .stream()
                .map(AlertDTO::from)
                .toList();
    }

    /**
     * Retrieves unresolved alerts for a specific user.
     *
     * @param userId id of the user
     * @return list of unresolved alerts for that user
     */
    @GetMapping("/user/{userId}/unresolved")
    public List<AlertDTO> getUserUnresolvedAlerts(@PathVariable Long userId) {
        return alertRepository.findByUserIdAndStatus(userId, Alert.Status.UNRESOLVED)
                .stream()
                .map(AlertDTO::from)
                .toList();
    }


    /**
     * Creates a new alert.
     *
     * @param req alert creation request
     * @return created alert DTO
     */
    @PostMapping
    public ResponseEntity<AlertDTO> create(@RequestBody CreateAlertRequest req) {
        Alert alert = Alert.builder()
                .userId(req.userId())
                .title(req.title())
                .printerId(req.printerId())
                .details(req.details())
                .severity(req.severity() != null ? Alert.Severity.valueOf(req.severity()) : Alert.Severity.INFO)
                .priority(req.priority() != null ? Alert.Priority.valueOf(req.priority()) : Alert.Priority.MEDIUM)
                .category(req.category())
                .assignedTo(req.assignedTo())
                .status(Alert.Status.UNRESOLVED)
                .build();
        Alert saved = alertRepository.save(alert);

        if(alert.getSeverity().equals(Alert.Severity.CRITICAL)){
            
            User user = userRepo.findByEmail("matt.theret@gmail.com")
                    .orElseThrow(() -> new IllegalArgumentException("Email not found"));
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(user.getEmail());
            msg.setSubject("Fablab Alert");
            msg.setText("A new alert was raised.");
            mailSender.send(msg);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(AlertDTO.from(saved));
    }

    /**
     * Updates the resolved state of an alert.
     * Only admin and superadmin users can resolve alerts.
     *
     * @param id alert id
     * @param body request body with resolved boolean
     * @return updated alert DTO or 403 Forbidden if user is not admin
     */
    @PatchMapping("/{id}/resolved")
    public ResponseEntity<AlertDTO> setResolved(@PathVariable Long id, @RequestBody java.util.Map<String, Alert.Status> body) {
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Alert.Status status = body.getOrDefault("status", Alert.Status.UNRESOLVED);
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
        Alert.Status newStatus = Alert.Status.UNRESOLVED;
        if(status == Alert.Status.UNRESOLVED)
            newStatus = Alert.Status.IN_PROGRESS;
        else if(status == Alert.Status.IN_PROGRESS)
            newStatus = Alert.Status.RESOLVED;
        alert.setStatus(newStatus);
        System.err.println("STATUS = "+status+", NEW STATUS = "+newStatus);

        Alert saved = alertRepository.save(alert);
        System.err.println("NEW ALERT "+saved);
        return ResponseEntity.ok(AlertDTO.from(saved));
    }

    /**
     * Updates alert details (title, details, category, severity, priority, assignedTo).
     *
     * @param id alert id
     * @param req update request
     * @return updated alert DTO
     */
    @PatchMapping("/{id}")
    public ResponseEntity<AlertDTO> update(@PathVariable Long id, @RequestBody CreateAlertRequest req) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + id));
        
        if (req.title() != null) alert.setTitle(req.title());
        if (req.details() != null) alert.setDetails(req.details());
        if (req.severity() != null) alert.setSeverity(Alert.Severity.valueOf(req.severity()));
        if (req.priority() != null) alert.setPriority(Alert.Priority.valueOf(req.priority()));
        if (req.category() != null) alert.setCategory(req.category());
        alert.setAssignedTo(req.assignedTo());
        
        Alert saved = alertRepository.save(alert);
        return ResponseEntity.ok(AlertDTO.from(saved));
    }

    /**
     * Deletes an alert.
     * Only admin and superadmin users can delete alerts.
     *
     * @param id alert id
     * @return no content or 403 Forbidden if user is not admin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        alertRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bed_level")
    public void checkBedLeveling(){
        Alert alert = Alert.builder()
                    .userId(null)
                    .title("BED_LEVEL")
                    .details(alertService.checkBedLeveling(null).toString())
                    .category("LEVELING")
                    .severity(Alert.Severity.INFO)
                    .priority(Alert.Priority.MEDIUM)
                    .status(Alert.Status.UNRESOLVED)
                    .build();
            alertRepository.save(alert);
        System.out.println(alertService.checkBedLeveling(null));
    }
}
