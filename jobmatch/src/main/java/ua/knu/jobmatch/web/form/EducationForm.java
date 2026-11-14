package ua.knu.jobmatch.web.form;

import jakarta.validation.constraints.*;
import ua.knu.jobmatch.domain.Education;
import ua.knu.jobmatch.domain.EducationLevel;

public class EducationForm {
    @NotNull(message = "Не вибрано рівень освіти. Виберіть рівень зі списку. Рівень освіти використовується в підборі.")
    private EducationLevel level;

    @NotBlank(message = "Не вказано навчальний заклад. Введіть назву закладу. Вказуйте офіційну назву.")
    @Size(max = 150, message = "Назва закладу довша за 150 символів. Скоротіть назву. Використовуйте загальноприйняту абревіатуру.")
    private String institution;

    @NotBlank(message = "Не вказано спеціальність. Введіть спеціальність з диплома. Спеціальність допомагає рекрутеру оцінити резюме.")
    @Size(max = 100, message = "Спеціальність довша за 100 символів. Скоротіть назву. Вказуйте назву з диплома без коду.")
    private String specialty;

    @NotNull(message = "Не вказано рік закінчення. Введіть рік від 1950 до 2035. Для незавершеного навчання вкажіть очікуваний рік.")
    @Min(value = 1950, message = "Рік закінчення раніший за 1950. Введіть рік від 1950 до 2035. Перевіряйте рік у дипломі.")
    @Max(value = 2035, message = "Рік закінчення пізніший за 2035. Введіть рік від 1950 до 2035. Вказуйте очікуваний рік закінчення.")
    private Integer graduationYear;

    public Education toEntity() {
        return new Education(level, institution, specialty, graduationYear);
    }

    public EducationLevel getLevel() { return level; }
    public void setLevel(EducationLevel level) { this.level = level; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = Forms.trim(institution); }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = Forms.trim(specialty); }
    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }
}
