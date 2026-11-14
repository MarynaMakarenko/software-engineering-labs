package ua.knu.jobmatch.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "education")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id")
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EducationLevel level;

    @Column(nullable = false, length = 150)
    private String institution;

    @Column(nullable = false, length = 100)
    private String specialty;

    @Column(name = "graduation_year", nullable = false)
    private int graduationYear;

    protected Education() { }

    public Education(EducationLevel level, String institution, String specialty, int graduationYear) {
        this.level = level;
        this.institution = institution;
        this.specialty = specialty;
        this.graduationYear = graduationYear;
    }

    public Long getId() { return id; }
    public Person getPerson() { return person; }
    void setPerson(Person person) { this.person = person; }
    public EducationLevel getLevel() { return level; }
    public String getInstitution() { return institution; }
    public String getSpecialty() { return specialty; }
    public int getGraduationYear() { return graduationYear; }
}
