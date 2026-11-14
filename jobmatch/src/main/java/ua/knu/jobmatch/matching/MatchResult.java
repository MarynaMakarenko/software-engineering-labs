package ua.knu.jobmatch.matching;

import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;

import java.util.List;

/** Результат оцінювання пари «резюме — вакансія». */
public record MatchResult(Person person, Vacancy vacancy, int score, List<CriterionScore> parts) {
}
