package ua.knu.jobmatch.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.knu.jobmatch.TestData.*;

class PersonTest {
    @Test
    void experienceSumsRecords() {
        Person p = person(1, null, null);
        withWork(p, "A", LocalDate.of(2019, 7, 1), LocalDate.of(2022, 3, 31));   // 32 міс.
        withWork(p, "B", LocalDate.of(2022, 4, 1), null);                        // до 14.11.2026 = 55 міс.
        assertThat(p.experienceText(TODAY)).isEqualTo("7 р. 3 міс.");
        assertThat(p.experienceYears(TODAY)).isEqualTo(7.3);
    }

    @Test
    void noWorkGivesZero() {
        assertThat(person(1, null, null).experienceText(TODAY)).isEqualTo("0 р. 0 міс.");
    }

    @Test
    void highestEducationEmptyWhenNone() {
        assertThat(person(1, null, null).highestEducation()).isEmpty();
    }

    @Test
    void hireFinishesCurrentJob() {
        Person p = withWork(person(1, null, null), "Old", LocalDate.of(2020, 1, 1), null);
        Vacancy v = vacancy(1, "New", 50000, null, 0);
        p.hire(v.getCompany(), "New", 50000, TODAY);
        assertThat(p.getWorkRecords()).hasSize(2).filteredOn(WorkRecord::isCurrent).singleElement()
                .extracting(WorkRecord::getPosition).isEqualTo("New");
        assertThat(p.isSearching()).isFalse();
    }

    @Test
    void finalApplicationStatusIsImmutable() {
        Application a = new Application(vacancy(1, "X", 1000, null, 0), person(1, null, null), ApplicationSource.CANDIDATE, null);
        a.changeStatus(ApplicationStatus.REJECTED);
        assertThatThrownBy(() -> a.changeStatus(ApplicationStatus.INVITED)).isInstanceOf(IllegalStateException.class);
    }
}
