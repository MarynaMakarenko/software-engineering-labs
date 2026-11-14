package ua.knu.jobmatch.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacancy")
public class Vacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false, length = 100)
    private String position;

    @Column(nullable = false)
    private int salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_education", length = 20)
    private EducationLevel requiredEducation;

    @Column(name = "required_experience_years", nullable = false)
    private int requiredExperienceYears;

    @Column(length = 2000)
    private String requirements;

    @Column(nullable = false, length = 60)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VacancyStatus status = VacancyStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Vacancy() { }

    public Vacancy(String position, int salary, EducationLevel requiredEducation, int requiredExperienceYears,
                   String requirements, String city) {
        this.position = position;
        this.salary = salary;
        this.requiredEducation = requiredEducation;
        this.requiredExperienceYears = requiredExperienceYears;
        this.requirements = requirements;
        this.city = city;
    }

    public boolean isOpen() { return status == VacancyStatus.OPEN; }

    public void close() { status = VacancyStatus.CLOSED; }

    public void reopen() { status = VacancyStatus.OPEN; }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    void setCompany(Company company) { this.company = company; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public int getSalary() { return salary; }
    public void setSalary(int salary) { this.salary = salary; }
    public EducationLevel getRequiredEducation() { return requiredEducation; }
    public void setRequiredEducation(EducationLevel requiredEducation) { this.requiredEducation = requiredEducation; }
    public int getRequiredExperienceYears() { return requiredExperienceYears; }
    public void setRequiredExperienceYears(int requiredExperienceYears) { this.requiredExperienceYears = requiredExperienceYears; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public VacancyStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
