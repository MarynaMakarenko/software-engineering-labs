package ua.knu.jobmatch.matching;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;

import java.time.LocalDate;

/** Досвід: 15 — стаж ≥ вимоги, 7 — ≥ половини вимоги, 0 — інакше. */
@Component
@Order(4)
public class ExperienceCriterion implements MatchCriterion {
    static final int MAX = 15;
    static final int PARTIAL = 7;

    @Override
    public String name() { return "Досвід"; }

    @Override
    public int maxPoints() { return MAX; }

    @Override
    public CriterionScore evaluate(Person person, Vacancy vacancy, LocalDate today) {
        double years = person.experienceYears(today);
        int required = vacancy.getRequiredExperienceYears();
        if (years >= required) {
            return new CriterionScore(name(), MAX, MAX, years + " р. ≥ " + required + " р.");
        }
        if (years * 2 >= required) {
            return new CriterionScore(name(), PARTIAL, MAX, years + " р. — не менше половини з " + required + " р.");
        }
        return new CriterionScore(name(), 0, MAX, years + " р. < " + required + " р.");
    }
}
