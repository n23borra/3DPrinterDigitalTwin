package com.fablab.backend.controllers;

import com.fablab.backend.models.Control;
import com.fablab.backend.repositories.ControlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/controls")
@RequiredArgsConstructor
public class ControlController {

    private final ControlRepository controlRepo;

    /**
     * Retrieves all defined controls.
     *
     * @return {@link ResponseEntity} containing the list of {@link Control} records
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Control>> list() {
        return ResponseEntity.ok(controlRepo.findAll());
    }

    /**
     * Creates a new control definition.
     *
     * @param c control payload to persist; its identifier is cleared to ensure a new record is inserted
     * @return {@link ResponseEntity} containing the stored {@link Control}
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Control> create(@RequestBody Control c) {
        c.setId(null);
        return ResponseEntity.ok(controlRepo.save(c));
    }

    /**
     * Updates an existing control with the provided values.
     *
     * @param id identifier of the control to update
     * @param data payload carrying the new values to merge into the control
     * @return {@link ResponseEntity} containing the updated {@link Control}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Control> update(@PathVariable Long id, @RequestBody Control data) {
        Control ctrl = controlRepo.findById(id).orElseThrow();
        if (data.getUuid() != null) ctrl.setUuid(data.getUuid());
        if (data.getCode() != null) ctrl.setCode(data.getCode());
        if (data.getLabel() != null) ctrl.setLabel(data.getLabel());
        if (data.getDescription() != null) ctrl.setDescription(data.getDescription());
        if (data.getMeta() != null) ctrl.setMeta(data.getMeta());
        if (data.getCategory() != null) ctrl.setCategory(data.getCategory());
        if (data.getReferential() != null) ctrl.setReferential(data.getReferential());
        if (data.getReferentialLabel() != null) ctrl.setReferentialLabel(data.getReferentialLabel());
        ctrl.setEfficiency(data.getEfficiency());
        return ResponseEntity.ok(controlRepo.save(ctrl));
    }

    /**
     * Deletes a control by its identifier.
     *
     * @param id identifier of the control to remove
     * @return {@link ResponseEntity} with {@code 200 OK} after deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        controlRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}