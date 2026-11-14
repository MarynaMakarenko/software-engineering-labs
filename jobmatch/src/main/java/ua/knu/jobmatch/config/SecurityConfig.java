package ua.knu.jobmatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/about", "/help", "/vacancies", "/vacancies/*", "/login", "/register",
                        "/css/**", "/js/**", "/img/**", "/docs/**", "/favicon.ico", "/error", "/error/**", "/robots.txt").permitAll()
                .requestMatchers("/me/**").hasRole("CANDIDATE")
                .requestMatchers("/company/**").hasRole("EMPLOYER")
                .requestMatchers("/recruiter/**").hasRole("RECRUITER")
                .anyRequest().permitAll())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .successHandler((req, res, authn) -> {
                    AccountPrincipal p = (AccountPrincipal) authn.getPrincipal();
                    res.sendRedirect(req.getContextPath() + p.homeUrl());
                })
                .failureUrl("/login?error")
                .permitAll())
            .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/?logout").permitAll())
            .exceptionHandling(e -> e.accessDeniedPage("/error/403"))
            .headers(h -> h
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; img-src 'self' data:; style-src 'self'; script-src 'self'; frame-ancestors 'none'; form-action 'self'"))
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));
        return http.build();
    }
}
