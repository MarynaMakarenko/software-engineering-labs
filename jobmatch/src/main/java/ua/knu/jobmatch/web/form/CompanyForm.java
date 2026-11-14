package ua.knu.jobmatch.web.form;

import jakarta.validation.constraints.*;
import ua.knu.jobmatch.domain.Company;

public class CompanyForm {
    @NotBlank(message = "Не вказано назву компанії. Введіть назву. Назва компанії має бути унікальною в JobMatch.")
    @Size(min = 2, max = 120, message = "Назва компанії має бути від 2 до 120 символів. Змініть назву. Вказуйте назву без організаційно-правової форми.")
    private String name;

    @NotBlank(message = "Не вказано місто. Введіть назву міста. Місто відображається в картці компанії.")
    @Size(max = 60, message = "Назва міста довша за 60 символів. Скоротіть назву. Вказуйте лише населений пункт.")
    private String city;

    @NotBlank(message = "Не вказано адресу. Введіть адресу офісу. Адреса потрібна кандидатам для співбесіди.")
    @Size(max = 200, message = "Адреса довша за 200 символів. Скоротіть адресу. Вказуйте вулицю й номер будинку.")
    private String address;

    @NotBlank(message = "Не вказано телефон. Введіть номер у форматі +380XXXXXXXXX. Телефон потрібен рекрутеру.")
    @Pattern(regexp = "^\\+380\\d{9}$", message = "Телефон має неправильний формат. Введіть номер у форматі +380XXXXXXXXX. Вводьте номер без пробілів і дужок.")
    private String phone;

    @NotBlank(message = "Не вказано e-mail. Введіть адресу у форматі hr@company.ua. E-mail потрібен для листування з агенцією.")
    @Email(message = "E-mail має неправильний формат. Введіть адресу у форматі hr@company.ua. Перевіряйте наявність «@» і домену.")
    @Size(max = 100, message = "E-mail довший за 100 символів. Скоротіть адресу. Використовуйте коротшу адресу.")
    private String email;

    @Size(max = 200, message = "Адреса сайту довша за 200 символів. Скоротіть адресу. Вказуйте головну сторінку сайту.")
    @Pattern(regexp = "^(https?://\\S+)?$", message = "Адреса сайту має неправильний формат. Введіть адресу, що починається з https://. Копіюйте адресу з рядка браузера.")
    private String website;

    @Size(max = 2000, message = "Опис довший за 2000 символів. Скоротіть опис. Пишіть про сферу діяльності та переваги роботи в компанії.")
    private String description;

    public static CompanyForm of(Company c) {
        CompanyForm f = new CompanyForm();
        f.name = c.getName();
        f.city = c.getCity();
        f.address = c.getAddress();
        f.phone = c.getPhone();
        f.email = c.getEmail();
        f.website = c.getWebsite();
        f.description = c.getDescription();
        return f;
    }

    public void applyTo(Company c) {
        c.setName(name);
        c.setCity(city);
        c.setAddress(address);
        c.setPhone(phone);
        c.setEmail(email);
        c.setWebsite(website);
        c.setDescription(description);
    }

    public String getName() { return name; }
    public void setName(String v) { name = Forms.trim(v); }
    public String getCity() { return city; }
    public void setCity(String v) { city = Forms.trim(v); }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = Forms.trim(v); }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = Forms.trim(v); }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = Forms.trim(v); }
    public String getWebsite() { return website; }
    public void setWebsite(String v) { website = Forms.trim(v); }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = Forms.trim(v); }
}
