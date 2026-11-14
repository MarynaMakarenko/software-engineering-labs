package ua.knu.jobmatch.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.knu.jobmatch.domain.Education;
import ua.knu.jobmatch.domain.Person;
import ua.knu.jobmatch.domain.WorkRecord;
import ua.knu.jobmatch.repository.PersonRepository;
import ua.knu.jobmatch.repository.UserAccountRepository;
import ua.knu.jobmatch.repository.PersonSpecifications;
import ua.knu.jobmatch.web.form.EducationForm;
import ua.knu.jobmatch.web.form.PersonForm;
import ua.knu.jobmatch.web.form.PersonSearchCriteria;
import ua.knu.jobmatch.web.form.WorkRecordForm;

import java.util.List;

/** Резюме, освіта та історія роботи (C1, C2, R1, R4, R5). */
@Service
@Transactional
public class PersonService {
    private final PersonRepository people;
    private final UserAccountRepository accounts;

    public PersonService(PersonRepository people, UserAccountRepository accounts) {
        this.people = people;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public Person get(Long id) {
        Person p = people.findById(id).orElseThrow(() -> new NotFoundException("Людину №" + id + " не знайдено"));
        p.getEducations().size();
        p.getWorkRecords().size();
        return p;
    }

    @Transactional(readOnly = true)
    public Person getByUser(Long userId) {
        Person p = people.findByUserId(userId).orElseThrow(() -> new NotFoundException("Резюме не знайдено"));
        p.getEducations().size();
        p.getWorkRecords().size();
        return p;
    }

    @Transactional(readOnly = true)
    public List<Person> search(PersonSearchCriteria c) {
        List<Person> found = people.findAll(PersonSpecifications.matching(c), Sort.by("lastName", "firstName"));
        found.forEach(p -> { p.getEducations().size(); p.getWorkRecords().size(); });
        return found;
    }

    public Person create(PersonForm f) {
        Person p = new Person();
        f.applyTo(p);
        return people.save(p);
    }

    public Person update(Long id, PersonForm f) {
        Person p = get(id);
        f.applyTo(p);
        return p;
    }

    public void delete(Long id) {
        Person p = get(id);
        ua.knu.jobmatch.domain.UserAccount account = p.getUser();
        people.delete(p);
        people.flush();
        if (account != null) {
            accounts.delete(account);
        }
    }

    public void addEducation(Long personId, EducationForm f) {
        get(personId).addEducation(f.toEntity());
    }

    public void removeEducation(Long personId, Long educationId) {
        Person p = get(personId);
        Education e = p.getEducations().stream().filter(x -> x.getId().equals(educationId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Запис освіти не знайдено"));
        p.removeEducation(e);
    }

    public WorkRecord addWorkRecord(Long personId, WorkRecordForm f) {
        WorkRecord w = new WorkRecord(f.getCompanyName(), f.getPosition(), f.getSalary(), f.getStartDate(), f.getEndDate());
        get(personId).addWorkRecord(w);
        return w;
    }

    public void updateWorkRecord(Long personId, Long recordId, WorkRecordForm f) {
        Person p = get(personId);
        WorkRecord w = findRecord(p, recordId);
        w.update(f.getCompanyName(), w.getCompany() != null && w.getCompany().getName().equalsIgnoreCase(f.getCompanyName())
                ? w.getCompany() : null, f.getPosition(), f.getSalary(), f.getStartDate(), f.getEndDate());
        p.touch();
    }

    public void deleteWorkRecord(Long personId, Long recordId) {
        Person p = get(personId);
        p.removeWorkRecord(findRecord(p, recordId));
    }

    @Transactional(readOnly = true)
    public WorkRecord getWorkRecord(Long personId, Long recordId) {
        return findRecord(get(personId), recordId);
    }

    private static WorkRecord findRecord(Person p, Long recordId) {
        return p.getWorkRecords().stream().filter(x -> x.getId().equals(recordId)).findFirst()
                .orElseThrow(() -> new NotFoundException("Запис історії роботи не знайдено"));
    }
}
