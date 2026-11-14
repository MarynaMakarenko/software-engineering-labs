package ua.knu.jobmatch.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Кандидатура: зв'язок людини з вакансією (відгук шукача або пропозиція рекрутера). */
@Entity
@Table(name = "application", uniqueConstraints = @UniqueConstraint(columnNames = {"vacancy_id", "person_id"}))
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id")
    private Vacancy vacancy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.NEW;

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Application() { }

    public Application(Vacancy vacancy, Person person, ApplicationSource source, String comment) {
        this.vacancy = vacancy;
        this.person = person;
        this.source = source;
        this.comment = comment;
    }

    public void changeStatus(ApplicationStatus newStatus) {
        if (status.isFinal()) {
            throw new IllegalStateException("Final status cannot be changed");
        }
        this.status = newStatus;
    }

    public Long getId() { return id; }
    public Vacancy getVacancy() { return vacancy; }
    public Person getPerson() { return person; }
    public ApplicationSource getSource() { return source; }
    public ApplicationStatus getStatus() { return status; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
