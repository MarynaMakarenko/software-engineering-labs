package ua.knu.jobmatch.domain;

public enum Role {
    CANDIDATE("Шукач"), EMPLOYER("Представник компанії"), RECRUITER("Рекрутер");

    private final String label;

    Role(String label) { this.label = label; }

    public String getLabel() { return label; }
}
