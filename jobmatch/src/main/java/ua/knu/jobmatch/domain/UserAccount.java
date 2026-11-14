package ua.knu.jobmatch.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_account")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected UserAccount() { }

    public UserAccount(String email, String passwordHash, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
