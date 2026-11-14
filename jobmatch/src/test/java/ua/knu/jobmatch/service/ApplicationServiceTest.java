package ua.knu.jobmatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ua.knu.jobmatch.domain.*;
import ua.knu.jobmatch.repository.ApplicationRepository;
import ua.knu.jobmatch.repository.PersonRepository;
import ua.knu.jobmatch.repository.VacancyRepository;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ua.knu.jobmatch.TestData.*;

/** Модульні тести ApplicationService.propose (R2 кроки 5–7, A3, A4, A5, A7) і hire (R7). */
class ApplicationServiceTest {
    private ApplicationRepository applications;
    private VacancyRepository vacancies;
    private PersonRepository people;
    private ApplicationService service;
    private Vacancy vacancy;
    private Person person;

    @BeforeEach
    void setUp() {
        applications = mock(ApplicationRepository.class);
        vacancies = mock(VacancyRepository.class);
        people = mock(PersonRepository.class);
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.of("Europe/Kyiv")).toInstant(), ZoneId.of("Europe/Kyiv"));
        service = new ApplicationService(applications, vacancies, people, clock);
        vacancy = vacancy(1, "Java-розробник", 85000, EducationLevel.BACHELOR, 3);
        person = person(3, "Java-розробник", 80000);
        when(vacancies.findById(1L)).thenReturn(Optional.of(vacancy));
        when(people.findById(3L)).thenReturn(Optional.of(person));
        when(applications.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void proposeWithoutComment() {
        Application a = service.propose(1L, 3L, null);
        assertThat(a.getStatus()).isEqualTo(ApplicationStatus.NEW);
        assertThat(a.getSource()).isEqualTo(ApplicationSource.RECRUITER);
        assertThat(a.getComment()).isNull();
        verify(applications).save(any());
    }

    @Test
    void proposeWithOneCharComment() {
        assertThat(service.propose(1L, 3L, "a").getComment()).isEqualTo("a");
    }

    @Test
    void proposeWithMaxComment() {
        String c500 = "к".repeat(500);
        assertThat(service.propose(1L, 3L, c500).getComment()).hasSize(500);
    }

    @Test
    void commentOver500Rejected() {   // A5
        assertThatThrownBy(() -> service.propose(1L, 3L, "к".repeat(501)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("500");
        verify(applications, never()).save(any());
    }

    @Test
    void duplicateRejected() {   // A3
        when(applications.existsByVacancyIdAndPersonId(1L, 3L)).thenReturn(true);
        assertThatThrownBy(() -> service.propose(1L, 3L, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("вже існує");
        verify(applications, never()).save(any());
    }

    @Test
    void closedVacancyRejected() {   // A4
        vacancy.close();
        assertThatThrownBy(() -> service.propose(1L, 3L, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("закрито");
    }

    @Test
    void missingVacancyIsNotFound() {   // A7
        when(vacancies.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.propose(99L, 3L, null)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void missingPersonIsNotFound() {   // A7
        when(people.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.propose(1L, 99L, null)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void hireRequiresInvitedStatus() {
        Application a = new Application(vacancy, person, ApplicationSource.RECRUITER, null);
        ReflectionTestUtils.setField(a, "id", 7L);
        when(applications.findById(7L)).thenReturn(Optional.of(a));
        assertThatThrownBy(() -> service.hire(7L, TODAY)).isInstanceOf(BusinessException.class).hasMessageContaining("Запрошено");
    }

    @Test
    void hireClosesVacancyAndAddsWorkRecord() {
        Application a = new Application(vacancy, person, ApplicationSource.RECRUITER, null);
        a.changeStatus(ApplicationStatus.INVITED);
        when(applications.findById(7L)).thenReturn(Optional.of(a));
        service.hire(7L, TODAY.plusDays(90));
        assertThat(a.getStatus()).isEqualTo(ApplicationStatus.HIRED);
        assertThat(vacancy.isOpen()).isFalse();
        assertThat(person.getJobStatus()).isEqualTo(JobStatus.NOT_SEARCHING);
        assertThat(person.getWorkRecords()).singleElement().satisfies(w -> {
            assertThat(w.getPosition()).isEqualTo("Java-розробник");
            assertThat(w.isCurrent()).isTrue();
        });
    }

    @Test
    void hireDateAfter90DaysRejected() {
        Application a = new Application(vacancy, person, ApplicationSource.RECRUITER, null);
        a.changeStatus(ApplicationStatus.INVITED);
        when(applications.findById(7L)).thenReturn(Optional.of(a));
        assertThatThrownBy(() -> service.hire(7L, TODAY.plusDays(91))).isInstanceOf(BusinessException.class);
        assertThat(vacancy.isOpen()).isTrue();
    }
}
