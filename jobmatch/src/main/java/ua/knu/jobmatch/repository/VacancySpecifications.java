package ua.knu.jobmatch.repository;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.domain.VacancyStatus;
import ua.knu.jobmatch.web.form.VacancySearchCriteria;

import java.util.ArrayList;
import java.util.List;

/** Динамічні запити пошуку відкритих вакансій (G1). */
public final class VacancySpecifications {
    private VacancySpecifications() { }

    public static Specification<Vacancy> openMatching(VacancySearchCriteria c) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                Fetch<?, ?> f = root.fetch("company", JoinType.INNER);
            }
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), VacancyStatus.OPEN));
            if (c.getPosition() != null) {
                ps.add(cb.like(cb.lower(root.get("position")), PersonSpecifications.like(c.getPosition())));
            }
            if (c.getCompany() != null) {
                ps.add(cb.like(cb.lower(root.get("company").get("name")), PersonSpecifications.like(c.getCompany())));
            }
            if (c.getCity() != null) {
                ps.add(cb.like(cb.lower(root.get("city")), PersonSpecifications.like(c.getCity())));
            }
            if (c.getSalaryFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("salary"), c.getSalaryFrom()));
            }
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(ps.toArray(Predicate[]::new));
        };
    }
}
