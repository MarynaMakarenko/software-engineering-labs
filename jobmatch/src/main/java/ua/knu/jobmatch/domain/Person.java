package ua.knu.jobmatch.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Людина (резюме) — центральна сутність кадрової агенції. */
@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(nullable = false, length = 13)
    private String phone;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 60)
    private String city;

    @Column(name = "home_address", length = 200)
    private String homeAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false, length = 20)
    private JobStatus jobStatus = JobStatus.SEARCHING;

    @Column(name = "desired_position", length = 100)
    private String desiredPosition;

    @Column(name = "desired_salary")
    private Integer desiredSalary;

    @Column(length = 1000)
    private String about;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("graduationYear DESC")
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC")
    private List<WorkRecord> workRecords = new ArrayList<>();

    public Person() { }

    public Person(String lastName, String firstName, String phone, String email, String city) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.phone = phone;
        this.email = email;
        this.city = city;
    }

    public String getFullName() {
        return lastName + " " + firstName + (middleName == null || middleName.isBlank() ? "" : " " + middleName);
    }

    public boolean isSearching() { return jobStatus == JobStatus.SEARCHING; }

    /** Information Expert: найвищий рівень освіти людини. */
    public Optional<EducationLevel> highestEducation() {
        return educations.stream().map(Education::getLevel).max(Comparator.naturalOrder());
    }

    /** Information Expert: сумарний стаж у роках (з точністю до 0.1). */
    public double experienceYears(LocalDate today) {
        long months = workRecords.stream().mapToLong(w -> w.months(today)).sum();
        return Math.round(months / 12.0 * 10) / 10.0;
    }

    /** Стаж у форматі «X р. Y міс.» (FR-7). */
    public String experienceText(LocalDate today) {
        long months = workRecords.stream().mapToLong(w -> w.months(today)).sum();
        return (months / 12) + " р. " + (months % 12) + " міс.";
    }

    public void addEducation(Education education) {
        education.setPerson(this);
        educations.add(education);
        touch();
    }

    public void removeEducation(Education education) {
        educations.remove(education);
        touch();
    }

    public void addWorkRecord(WorkRecord record) {
        record.setPerson(this);
        workRecords.add(record);
        touch();
    }

    public void removeWorkRecord(WorkRecord record) {
        workRecords.remove(record);
        touch();
    }

    /** Реєстрація працевлаштування: завершує поточні записи, додає новий, знімає з пошуку. */
    public WorkRecord hire(Company company, String position, Integer salary, LocalDate startDate) {
        workRecords.stream().filter(WorkRecord::isCurrent).forEach(w -> w.finish(startDate));
        WorkRecord record = new WorkRecord(company.getName(), position, salary, startDate, null);
        record.setCompany(company);
        addWorkRecord(record);
        jobStatus = JobStatus.NOT_SEARCHING;
        return record;
    }

    public void touch() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getHomeAddress() { return homeAddress; }
    public void setHomeAddress(String homeAddress) { this.homeAddress = homeAddress; }
    public JobStatus getJobStatus() { return jobStatus; }
    public void setJobStatus(JobStatus jobStatus) { this.jobStatus = jobStatus; }
    public String getDesiredPosition() { return desiredPosition; }
    public void setDesiredPosition(String desiredPosition) { this.desiredPosition = desiredPosition; }
    public Integer getDesiredSalary() { return desiredSalary; }
    public void setDesiredSalary(Integer desiredSalary) { this.desiredSalary = desiredSalary; }
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<Education> getEducations() { return educations; }
    public List<WorkRecord> getWorkRecords() { return workRecords; }
}
