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
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByStatus(UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Find users that have a specific role (list and paginated variants)
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") UserRole role);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    Page<User> findByRole(@Param("role") UserRole role, Pageable pageable);

    /**
     * Search users by full name, email or phone number
     */
    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')
    """)
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find users with push notifications enabled (for geo-notification scheduler)
     */
    @Query("""
        SELECT u FROM User u 
        WHERE u.notificationEnabled = true 
        AND u.status = :status
        AND u.latitude IS NOT NULL 
        AND u.longitude IS NOT NULL
    """)
    List<User> findUsersWithNotificationsEnabled(@Param("status") UserStatus status);

    default List<User> findUsersWithNotificationsEnabled() {
        return findUsersWithNotificationsEnabled(UserStatus.ACTIVE);
    }
}
