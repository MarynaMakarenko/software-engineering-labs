package ua.knu.jobmatch.domain;

public enum ApplicationStatus {
    NEW("Нова"), INVITED("Запрошено"), REJECTED("Відхилено"), HIRED("Працевлаштовано");

    private final String label;

    ApplicationStatus(String label) { this.label = label; }

    public String getLabel() { return label; }

    public boolean isFinal() { return this == REJECTED || this == HIRED; }
}
