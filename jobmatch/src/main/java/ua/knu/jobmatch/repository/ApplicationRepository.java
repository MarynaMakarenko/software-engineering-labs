package ua.knu.jobmatch.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.knu.jobmatch.domain.Application;
import ua.knu.jobmatch.domain.ApplicationStatus;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByVacancyIdAndPersonId(Long vacancyId, Long personId);

    Optional<Application> findByVacancyIdAndPersonId(Long vacancyId, Long personId);

    List<Application> findByVacancyId(Long vacancyId);

    @EntityGraph(attributePaths = {"person"})
    List<Application> findByVacancyIdOrderByCreatedAtDesc(Long vacancyId);

    @EntityGraph(attributePaths = {"vacancy", "vacancy.company"})
    List<Application> findByPersonIdOrderByCreatedAtDesc(Long personId);

    @EntityGraph(attributePaths = {"vacancy", "vacancy.company", "person"})
    List<Application> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"vacancy", "vacancy.company", "person"})
    List<Application> findByStatusOrderByCreatedAtDesc(ApplicationStatus status);

    long countByStatus(ApplicationStatus status);
}
