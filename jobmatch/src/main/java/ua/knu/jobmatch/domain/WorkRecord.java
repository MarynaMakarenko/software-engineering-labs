package ua.knu.jobmatch.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "work_record")
public class WorkRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "company_name", nullable = false, length = 120)
    private String companyName;

    @Column(nullable = false, length = 100)
    private String position;

    private Integer salary;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    protected WorkRecord() { }

    public WorkRecord(String companyName, String position, Integer salary, LocalDate startDate, LocalDate endDate) {
        this.companyName = companyName;
        this.position = position;
        this.salary = salary;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isCurrent() { return endDate == null; }

    /** Information Expert: запис сам знає свою тривалість у місяцях. */
    public long months(LocalDate today) {
        LocalDate end = endDate == null ? today : endDate;
        if (end.isBefore(startDate)) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(startDate, end);
    }

    public void update(String companyName, Company company, String position, Integer salary,
                       LocalDate startDate, LocalDate endDate) {
        this.companyName = companyName;
        this.company = company;
        this.position = position;
        this.salary = salary;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void finish(LocalDate date) { this.endDate = date; }

    public Long getId() { return id; }
    public Person getPerson() { return person; }
    void setPerson(Person person) { this.person = person; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public String getCompanyName() { return companyName; }
    public String getPosition() { return position; }
    public Integer getSalary() { return salary; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
