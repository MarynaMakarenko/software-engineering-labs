package ua.knu.jobmatch.web.form;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import ua.knu.jobmatch.domain.WorkRecord;

import java.time.LocalDate;

public class WorkRecordForm {
    @NotBlank(message = "Не вказано компанію. Введіть назву компанії-роботодавця. Вказуйте назву так, як вона зазначена в трудовій книжці.")
    @Size(max = 120, message = "Назва компанії довша за 120 символів. Скоротіть назву. Вказуйте назву без організаційно-правової форми.")
    private String companyName;

    @NotBlank(message = "Не вказано посаду. Введіть назву посади. Посада використовується в підборі вакансій.")
    @Size(max = 100, message = "Посада довша за 100 символів. Скоротіть назву посади. Вказуйте одну посаду на запис.")
    private String position;

    @Min(value = 0, message = "Зарплата від’ємна. Введіть суму від 0 до 1 000 000 грн або залиште поле порожнім. Вказуйте суму за місяць.")
    @Max(value = 1_000_000, message = "Зарплата більша за 1 000 000 грн. Введіть суму за місяць від 0 до 1 000 000 грн. Не вказуйте річну суму.")
    private Integer salary;

    @NotNull(message = "Не вказано дату початку роботи. Виберіть дату в календарі. Дата потрібна для розрахунку стажу.")
    @PastOrPresent(message = "Дата початку роботи в майбутньому. Вкажіть дату не пізніше сьогоднішньої. Майбутні працевлаштування реєструє рекрутер.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @PastOrPresent(message = "Дата завершення роботи в майбутньому. Вкажіть дату не пізніше сьогоднішньої або залиште поле порожнім для поточної роботи. Порожнє поле означає «досі».")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @AssertTrue(message = "Дата завершення раніша за дату початку. Виправте одну з дат. Перевіряйте послідовність дат перед збереженням.")
    public boolean isDatesOrdered() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    public static WorkRecordForm of(WorkRecord w) {
        WorkRecordForm f = new WorkRecordForm();
        f.companyName = w.getCompanyName();
        f.position = w.getPosition();
        f.salary = w.getSalary();
        f.startDate = w.getStartDate();
        f.endDate = w.getEndDate();
        return f;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String v) { companyName = Forms.trim(v); }
    public String getPosition() { return position; }
    public void setPosition(String v) { position = Forms.trim(v); }
    public Integer getSalary() { return salary; }
    public void setSalary(Integer v) { salary = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { startDate = v; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate v) { endDate = v; }
}
