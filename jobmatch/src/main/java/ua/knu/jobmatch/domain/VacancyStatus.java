package ua.knu.jobmatch.domain;

public enum VacancyStatus {
    OPEN("Відкрита"), CLOSED("Закрита");

    private final String label;

    VacancyStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
