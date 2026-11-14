package ua.knu.jobmatch.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private UserAccount owner;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, length = 60)
    private String city;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 13)
    private String phone;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 200)
    private String website;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Vacancy> vacancies = new ArrayList<>();

    public Company() { }

    public Long getId() { return id; }
    public UserAccount getOwner() { return owner; }
    public void setOwner(UserAccount owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Vacancy> getVacancies() { return vacancies; }

    /** Creator (GRASP): компанія створює власні вакансії. */
    public Vacancy addVacancy(Vacancy vacancy) {
        vacancy.setCompany(this);
        vacancies.add(vacancy);
        return vacancy;
    }

    public long getOpenVacancyCount() {
        return vacancies.stream().filter(Vacancy::isOpen).count();
    }
}
