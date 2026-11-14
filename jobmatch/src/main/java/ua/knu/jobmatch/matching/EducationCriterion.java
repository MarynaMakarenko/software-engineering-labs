package ua.knu.jobmatch.matching;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ua.knu.jobmatch.domain.EducationLevel;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;

import java.time.LocalDate;
import java.util.Optional;

/** Освіта: 15 — найвищий рівень людини не нижчий за вимогу (або вимоги немає), 0 — інакше. */
@Component
@Order(3)
public class EducationCriterion implements MatchCriterion {
    static final int MAX = 15;

    @Override
    public String name() { return "Освіта"; }

    @Override
    public int maxPoints() { return MAX; }

    @Override
    public CriterionScore evaluate(Person person, Vacancy vacancy, LocalDate today) {
        EducationLevel required = vacancy.getRequiredEducation();
        Optional<EducationLevel> highest = person.highestEducation();
        if (required == null) {
            return new CriterionScore(name(), MAX, MAX, "вимоги до освіти немає");
        }
        if (highest.isPresent() && highest.get().isAtLeast(required)) {
            return new CriterionScore(name(), MAX, MAX, highest.get().getLabel() + " ≥ " + required.getLabel());
        }
        String has = highest.map(EducationLevel::getLabel).orElse("не вказано");
        return new CriterionScore(name(), 0, MAX, has + " < " + required.getLabel());
    }
}
