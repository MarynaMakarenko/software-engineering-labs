package ua.knu.jobmatch.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ua.knu.jobmatch.domain.Vacancy;
import ua.knu.jobmatch.domain.VacancyStatus;

import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Long>, JpaSpecificationExecutor<Vacancy> {
    @EntityGraph(attributePaths = {"company"})
    List<Vacancy> findByStatusOrderByCreatedAtDesc(VacancyStatus status);

    @EntityGraph(attributePaths = {"company"})
    List<Vacancy> findTop6ByStatusOrderByCreatedAtDesc(VacancyStatus status);

    List<Vacancy> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    long countByStatus(VacancyStatus status);
}
