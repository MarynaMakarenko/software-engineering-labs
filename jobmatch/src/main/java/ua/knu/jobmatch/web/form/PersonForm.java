package ua.knu.jobmatch.web.form;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import ua.knu.jobmatch.domain.JobStatus;
import ua.knu.jobmatch.domain.Person;

import java.time.LocalDate;

public class PersonForm {
    static final String NAME = "^[\\p{L}][\\p{L}'’ -]*$";

    @NotBlank(message = "Не вказано прізвище. Введіть прізвище кирилицею або латиницею. Прізвище обов’язкове для резюме.")
    @Size(max = 50, message = "Прізвище довше за 50 символів. Скоротіть прізвище. Вводьте прізвище без ініціалів.")
    @Pattern(regexp = NAME, message = "Прізвище містить недопустимі символи. Залиште лише літери, апостроф, дефіс і пробіл. Не вводьте цифри та знаки пунктуації.")
    private String lastName;

    @NotBlank(message = "Не вказано ім’я. Введіть ім’я. Ім’я обов’язкове для резюме.")
    @Size(max = 50, message = "Ім’я довше за 50 символів. Скоротіть ім’я. Вводьте лише ім’я.")
    @Pattern(regexp = NAME, message = "Ім’я містить недопустимі символи. Залиште лише літери, апостроф, дефіс і пробіл. Не вводьте цифри та знаки пунктуації.")
    private String firstName;

    @Size(max = 50, message = "По батькові довше за 50 символів. Скоротіть значення. Поле необов’язкове — його можна залишити порожнім.")
    @Pattern(regexp = NAME, message = "По батькові містить недопустимі символи. Залиште лише літери, апостроф і дефіс. Не вводьте цифри.")
    private String middleName;

    @Past(message = "Дата народження не в минулому. Вкажіть реальну дату народження. Перевіряйте рік у календарі.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    @NotBlank(message = "Не вказано телефон. Введіть номер у форматі +380XXXXXXXXX. Телефон потрібен для зв’язку рекрутера.")
    @Pattern(regexp = "^\\+380\\d{9}$", message = "Телефон має неправильний формат. Введіть номер у форматі +380XXXXXXXXX (9 цифр після +380). Вводьте номер без пробілів і дужок.")
    private String phone;

    @NotBlank(message = "Не вказано e-mail. Введіть адресу у форматі name@example.com. E-mail потрібен для зв’язку.")
    @Email(message = "E-mail має неправильний формат. Введіть адресу у форматі name@example.com. Перевіряйте наявність «@» і домену.")
    @Size(max = 100, message = "E-mail довший за 100 символів. Скоротіть адресу. Використовуйте коротшу адресу.")
    private String email;

    @NotBlank(message = "Не вказано місто. Введіть назву міста, наприклад «Київ». Місто використовується в підборі.")
    @Size(max = 60, message = "Назва міста довша за 60 символів. Скоротіть назву. Вказуйте лише назву населеного пункту.")
    private String city;

    @Size(max = 200, message = "Домашня адреса довша за 200 символів. Скоротіть адресу. Вказуйте вулицю, будинок і квартиру без зайвих пояснень.")
    private String homeAddress;

    @NotNull(message = "Не вибрано статус пошуку. Виберіть «Шукає роботу» або «Не шукає». Статус визначає участь резюме в підборі.")
    private JobStatus jobStatus = JobStatus.SEARCHING;

    @Size(max = 100, message = "Бажана посада довша за 100 символів. Скоротіть назву посади. Вказуйте одну посаду.")
    private String desiredPosition;

    @Min(value = 1000, message = "Бажана зарплата менша за 1000 грн. Введіть суму від 1000 до 1 000 000 грн або залиште поле порожнім. Вказуйте суму за місяць до сплати податків.")
    @Max(value = 1_000_000, message = "Бажана зарплата більша за 1 000 000 грн. Введіть суму від 1000 до 1 000 000 грн. Вказуйте суму за місяць, а не за рік.")
    private Integer desiredSalary;

    @Size(max = 1000, message = "Поле «Про себе» довше за 1000 символів. Скоротіть текст. Пишіть 3–5 речень про ключові навички.")
    private String about;

    public static PersonForm of(Person p) {
        PersonForm f = new PersonForm();
        f.lastName = p.getLastName();
        f.firstName = p.getFirstName();
        f.middleName = p.getMiddleName();
        f.birthDate = p.getBirthDate();
        f.phone = p.getPhone();
        f.email = p.getEmail();
        f.city = p.getCity();
        f.homeAddress = p.getHomeAddress();
        f.jobStatus = p.getJobStatus();
        f.desiredPosition = p.getDesiredPosition();
        f.desiredSalary = p.getDesiredSalary();
        f.about = p.getAbout();
        return f;
    }

    public void applyTo(Person p) {
        p.setLastName(lastName);
        p.setFirstName(firstName);
        p.setMiddleName(middleName);
        p.setBirthDate(birthDate);
        p.setPhone(phone);
        p.setEmail(email);
        p.setCity(city);
        p.setHomeAddress(homeAddress);
        p.setJobStatus(jobStatus);
        p.setDesiredPosition(desiredPosition);
        p.setDesiredSalary(desiredSalary);
        p.setAbout(about);
        p.touch();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String v) { lastName = Forms.trim(v); }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { firstName = Forms.trim(v); }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String v) { middleName = Forms.trim(v); }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate v) { birthDate = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = Forms.trim(v); }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = Forms.trim(v); }
    public String getCity() { return city; }
    public void setCity(String v) { city = Forms.trim(v); }
    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String v) { homeAddress = Forms.trim(v); }
    public JobStatus getJobStatus() { return jobStatus; }
    public void setJobStatus(JobStatus v) { jobStatus = v; }
    public String getDesiredPosition() { return desiredPosition; }
    public void setDesiredPosition(String v) { desiredPosition = Forms.trim(v); }
    public Integer getDesiredSalary() { return desiredSalary; }
    public void setDesiredSalary(Integer v) { desiredSalary = v; }
    public String getAbout() { return about; }
    public void setAbout(String v) { about = Forms.trim(v); }
}
