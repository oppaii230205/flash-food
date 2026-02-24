package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.RefreshToken;
import com.flashfood.flash_food.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for the {@link RefreshToken} entity.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /** Retrieves all active (non-revoked) tokens for a user — used to revoke them on login/security events. */
    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    /** Bulk-revoke all tokens for a user via a single UPDATE (more efficient than loading entities). */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
    int revokeAllActiveTokensByUserId(@Param("userId") Long userId);

    /** Housekeeping: delete expired tokens to keep the table lean. */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    int deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

    void deleteByUser(User user);
}
