package ua.knu.jobmatch.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import ua.knu.jobmatch.domain.Role;
import ua.knu.jobmatch.domain.UserAccount;

import java.util.List;

/** Автентифікований користувач із ідентифікатором облікового запису та роллю. */
public class AccountPrincipal extends User {
    private final Long accountId;
    private final Role role;

    public AccountPrincipal(UserAccount a) {
        super(a.getEmail(), a.getPasswordHash(), a.isEnabled(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + a.getRole().name())));
        this.accountId = a.getId();
        this.role = a.getRole();
    }

    public Long getAccountId() { return accountId; }

    public Role getRole() { return role; }

    public String homeUrl() {
        return switch (role) {
            case CANDIDATE -> "/me/resume";
            case EMPLOYER -> "/company";
            case RECRUITER -> "/recruiter";
        };
    }
}
