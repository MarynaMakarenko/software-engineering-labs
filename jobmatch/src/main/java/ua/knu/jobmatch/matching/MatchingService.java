package ua.knu.jobmatch.matching;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.knu.jobmatch.domain.JobStatus;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.domain.VacancyStatus;
import ua.knu.jobmatch.repository.PersonRepository;
import ua.knu.jobmatch.repository.VacancyRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Facade (GoF) + Pure Fabrication (GRASP): оцінювання відповідності (UC M1)
 * та двосторонній підбір «вакансія → резюме» (R2, E3) і «резюме → вакансії» (R3, C3).
 */
@Service
@Transactional(readOnly = true)
public class MatchingService {
    public static final int DEFAULT_THRESHOLD = 50;
    public static final int PARTIAL_THRESHOLD = 25;

    private final List<MatchCriterion> criteria;
    private final PersonRepository personRepository;
    private final VacancyRepository vacancyRepository;
    private final Clock clock;

    public MatchingService(List<MatchCriterion> criteria, PersonRepository personRepository,
                           VacancyRepository vacancyRepository, Clock clock) {
        this.criteria = criteria;
        this.personRepository = personRepository;
        this.vacancyRepository = vacancyRepository;
        this.clock = clock;
    }

    public MatchResult evaluate(Person person, Vacancy vacancy) {
        LocalDate today = LocalDate.now(clock);
        List<CriterionScore> parts = criteria.stream().map(c -> c.evaluate(person, vacancy, today)).toList();
        int score = parts.stream().mapToInt(CriterionScore::points).sum();
        return new MatchResult(person, vacancy, score, parts);
    }

    /** Кандидати зі статусом «шукає роботу» з оцінкою ≥ minScore, за спаданням оцінки. */
    public List<MatchResult> findCandidates(Vacancy vacancy, int minScore) {
        validateThreshold(minScore);
        List<Person> searching = personRepository.findByJobStatus(JobStatus.SEARCHING);
        if (!searching.isEmpty()) {
            personRepository.fetchWorkRecords(searching);
        }
        return searching.stream()
                .map(p -> evaluate(p, vacancy))
                .filter(r -> r.score() >= minScore)
                .sorted(Comparator.comparingInt(MatchResult::score).reversed()
                        .thenComparing(r -> r.person().getLastName()))
                .toList();
    }

    /** Відкриті вакансії з оцінкою ≥ minScore для людини, за спаданням оцінки. */
    public List<MatchResult> findVacancies(Person person, int minScore) {
        validateThreshold(minScore);
        return vacancyRepository.findByStatusOrderByCreatedAtDesc(VacancyStatus.OPEN).stream()
                .map(v -> evaluate(person, v))
                .filter(r -> r.score() >= minScore)
                .sorted(Comparator.comparingInt(MatchResult::score).reversed()
                        .thenComparing(r -> r.vacancy().getPosition()))
                .toList();
    }

    public static void validateThreshold(int minScore) {
        if (minScore < 0 || minScore > 100) {
            throw new IllegalArgumentException("minScore must be in 0..100");
        }
    }
}
