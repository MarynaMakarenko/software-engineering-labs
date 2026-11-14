package ua.knu.jobmatch.matching;

/** Складова оцінки відповідності: назва критерію, бали, максимум і пояснення. */
public record CriterionScore(String criterion, int points, int maxPoints, String explanation) {
    public boolean isFull() { return points == maxPoints; }
}
