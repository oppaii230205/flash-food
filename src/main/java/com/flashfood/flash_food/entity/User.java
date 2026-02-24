package com.flashfood.flash_food.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity - holds authentication credentials and account meta-data only.
 *
 * Personal information (name, phone, address, location, etc.) lives in the
 * {@link Profile} entity, following the single-responsibility principle and
 * making it easy to extend either side independently.
 *
 * Implements {@link UserDetails} so it can be used directly as a Spring Security
 * principal without an extra wrapper class.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * One-to-one link to the profile that owns this user's personal data.
     * Cascade ALL so that an orphaned profile is deleted when the user is deleted.
     * EAGER fetch ensures the profile is always available without extra queries
     * (virtually every authenticated request needs name/phone data).
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
    private Profile profile;

    /** Supports multiple roles per account (e.g., STORE_OWNER + ADMIN). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // -------------------------------------------------------------------------
    // UserDetails implementation
    // -------------------------------------------------------------------------

    /**
     * Maps each {@link UserRole} to a Spring Security authority using the
     * conventional "ROLE_" prefix so that @PreAuthorize("hasRole('ADMIN')") works.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
    }

    /** The login identifier is the email address. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
