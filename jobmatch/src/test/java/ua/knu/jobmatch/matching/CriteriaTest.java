package ua.knu.jobmatch.matching;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ua.knu.jobmatch.domain.EducationLevel;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.knu.jobmatch.TestData.*;

/** Модульні тести критеріїв M1: еквівалентні класи + граничні значення. */
class CriteriaTest {

    @Nested
    class Position {
        private final PositionCriterion c = new PositionCriterion();

        @ParameterizedTest(name = "[{index}] бажана «{0}» vs вакансія «{1}» → {2}")
        @CsvSource(delimiter = '|', value = {
                "Java-розробник|Java-розробник|55",          // точний збіг (типовий)
                "JAVA-РОЗРОБНИК|java-розробник|55",          // регістр
                "Java|Java-розробник|55",                    // одна містить іншу (мінімальна назва)
                "Розробник ПЗ|Java-розробник|25",            // спільне слово ≥ 4
                "Java-тестувальник|Java-розробник|25",       // спільне слово «java» = 4 символи (межа)
                "Агроном|Java-розробник|0",                  // немає збігу
                "QA|QA-інженер|55",                          // коротка назва — ціле слово
                "ІТ|Архітектор рішень|0"                     // «іт» всередині слова не є збігом
        })
        void desiredPosition(String desired, String vacancyPosition, int expected) {
            Person p = person(1, desired, null);
            Vacancy v = vacancy(1, vacancyPosition, 50000, null, 0);
            assertThat(c.evaluate(p, v, TODAY).points()).isEqualTo(expected);
        }

        @Test
        void wordShorterThanFourIsIgnored() {
            // «Head» має 4 літери → враховується; «PM» — ні
            assertThat(PositionCriterion.compare("pm lead", "pm assistant")).isZero();
        }

        @Test
        void positionFromWorkHistoryCounts() {
            Person p = withWork(person(1, null, null), "Java-розробник", LocalDate.of(2020, 1, 1), null);
            assertThat(c.evaluate(p, vacancy(1, "Java-розробник", 1000, null, 0), TODAY).points()).isEqualTo(55);
        }

        @Test
        void noPositionsGivesZero() {
            assertThat(c.evaluate(person(1, null, null), vacancy(1, "Логіст", 1000, null, 0), TODAY).points()).isZero();
        }
    }

    @Nested
    class Salary {
        private final SalaryCriterion c = new SalaryCriterion();

        @ParameterizedTest(name = "[{index}] бажана {0} при {1} → {2}")
        @CsvSource({
                "1000, 1000, 15",        // мінімальні, рівні (межа ≤)
                "85000, 85000, 15",      // рівні
                "85001, 85000, 7",       // трохи вище
                "97750, 85000, 7",       // рівно 115 % (межа)
                "97751, 85000, 0",       // 115 % + 1
                "1000000, 1000, 0"       // максимальна бажана
        })
        void desiredVsOffered(int desired, int offered, int expected) {
            assertThat(c.evaluate(person(1, null, desired), vacancy(1, "X", offered, null, 0), TODAY).points())
                    .isEqualTo(expected);
        }

        @Test
        void notSpecifiedGivesFull() {
            assertThat(c.evaluate(person(1, null, null), vacancy(1, "X", 5000, null, 0), TODAY).points()).isEqualTo(15);
        }
    }

    @Nested
    class Education {
        private final EducationCriterion c = new EducationCriterion();

        @ParameterizedTest(name = "[{index}] має {0}, вимога {1} → {2}")
        @CsvSource({
                "BACHELOR, BACHELOR, 15",
                "MASTER, BACHELOR, 15",
                "PHD, PHD, 15",
                "SECONDARY, VOCATIONAL, 0",
                "BACHELOR, MASTER, 0"
        })
        void levels(EducationLevel has, EducationLevel required, int expected) {
            Person p = withEducation(person(1, null, null), has);
            assertThat(c.evaluate(p, vacancy(1, "X", 1000, required, 0), TODAY).points()).isEqualTo(expected);
        }

        @Test
        void noRequirementGivesFullEvenWithoutEducation() {
            assertThat(c.evaluate(person(1, null, null), vacancy(1, "X", 1000, null, 0), TODAY).points()).isEqualTo(15);
        }

        @Test
        void noEducationWithRequirementGivesZero() {
            assertThat(c.evaluate(person(1, null, null), vacancy(1, "X", 1000, EducationLevel.SECONDARY, 0), TODAY).points()).isZero();
        }

        @Test
        void highestLevelIsUsed() {
            Person p = withEducation(withEducation(person(1, null, null), EducationLevel.BACHELOR), EducationLevel.MASTER);
            assertThat(c.evaluate(p, vacancy(1, "X", 1000, EducationLevel.MASTER, 0), TODAY).points()).isEqualTo(15);
        }
    }

    @Nested
    class Experience {
        private final ExperienceCriterion c = new ExperienceCriterion();

        @Test
        void zeroRequiredZeroExperience() {
            assertThat(c.evaluate(person(1, null, null), vacancy(1, "X", 1000, null, 0), TODAY).points()).isEqualTo(15);
        }

        @Test
        void exactlyRequired() {
            Person p = withWork(person(1, null, null), "X", TODAY.minusYears(3), TODAY);
            assertThat(c.evaluate(p, vacancy(1, "X", 1000, null, 3), TODAY).points()).isEqualTo(15);
        }

        @Test
        void halfRequiredBoundary() {
            Person p = withWork(person(1, null, null), "X", TODAY.minusMonths(18), TODAY);
            assertThat(c.evaluate(p, vacancy(1, "X", 1000, null, 3), TODAY).points()).isEqualTo(7);
        }

        @Test
        void belowHalf() {
            Person p = withWork(person(1, null, null), "X", TODAY.minusMonths(17), TODAY);
            assertThat(c.evaluate(p, vacancy(1, "X", 1000, null, 3), TODAY).points()).isZero();
        }

        @Test
        void currentJobCountsUntilToday() {
            Person p = withWork(person(1, null, null), "X", TODAY.minusYears(40), null);
            assertThat(c.evaluate(p, vacancy(1, "X", 1000, null, 40), TODAY).points()).isEqualTo(15);
        }
    }
}
