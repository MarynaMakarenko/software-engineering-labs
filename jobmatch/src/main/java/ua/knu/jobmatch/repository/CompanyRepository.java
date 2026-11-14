package ua.knu.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.knu.jobmatch.domain.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByOwnerId(Long ownerId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Company> findAllByOrderByNameAsc();

    List<Company> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
