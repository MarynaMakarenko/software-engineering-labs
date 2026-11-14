package ua.knu.jobmatch.web.form;

import jakarta.validation.constraints.*;
import ua.knu.jobmatch.domain.EducationLevel;
import ua.knu.jobmatch.domain.Vacancy;

public class VacancyForm {
    @NotBlank(message = "Не вказано посаду. Введіть назву посади. Посада — головний критерій підбору.")
    @Size(min = 2, max = 100, message = "Назва посади має бути від 2 до 100 символів. Змініть назву. Вказуйте одну посаду на вакансію.")
    private String position;

    @NotNull(message = "Не вказано зарплату. Введіть суму від 1000 до 1 000 000 грн. Зарплата використовується в підборі.")
    @Min(value = 1000, message = "Зарплата менша за 1000 грн. Введіть суму від 1000 до 1 000 000 грн. Вказуйте суму за місяць до сплати податків.")
    @Max(value = 1_000_000, message = "Зарплата більша за 1 000 000 грн. Введіть суму від 1000 до 1 000 000 грн. Вказуйте суму за місяць, а не за рік.")
    private Integer salary;

    private EducationLevel requiredEducation;

    @NotNull(message = "Не вказано досвід. Введіть кількість років від 0 до 40. Для вакансії без досвіду введіть 0.")
    @Min(value = 0, message = "Досвід від’ємний. Введіть кількість років від 0 до 40. Для вакансії без досвіду введіть 0.")
    @Max(value = 40, message = "Досвід більший за 40 років. Введіть кількість років від 0 до 40. Вказуйте мінімально необхідний досвід.")
    private Integer requiredExperienceYears = 0;

    @Size(max = 2000, message = "Вимоги довші за 2000 символів. Скоротіть текст. Перелічуйте 5–7 ключових вимог.")
    private String requirements;

    @NotBlank(message = "Не вказано місто. Введіть місто роботи. Місто використовується в пошуку вакансій.")
    @Size(max = 60, message = "Назва міста довша за 60 символів. Скоротіть назву. Вказуйте лише населений пункт.")
    private String city;

    public static VacancyForm of(Vacancy v) {
        VacancyForm f = new VacancyForm();
        f.position = v.getPosition();
        f.salary = v.getSalary();
        f.requiredEducation = v.getRequiredEducation();
        f.requiredExperienceYears = v.getRequiredExperienceYears();
        f.requirements = v.getRequirements();
        f.city = v.getCity();
        return f;
    }

    public Vacancy toEntity() {
        return new Vacancy(position, salary, requiredEducation, requiredExperienceYears, requirements, city);
    }

    public void applyTo(Vacancy v) {
        v.setPosition(position);
        v.setSalary(salary);
        v.setRequiredEducation(requiredEducation);
        v.setRequiredExperienceYears(requiredExperienceYears);
        v.setRequirements(requirements);
        v.setCity(city);
    }

    public String getPosition() { return position; }
    public void setPosition(String v) { position = Forms.trim(v); }
    public Integer getSalary() { return salary; }
    public void setSalary(Integer v) { salary = v; }
    public EducationLevel getRequiredEducation() { return requiredEducation; }
    public void setRequiredEducation(EducationLevel v) { requiredEducation = v; }
    public Integer getRequiredExperienceYears() { return requiredExperienceYears; }
    public void setRequiredExperienceYears(Integer v) { requiredExperienceYears = v; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String v) { requirements = Forms.trim(v); }
    public String getCity() { return city; }
    public void setCity(String v) { city = Forms.trim(v); }
}
