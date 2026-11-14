package ua.knu.jobmatch;

import org.springframework.test.util.ReflectionTestUtils;
import ua.knu.jobmatch.domain.*;

import java.time.LocalDate;

/** Фабрика тестових об'єктів для модульних тестів. */
public final class TestData {
    public static final LocalDate TODAY = LocalDate.of(2026, 11, 14);

    private TestData() { }

    public static Person person(long id, String desiredPosition, Integer desiredSalary) {
        Person p = new Person("Петренко", "Іван", "+380671234567", "ivan@example.com", "Київ");
        p.setDesiredPosition(desiredPosition);
        p.setDesiredSalary(desiredSalary);
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    public static Person withEducation(Person p, EducationLevel level) {
        p.addEducation(new Education(level, "КНУ", "Комп'ютерні науки", 2019));
        return p;
    }

    public static Person withWork(Person p, String position, LocalDate start, LocalDate end) {
        p.addWorkRecord(new WorkRecord("Компанія", position, 50000, start, end));
        return p;
    }

    public static Vacancy vacancy(long id, String position, int salary, EducationLevel edu, int years) {
        Company c = new Company();
        c.setName("ТехноСофт");
        ReflectionTestUtils.setField(c, "id", 1L);
        Vacancy v = c.addVacancy(new Vacancy(position, salary, edu, years, null, "Київ"));
        ReflectionTestUtils.setField(v, "id", id);
        return v;
    }
}
