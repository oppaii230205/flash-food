package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.entity.UserRole;
import com.flashfood.flash_food.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link User} entity.
 *
 * Note: phone number uniqueness queries now live in {@link ProfileRepository}
 * because the {@code phone_number} column was moved to the {@code profiles} table.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Find users that have a specific role.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") UserRole role);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    Page<User> findByRole(@Param("role") UserRole role, Pageable pageable);

    /**
     * Search users by full name, email or phone number.
     * fullName and phoneNumber now reside in the joined Profile.
     */
    @Query("""
        SELECT u FROM User u
        JOIN u.profile p
        WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR p.phoneNumber LIKE CONCAT('%', :keyword, '%')
    """)
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find users with push notifications enabled whose location is known.
     * Location and notification preferences now reside in the joined Profile.
     */
    @Query("""
        SELECT u FROM User u
        JOIN u.profile p
        WHERE p.notificationEnabled = true
        AND u.status = :status
        AND p.latitude IS NOT NULL
        AND p.longitude IS NOT NULL
    """)
    List<User> findUsersWithNotificationsEnabled(@Param("status") UserStatus status);

    default List<User> findUsersWithNotificationsEnabled() {
        return findUsersWithNotificationsEnabled(UserStatus.ACTIVE);
    }
}

