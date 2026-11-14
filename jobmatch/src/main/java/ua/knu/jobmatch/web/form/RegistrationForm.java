package ua.knu.jobmatch.web.form;

import jakarta.validation.constraints.*;
import ua.knu.jobmatch.domain.Role;

public class RegistrationForm {
    @NotNull(message = "Не вибрано тип облікового запису. Виберіть «Шукач роботи» або «Представник компанії». Тип запису визначає доступні розділи.")
    private Role role = Role.CANDIDATE;

    @NotBlank(message = "Не вказано e-mail. Введіть адресу у форматі name@example.com. Використовуйте адресу, до якої маєте доступ.")
    @Email(message = "E-mail має неправильний формат. Введіть адресу у форматі name@example.com. Перевіряйте наявність «@» і домену.")
    @Size(max = 100, message = "E-mail довший за 100 символів. Скоротіть адресу. Використовуйте коротшу робочу адресу.")
    private String email;

    @NotBlank(message = "Не вказано пароль. Введіть пароль від 8 до 64 символів. Зберігайте пароль у менеджері паролів.")
    @Size(min = 8, max = 64, message = "Пароль має містити від 8 до 64 символів. Введіть довший (або коротший) пароль. Не використовуйте прості паролі на кшталт «12345678».")
    private String password;

    @NotBlank(message = "Не повторено пароль. Введіть пароль ще раз у поле «Повторіть пароль». Повтор захищає від описок.")
    private String passwordConfirm;

    // шукач
    @Size(max = 50, message = "Прізвище довше за 50 символів. Скоротіть прізвище. Вводьте прізвище без ініціалів.")
    private String lastName;

    @Size(max = 50, message = "Ім’я довше за 50 символів. Скоротіть ім’я. Вводьте лише ім’я.")
    private String firstName;

    @Size(max = 200, message = "Домашня адреса довша за 200 символів. Скоротіть адресу. Вказуйте вулицю, будинок і квартиру.")
    private String homeAddress;

    private boolean consent;

    // компанія
    @Size(max = 200, message = "Адреса компанії довша за 200 символів. Скоротіть адресу. Вказуйте вулицю й номер будинку.")
    private String companyAddress;

    @Email(message = "E-mail компанії має неправильний формат. Введіть адресу у форматі hr@company.ua. Перевіряйте наявність «@» і домену.")
    @Size(max = 100, message = "E-mail компанії довший за 100 символів. Скоротіть адресу. Використовуйте коротшу адресу.")
    private String companyEmail;

    @Size(max = 200, message = "Адреса сайту довша за 200 символів. Скоротіть адресу. Вказуйте головну сторінку сайту.")
    @Pattern(regexp = "^(https?://\\S+)?$", message = "Адреса сайту має неправильний формат. Введіть адресу, що починається з https://. Копіюйте адресу з рядка браузера.")
    private String website;

    @Size(max = 2000, message = "Опис компанії довший за 2000 символів. Скоротіть опис. Пишіть про сферу діяльності компанії.")
    private String description;

    @AssertTrue(message = "Не вказано прізвище, ім’я або домашню адресу шукача. Заповніть поля «Прізвище», «Ім’я» і «Домашня адреса». Поля, позначені *, обов’язкові.")
    public boolean isCandidateDataProvided() {
        return role != Role.CANDIDATE || (lastName != null && firstName != null && homeAddress != null);
    }

    @AssertTrue(message = "Не надано згоду на обробку персональних даних. Позначте поле згоди, щоб створити резюме. Без згоди агенція не може зберігати резюме (Закон України «Про захист персональних даних»).")
    public boolean isConsentGiven() {
        return role != Role.CANDIDATE || consent;
    }

    @AssertTrue(message = "Не вказано назву, адресу або e-mail компанії. Заповніть поля «Назва компанії», «Адреса компанії» і «E-mail компанії». Поля, позначені *, обов’язкові.")
    public boolean isCompanyDataProvided() {
        return role != Role.EMPLOYER || (companyName != null && companyAddress != null && companyEmail != null);
    }

    @Size(max = 120, message = "Назва компанії довша за 120 символів. Скоротіть назву. Вказуйте назву без організаційно-правової форми.")
    private String companyName;

    @NotBlank(message = "Не вказано телефон. Введіть номер у форматі +380XXXXXXXXX. Телефон потрібен для зв’язку рекрутера.")
    @Pattern(regexp = "^\\+380\\d{9}$", message = "Телефон має неправильний формат. Введіть номер у форматі +380XXXXXXXXX (9 цифр після +380). Вводьте номер без пробілів і дужок.")
    private String phone;

    @NotBlank(message = "Не вказано місто. Введіть назву міста, наприклад «Київ». Місто використовується в пошуку вакансій.")
    @Size(max = 60, message = "Назва міста довша за 60 символів. Скоротіть назву. Вказуйте лише назву населеного пункту.")
    private String city;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim(); }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = Forms.trim(lastName); }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = Forms.trim(firstName); }
    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String v) { homeAddress = Forms.trim(v); }
    public boolean isConsent() { return consent; }
    public void setConsent(boolean consent) { this.consent = consent; }
    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String v) { companyAddress = Forms.trim(v); }
    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String v) { companyEmail = Forms.trim(v); }
    public String getWebsite() { return website; }
    public void setWebsite(String v) { website = Forms.trim(v); }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = Forms.trim(v); }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = Forms.trim(companyName); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = Forms.trim(phone); }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = Forms.trim(city); }
}
