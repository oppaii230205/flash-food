package com.flashfood.flash_food.repository;

import com.flashfood.flash_food.entity.Store;
import com.flashfood.flash_food.entity.StoreStatus;
import com.flashfood.flash_food.entity.StoreType;
import com.flashfood.flash_food.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Store}.
 *
 * JPQL note: enum constants are passed as named parameters so that Hibernate can
 * apply the registered {@code AttributeConverter} automatically — never hard-code
 * enum literals inside the JPQL string (e.g. {@code = StoreStatus.ACTIVE}) because
 * that form bypasses the converter and compares against the enum name rather than
 * its integer DB code.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // -------------------------------------------------------------------------
    // Simple finders
    // -------------------------------------------------------------------------

    Optional<Store> findByIdAndStatus(Long id, StoreStatus status);

    Page<Store> findByStatus(StoreStatus status, Pageable pageable);

    Page<Store> findByType(StoreType type, Pageable pageable);

    Page<Store> findByStatusAndType(StoreStatus status, StoreType type, Pageable pageable);

    List<Store> findByOwner(User owner);

    List<Store> findByIdInAndStatus(List<Long> ids, StoreStatus status);

    boolean existsByOwnerAndName(User owner, String name);

    // -------------------------------------------------------------------------
    // Full-text search
    // -------------------------------------------------------------------------

    /**
     * Case-insensitive name search restricted to ACTIVE stores.
     * The {@code :status} parameter lets Hibernate apply the converter correctly.
     */
    @Query("""
        SELECT s FROM Store s
        WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          AND s.status = :status
        ORDER BY s.rating DESC
        """)
    Page<Store> searchByNameAndStatus(
            @Param("keyword") String keyword,
            @Param("status")  StoreStatus status,
            Pageable pageable);

    /**
     * Unrestricted name search across all statuses (admin use).
     */
    @Query("""
        SELECT s FROM Store s
        WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY s.rating DESC
        """)
    Page<Store> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // -------------------------------------------------------------------------
    // Geo / admin helpers
    // -------------------------------------------------------------------------

    /**
     * Returns all ACTIVE stores that have coordinates set so they can be
     * (re-)indexed into Redis Geo on startup.
     */
    @Query("""
        SELECT s FROM Store s
        WHERE s.status     = :status
          AND s.latitude   IS NOT NULL
          AND s.longitude  IS NOT NULL
        """)
    List<Store> findActiveStoresWithCoordinates(@Param("status") StoreStatus status);
}

