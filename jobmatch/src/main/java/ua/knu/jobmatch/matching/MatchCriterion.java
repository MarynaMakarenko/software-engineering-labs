package ua.knu.jobmatch.matching;

import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;

import java.time.LocalDate;

/** Strategy (GoF): окремий критерій оцінювання відповідності резюме й вакансії. */
public interface MatchCriterion {
    String name();

    int maxPoints();

    CriterionScore evaluate(Person person, Vacancy vacancy, LocalDate today);
}
