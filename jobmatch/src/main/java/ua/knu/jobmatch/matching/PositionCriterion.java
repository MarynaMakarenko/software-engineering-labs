package ua.knu.jobmatch.matching;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.domain.WorkRecord;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Посада: 55 — збіг (назва однієї посади входить в іншу цілими словами), 25 — спільне слово ≥ 4 літер, 0 — інакше. Без збігу посади оцінка не перевищує 45 < 50. */
@Component
@Order(1)
public class PositionCriterion implements MatchCriterion {
    static final int MAX = 55;
    static final int PARTIAL = 25;

    @Override
    public String name() { return "Посада"; }

    @Override
    public int maxPoints() { return MAX; }

    @Override
    public CriterionScore evaluate(Person person, Vacancy vacancy, LocalDate today) {
        String target = normalize(vacancy.getPosition());
        int best = 0;
        String matched = null;
        for (String candidate : candidatePositions(person)) {
            int points = compare(normalize(candidate), target);
            if (points > best) {
                best = points;
                matched = candidate;
            }
        }
        String explanation = best == MAX ? "збіг посади: «" + matched + "»"
                : best == PARTIAL ? "частковий збіг: «" + matched + "»"
                : "посада не збігається";
        return new CriterionScore(name(), best, MAX, explanation);
    }

    static int compare(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        String wa = " " + String.join(" ", tokens(a)) + " ";
        String wb = " " + String.join(" ", tokens(b)) + " ";
        if (wa.contains(wb) || wb.contains(wa)) {
            return MAX;
        }
        Set<String> wordsA = words(a);
        Set<String> wordsB = words(b);
        wordsA.retainAll(wordsB);
        return wordsA.isEmpty() ? 0 : PARTIAL;
    }

    /** Слова назви посади (розділювачі — пробіли, дефіси, розділові знаки). */
    private static java.util.List<String> tokens(String s) {
        return Arrays.stream(s.split("[^\\p{L}\\p{N}+#']+")).filter(w -> !w.isEmpty()).toList();
    }

    private static Set<String> words(String s) {
        return tokens(s).stream().filter(w -> w.length() >= 4).collect(Collectors.toSet());
    }

    static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replace('’', '\'');
    }

    private static Stream<String> candidatePositionsStream(Person person) {
        return Stream.concat(Stream.ofNullable(person.getDesiredPosition()),
                person.getWorkRecords().stream().map(WorkRecord::getPosition));
    }

    private static Iterable<String> candidatePositions(Person person) {
        return candidatePositionsStream(person).filter(p -> !p.isBlank()).toList();
    }
}
