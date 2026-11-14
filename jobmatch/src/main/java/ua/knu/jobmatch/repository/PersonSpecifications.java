package ua.knu.jobmatch.repository;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import ua.knu.jobmatch.domain.*;
import ua.knu.jobmatch.web.form.PersonSearchCriteria;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Динамічні запити пошуку резюме (R1) — параметризовані, без конкатенації SQL. */
public final class PersonSpecifications {
    private PersonSpecifications() { }

    public static Specification<Person> matching(PersonSearchCriteria c) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> ps = new ArrayList<>();
            if (c.getName() != null) {
                String like = like(c.getName());
                ps.add(cb.or(cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("firstName")), like)));
            }
            if (c.getStatus() != null) {
                ps.add(cb.equal(root.get("jobStatus"), c.getStatus()));
            }
            if (c.getSalaryFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("desiredSalary"), c.getSalaryFrom()));
            }
            if (c.getSalaryTo() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("desiredSalary"), c.getSalaryTo()));
            }
            if (c.getMinEducation() != null) {
                Subquery<Long> sq = query.subquery(Long.class);
                Root<Education> e = sq.from(Education.class);
                List<EducationLevel> levels = Arrays.stream(EducationLevel.values())
                        .filter(l -> l.isAtLeast(c.getMinEducation())).toList();
                sq.select(e.get("id")).where(cb.equal(e.get("person"), root), e.get("level").in(levels));
                ps.add(cb.exists(sq));
            }
            if (c.getCompany() != null) {
                Subquery<Long> sq = query.subquery(Long.class);
                Root<WorkRecord> w = sq.from(WorkRecord.class);
                sq.select(w.get("id")).where(cb.equal(w.get("person"), root),
                        cb.like(cb.lower(w.get("companyName")), like(c.getCompany())));
                ps.add(cb.exists(sq));
            }
            if (c.getPosition() != null) {
                String like = like(c.getPosition());
                Subquery<Long> sq = query.subquery(Long.class);
                Root<WorkRecord> w = sq.from(WorkRecord.class);
                sq.select(w.get("id")).where(cb.equal(w.get("person"), root),
                        cb.like(cb.lower(w.get("position")), like));
                ps.add(cb.or(cb.like(cb.lower(root.get("desiredPosition")), like), cb.exists(sq)));
            }
            return cb.and(ps.toArray(Predicate[]::new));
        };
    }

    static String like(String s) {
        String escaped = s.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
