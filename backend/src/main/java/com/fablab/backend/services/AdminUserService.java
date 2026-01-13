package com.fablab.backend.services;

import com.fablab.backend.dto.AdminUserDTO;
import com.fablab.backend.models.User;
import com.fablab.backend.repositories.AuditLogRepository;
import com.fablab.backend.repositories.PasswordResetTokenRepository;
import com.fablab.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    /**
     * Returns users filtered by optional search and role.
     *
     * @param search search substring for username/email
     * @param role   role filter as string
     * @return list of {@link AdminUserDTO} projections
     */
    @Transactional(readOnly = true)
    public List<AdminUserDTO> listUsers(String search, String role) {
        String trimmedSearch = sanitize(search);
        User.Role parsedRole = parseRole(role, true);
        return userRepository.searchUsers(trimmedSearch, parsedRole)
                .stream()
                .map(this::toAdminUserDto)
                .toList();
    }

    /**
     * Updates the role for a specific user.
     *
     * @param userId        identifier of the user to update
     * @param role          new role value
     * @param actorUsername username of the requesting super admin
     * @return updated user projection
     */
    @Transactional
    public AdminUserDTO changeRole(Long userId, String role, String actorUsername) {
        User actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        User.Role targetRole = parseRole(role, false);
        if (user.getRole() == User.Role.SUPER_ADMIN
                && targetRole != User.Role.SUPER_ADMIN
                && isLastSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot downgrade the last SUPER_ADMIN");
        }

        if (user.getRole() != targetRole) {
            User.Role previousRole = user.getRole();
            user.setRole(targetRole);
            userRepository.save(user);
            auditLogService.logAction(actor.getId(), "USER_ROLE_CHANGE",
                    "Changed role for user " + user.getUsername() + " from " + previousRole + " to " + targetRole);
        }

        return toAdminUserDto(user);
    }

    /**
     * Deletes a user account after enforcing guardrails.
     *
     * @param userId        identifier of the user to delete
     * @param actorUsername username of the requesting super admin
     */
    @Transactional
    public void deleteUser(Long userId, String actorUsername) {
        User actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (actor.getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete your own account");
        }

        if (user.getRole() == User.Role.SUPER_ADMIN && isLastSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete the last SUPER_ADMIN");
        }

        passwordResetTokenRepository.deleteByUser_Id(user.getId());
        auditLogRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
        auditLogService.logAction(actor.getId(), "USER_DELETE",
                "Deleted user " + user.getUsername() + " (" + user.getEmail() + ")");
    }

    private AdminUserDTO toAdminUserDto(User user) {
        return new AdminUserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    private User.Role parseRole(String roleValue, boolean allowNull) {
        String cleaned = sanitize(roleValue);
        if (cleaned == null) {
            if (allowNull) {
                return null;
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        try {
            return User.Role.valueOf(cleaned);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + cleaned);
        }
    }

    private String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isLastSuperAdmin() {
        return userRepository.countByRole(User.Role.SUPER_ADMIN) <= 1;
    }
}