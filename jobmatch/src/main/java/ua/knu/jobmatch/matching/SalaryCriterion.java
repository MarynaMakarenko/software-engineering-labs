package ua.knu.jobmatch.matching;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;

import java.time.LocalDate;

/** Зарплата: 15 — бажана ≤ запропонованої або не вказана, 7 — ≤ 115 %, 0 — інакше. */
@Component
@Order(2)
public class SalaryCriterion implements MatchCriterion {
    static final int MAX = 15;
    static final int PARTIAL = 7;

    @Override
    public String name() { return "Зарплата"; }

    @Override
    public int maxPoints() { return MAX; }

    @Override
    public CriterionScore evaluate(Person person, Vacancy vacancy, LocalDate today) {
        Integer desired = person.getDesiredSalary();
        int offered = vacancy.getSalary();
        if (desired == null) {
            return new CriterionScore(name(), MAX, MAX, "бажану зарплату не вказано");
        }
        if (desired <= offered) {
            return new CriterionScore(name(), MAX, MAX, desired + " ≤ " + offered + " грн");
        }
        if (desired * 100L <= offered * 115L) {
            return new CriterionScore(name(), PARTIAL, MAX, desired + " грн — до 15 % вище за " + offered);
        }
        return new CriterionScore(name(), 0, MAX, desired + " грн — більш ніж на 15 % вище за " + offered);
    }
}
