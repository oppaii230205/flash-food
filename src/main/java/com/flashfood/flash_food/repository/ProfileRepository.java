package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.Profile;
import com.flashfood.flash_food.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for the {@link Profile} entity.
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(User user);

    Optional<Profile> findByUserId(Long userId);

    boolean existsByPhoneNumber(String phoneNumber);

    /** Used when updating a phone number to ensure uniqueness excluding the current user's profile. */
    boolean existsByPhoneNumberAndUser_IdNot(String phoneNumber, Long userId);
}
