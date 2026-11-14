package ua.knu.jobmatch.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.knu.jobmatch.domain.Company;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.repository.ApplicationRepository;
import ua.knu.jobmatch.repository.CompanyRepository;
import ua.knu.jobmatch.repository.VacancyRepository;
import ua.knu.jobmatch.repository.VacancySpecifications;
import ua.knu.jobmatch.repository.UserAccountRepository;
import ua.knu.jobmatch.web.form.CompanyForm;
import ua.knu.jobmatch.web.form.VacancyForm;
import ua.knu.jobmatch.web.form.VacancySearchCriteria;

import java.util.List;

/** Компанії та вакансії (G1, E1, E2, R6). */
@Service
@Transactional
public class CompanyService {
    private final CompanyRepository companies;
    private final VacancyRepository vacancies;
    private final ApplicationRepository applications;
    private final UserAccountRepository accounts;

    public CompanyService(CompanyRepository companies, VacancyRepository vacancies, ApplicationRepository applications,
                          UserAccountRepository accounts) {
        this.companies = companies;
        this.vacancies = vacancies;
        this.applications = applications;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public Company get(Long id) {
        Company c = companies.findById(id).orElseThrow(() -> new NotFoundException("Компанію №" + id + " не знайдено"));
        c.getVacancies().size();
        if (c.getOwner() != null) {
            c.getOwner().getEmail();
        }
        return c;
    }

    @Transactional(readOnly = true)
    public Company getByOwner(Long userId) {
        Company c = companies.findByOwnerId(userId).orElseThrow(() -> new NotFoundException("Профіль компанії не знайдено"));
        c.getVacancies().size();
        return c;
    }

    @Transactional(readOnly = true)
    public List<Company> list(String query) {
        List<Company> list = query == null || query.isBlank() ? companies.findAllByOrderByNameAsc()
                : companies.findByNameContainingIgnoreCaseOrderByNameAsc(query.trim());
        list.forEach(c -> {
            c.getVacancies().size();
            if (c.getOwner() != null) {
                c.getOwner().getEmail();
            }
        });
        return list;
    }

    public Company create(CompanyForm f) {
        if (companies.existsByNameIgnoreCase(f.getName())) {
            throw duplicateName(f.getName());
        }
        Company c = new Company();
        f.applyTo(c);
        return companies.save(c);
    }

    public Company update(Long id, CompanyForm f) {
        if (companies.existsByNameIgnoreCaseAndIdNot(f.getName(), id)) {
            throw duplicateName(f.getName());
        }
        Company c = get(id);
        f.applyTo(c);
        return c;
    }

    public void delete(Long id) {
        Company c = get(id);
        c.getVacancies().forEach(v -> applications.deleteAll(applications.findByVacancyId(v.getId())));
        ua.knu.jobmatch.domain.UserAccount owner = c.getOwner();
        companies.delete(c);
        companies.flush();
        if (owner != null) {
            accounts.delete(owner);
        }
    }

    /** Кількість кандидатур на вакансії компанії — для тексту підтвердження видалення (FR-5). */
    @Transactional(readOnly = true)
    public long applicationCount(Company c) {
        return c.getVacancies().stream().mapToLong(v -> applications.findByVacancyId(v.getId()).size()).sum();
    }

    // ---- вакансії ----

    @Transactional(readOnly = true)
    public Vacancy getVacancy(Long id) {
        Vacancy v = vacancies.findById(id).orElseThrow(() -> new NotFoundException("Вакансію №" + id + " не знайдено"));
        v.getCompany().getName();
        return v;
    }

    @Transactional(readOnly = true)
    public List<Vacancy> searchOpen(VacancySearchCriteria c) {
        return vacancies.findAll(VacancySpecifications.openMatching(c));
    }

    @Transactional(readOnly = true)
    public List<Vacancy> latestOpen() {
        return vacancies.findTop6ByStatusOrderByCreatedAtDesc(ua.knu.jobmatch.domain.VacancyStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public List<Vacancy> allOpen() {
        return vacancies.findByStatusOrderByCreatedAtDesc(ua.knu.jobmatch.domain.VacancyStatus.OPEN);
    }

    public Vacancy addVacancy(Long companyId, VacancyForm f) {
        Company c = get(companyId);
        Vacancy v = c.addVacancy(f.toEntity());
        companies.flush();
        return v;
    }

    public Vacancy updateVacancy(Long id, VacancyForm f) {
        Vacancy v = getVacancy(id);
        f.applyTo(v);
        return v;
    }

    public void setVacancyOpen(Long id, boolean open) {
        Vacancy v = getVacancy(id);
        if (open) {
            v.reopen();
        } else {
            v.close();
        }
    }

    public void deleteVacancy(Long id) {
        Vacancy v = getVacancy(id);
        applications.deleteAll(applications.findByVacancyId(id));
        v.getCompany().getVacancies().remove(v);
    }

    private static BusinessException duplicateName(String name) {
        return new BusinessException("Компанія з назвою «" + name + "» вже існує.",
                "Змініть назву (наприклад, додайте місто) або відкрийте наявну компанію через пошук.",
                "Перед додаванням компанії перевіряйте список «Компанії».");
    }
}
