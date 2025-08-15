package com.otavio.aifoodapp.model;

import com.otavio.aifoodapp.enums.UserRoles;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Entity(name = "users")
@Table(name = "tb_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Make nullable for Google-only users
    @Column(unique = true)
    private String login;

    // Make nullable for Google-only users
    @Column
    private String password;

    private String firstName;
    private String lastName;

    // Unique email (case-insensitive uniqueness is enforced by DB index on lower(email))
    @Column
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRoles role;

    // Google OAuth fields
    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "provider")
    private String provider; // e.g., "GOOGLE" or "LOCAL"

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    public User(String login, String password, UserRoles role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }

    // UserDetails

    @Override
    public String getUsername() {
        return login != null ? login : (email != null ? email : (googleId != null ? googleId : String.valueOf(id)));
    }

    @Override
    public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        if (this.role == UserRoles.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return isActive != null ? isActive : true; }
}