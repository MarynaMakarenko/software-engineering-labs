package ua.knu.jobmatch.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ua.knu.jobmatch.domain.EducationLevel;
import ua.knu.jobmatch.domain.JobStatus;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.domain.VacancyStatus;
import ua.knu.jobmatch.repository.PersonRepository;
import ua.knu.jobmatch.repository.VacancyRepository;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static ua.knu.jobmatch.TestData.*;

/** Модульні тести MatchingService (R2 кроки 2–3, A1, A2) з mock-репозиторіями. */
class MatchingServiceTest {
    private PersonRepository people;
    private VacancyRepository vacancies;
    private MatchingService service;
    private final Vacancy java = vacancy(1, "Java-розробник", 85000, EducationLevel.BACHELOR, 3);

    @BeforeEach
    void setUp() {
        people = mock(PersonRepository.class);
        vacancies = mock(VacancyRepository.class);
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.of("Europe/Kyiv")).toInstant(), ZoneId.of("Europe/Kyiv"));
        service = new MatchingService(List.of(new PositionCriterion(), new SalaryCriterion(), new EducationCriterion(),
                new ExperienceCriterion()), people, vacancies, clock);
    }

    private Person ideal() {
        Person p = withEducation(person(3, "Java-розробник", 80000), EducationLevel.BACHELOR);
        return withWork(p, "Java-розробник", TODAY.minusYears(5), null);
    }

    @Test
    void evaluateSumsAllCriteria() {
        MatchResult r = service.evaluate(ideal(), java);
        assertThat(r.score()).isEqualTo(100);
        assertThat(r.parts()).hasSize(4).extracting(CriterionScore::points).containsExactly(55, 15, 15, 15);
    }

    @Test
    void candidateWithoutPositionMatchStaysBelowDefaultThreshold() {
        Person agronom = withWork(withEducation(person(8, "Агроном", 30000), EducationLevel.MASTER), "Агроном", TODAY.minusYears(10), null);
        assertThat(service.evaluate(agronom, java).score()).isEqualTo(45).isLessThan(MatchingService.DEFAULT_THRESHOLD);
    }

    @Test
    void findCandidatesFiltersByThresholdAndSortsDesc() {
        Person best = ideal();
        Person partial = withEducation(person(4, "Java-розробник", 95000), EducationLevel.MASTER); // 55+7+15+0 = 77
        Person weak = person(5, "Агроном", null);                                                  // 0+15+0+0 = 15
        when(people.findByJobStatus(JobStatus.SEARCHING)).thenReturn(List.of(weak, partial, best));
        List<MatchResult> res = service.findCandidates(java, 50);
        assertThat(res).extracting(MatchResult::score).containsExactly(100, 77);
        verify(people).fetchWorkRecords(anyList());
    }

    @Test
    void emptyListWhenNobodyReachesThreshold() {   // A1
        when(people.findByJobStatus(JobStatus.SEARCHING)).thenReturn(List.of(person(5, "Агроном", null)));
        assertThat(service.findCandidates(java, 50)).isEmpty();
        assertThat(service.findCandidates(java, 0)).hasSize(1);
    }

    @Test
    void noSearchingPeopleSkipsFetch() {
        when(people.findByJobStatus(JobStatus.SEARCHING)).thenReturn(List.of());
        assertThat(service.findCandidates(java, 50)).isEmpty();
        verify(people, never()).fetchWorkRecords(anyList());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 50, 99, 100})
    void validThresholds(int t) {
        when(people.findByJobStatus(JobStatus.SEARCHING)).thenReturn(List.of(ideal()));
        assertThat(service.findCandidates(java, t)).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101, Integer.MIN_VALUE, Integer.MAX_VALUE})
    void invalidThresholdsRejected(int t) {   // A2 некоректні
        assertThatThrownBy(() -> service.findCandidates(java, t)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findVacanciesUsesOnlyOpen() {
        Vacancy qa = vacancy(2, "QA-інженер", 45000, null, 1);
        when(vacancies.findByStatusOrderByCreatedAtDesc(VacancyStatus.OPEN)).thenReturn(List.of(qa, java));
        List<MatchResult> res = service.findVacancies(ideal(), 50);
        assertThat(res).extracting(r -> r.vacancy().getId()).containsExactly(1L);
    }
}
