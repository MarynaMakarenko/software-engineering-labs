package ua.knu.jobmatch.web.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProposeForm {
    @NotNull(message = "Не вибрано кандидата. Натисніть «Запропонувати» в рядку кандидата. Пропонуйте кандидатів зі списку підбору.")
    private Long personId;

    @Size(max = 500, message = "Коментар довший за 500 символів. Скоротіть коментар до 500 символів. Пишіть 2–3 речення про сильні сторони кандидата.")
    private String comment;

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = Forms.trim(comment); }
}
