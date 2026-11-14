package ua.knu.jobmatch.domain;

/** Рівні освіти у порядку зростання. */
public enum EducationLevel {
    SECONDARY("Середня"), VOCATIONAL("Професійно-технічна"), BACHELOR("Бакалавр"), MASTER("Магістр"), PHD("Доктор філософії");

    private final String label;

    EducationLevel(String label) { this.label = label; }

    public String getLabel() { return label; }

    public boolean isAtLeast(EducationLevel other) {
        return other == null || compareTo(other) >= 0;
    }
}
