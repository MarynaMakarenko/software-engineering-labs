package ua.knu.jobmatch.domain;

public enum ApplicationSource {
    CANDIDATE("Відгук шукача"), RECRUITER("Пропозиція рекрутера");

    private final String label;

    ApplicationSource(String label) { this.label = label; }

    public String getLabel() { return label; }
}
