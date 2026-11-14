package ua.knu.jobmatch.domain;

public enum JobStatus {
    SEARCHING("Шукає роботу"), NOT_SEARCHING("Не шукає");

    private final String label;

    JobStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
