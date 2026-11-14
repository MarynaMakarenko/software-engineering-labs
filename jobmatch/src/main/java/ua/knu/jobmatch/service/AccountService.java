package ua.knu.jobmatch.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.knu.jobmatch.config.AccountPrincipal;
import ua.knu.jobmatch.domain.*;
import ua.knu.jobmatch.repository.CompanyRepository;
import ua.knu.jobmatch.repository.PersonRepository;
import ua.knu.jobmatch.repository.UserAccountRepository;
import ua.knu.jobmatch.web.form.RegistrationForm;

/** Реєстрація (G2) і автентифікація (U1). */
@Service
public class AccountService implements UserDetailsService {
    private final UserAccountRepository accounts;
    private final PersonRepository people;
    private final CompanyRepository companies;
    private final PasswordEncoder encoder;

    public AccountService(UserAccountRepository accounts, PersonRepository people,
                          CompanyRepository companies, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.people = people;
        this.companies = companies;
        this.encoder = encoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        UserAccount a = accounts.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("not found"));
        return new AccountPrincipal(a);
    }

    @Transactional
    public UserAccount register(RegistrationForm f) {
        if (!f.getPassword().equals(f.getPasswordConfirm())) {
            throw new BusinessException("Паролі не збігаються.",
                    "Введіть однаковий пароль у поля «Пароль» і «Повторіть пароль».",
                    "Вмикайте показ пароля перед введенням, щоб уникнути описок.");
        }
        if (accounts.existsByEmailIgnoreCase(f.getEmail())) {
            throw new BusinessException("Обліковий запис з e-mail " + f.getEmail() + " вже існує.",
                    "Увійдіть з цим e-mail на сторінці «Вхід» або вкажіть інший e-mail.",
                    "Використовуйте один e-mail для одного облікового запису.");
        }
        if (f.getRole() == Role.RECRUITER) {
            throw new BusinessException("Самостійна реєстрація рекрутера недоступна.",
                    "Виберіть тип «Шукач роботи» або «Представник компанії».",
                    "Облікові записи рекрутерів створює адміністратор агенції.");
        }
        if (f.getRole() == Role.EMPLOYER && f.getCompanyName() != null && companies.existsByNameIgnoreCase(f.getCompanyName())) {
            throw new BusinessException("Компанія «" + f.getCompanyName() + "» вже зареєстрована.",
                    "Перевірте назву або зверніться до рекрутера агенції, щоб отримати доступ до профілю компанії.",
                    "Одна компанія має один профіль у JobMatch.");
        }
        UserAccount account = accounts.save(new UserAccount(f.getEmail().toLowerCase(), encoder.encode(f.getPassword()), f.getRole()));
        if (f.getRole() == Role.CANDIDATE) {
            requireText(f.getLastName(), "прізвище");
            requireText(f.getFirstName(), "ім'я");
            Person p = new Person(f.getLastName(), f.getFirstName(), f.getPhone(), account.getEmail(), f.getCity());
            p.setHomeAddress(f.getHomeAddress());
            p.setUser(account);
            people.save(p);
        } else {
            requireText(f.getCompanyName(), "назву компанії");
            if (companies.existsByNameIgnoreCase(f.getCompanyName())) {
                throw new BusinessException("Компанія «" + f.getCompanyName() + "» вже зареєстрована.",
                        "Перевірте назву або зверніться до рекрутера агенції, щоб отримати доступ до профілю компанії.",
                        "Одна компанія має один профіль у JobMatch.");
            }
            Company c = new Company();
            c.setName(f.getCompanyName());
            c.setCity(f.getCity());
            c.setAddress(f.getCompanyAddress());
            c.setPhone(f.getPhone());
            c.setEmail(f.getCompanyEmail());
            c.setWebsite(f.getWebsite());
            c.setDescription(f.getDescription());
            c.setOwner(account);
            companies.save(c);
        }
        return account;
    }

    private static void requireText(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Не вказано " + what + ".",
                    "Заповніть поле «" + what + "» і повторіть реєстрацію.",
                    "Поля, позначені *, обов'язкові.");
        }
    }
}
