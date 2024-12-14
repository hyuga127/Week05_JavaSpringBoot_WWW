package vn.edu.iuh.fit.week05.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.week05.backend.models.Job;

import java.util.List;
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByCompanyId(Long companyId, Pageable pageable);
    Job findByIdAndCompanyId(Long id, Long companyId);
}
