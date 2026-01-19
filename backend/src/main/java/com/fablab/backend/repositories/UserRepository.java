package com.fablab.backend.repositories;

import com.fablab.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository providing lookup operations for {@link User} accounts.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by username.
     *
     * @param username username supplied by the client
     * @return matching user when present
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by email address.
     *
     * @param email email supplied by the client
     * @return matching user when present
     */
    Optional<User> findByEmail(String email);

    /**
     * Searches for users by optional username/email substring and role filter.
     *
     * @param search optional substring matched against username or email
     * @param role   optional role filter
     * @return list of users matching the criteria
     */
    @Query("SELECT u FROM User u "
            + "WHERE (:role IS NULL OR u.role = :role) "
            + "AND (:search IS NULL OR (LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))) "
            + "ORDER BY u.username ASC")
    List<User> searchUsers(@Param("search") String search, @Param("role") User.Role role);

    /**
     * Lists all users ordered by username.
     *
     * @return list of all users ordered by username
     */
    List<User> findAllByOrderByUsernameAsc();

    /**
     * Lists all users for a specific role ordered by username.
     *
     * @param role role filter
     * @return list of users with the given role ordered by username
     */
    List<User> findByRoleOrderByUsernameAsc(User.Role role);

    /**
     * Counts the number of users with the specified role.
     *
     * @param role role to count
     * @return number of users assigned to the role
     */
    long countByRole(User.Role role);
}
