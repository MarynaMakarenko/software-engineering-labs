package ua.knu.jobmatch.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ua.knu.jobmatch.domain.JobStatus;
import ua.knu.jobmatch.domain.Person;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
    Optional<Person> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"educations"})
    List<Person> findByJobStatus(JobStatus status);

    @Query("select distinct p from Person p left join fetch p.workRecords where p in :people")
    List<Person> fetchWorkRecords(List<Person> people);
}
