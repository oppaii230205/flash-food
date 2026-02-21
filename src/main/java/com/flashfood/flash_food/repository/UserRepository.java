package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.entity.UserRole;
import com.flashfood.flash_food.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    
    /**
     * Find users that have a specific role
     * Uses JOIN on user_roles table
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") UserRole role);
    
    /**
     * Find users within a certain radius for geo-notifications
     * Note: For production, use Redis Geo for this operation
     */
    @Query("""
        SELECT u FROM User u 
        WHERE u.notificationEnabled = true 
        AND u.status = UserStatus.ACTIVE
        AND u.latitude IS NOT NULL 
        AND u.longitude IS NOT NULL
    """)
    List<User> findUsersWithNotificationsEnabled();
}
