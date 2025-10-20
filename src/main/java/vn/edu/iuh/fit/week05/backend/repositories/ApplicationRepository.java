package vn.edu.iuh.fit.week05.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.week05.backend.models.Application;
import vn.edu.iuh.fit.week05.backend.models.ApplicationStatus;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.Job;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Kiểm tra xem candidate đã apply job này chưa
     */
    boolean existsByCandidateAndJob(Candidate candidate, Job job);

    /**
     * Kiểm tra xem candidate đã apply job này chưa bằng IDs
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Application a WHERE a.candidate.id = :candidateId AND a.job.id = :jobId")
    boolean existsByCandidateIdAndJobId(@Param("candidateId") Long candidateId, @Param("jobId") Long jobId);

    /**
     * Tìm application của candidate cho một job cụ thể
     */
    Optional<Application> findByCandidateAndJob(Candidate candidate, Job job);

    /**
     * Tìm application bằng candidate ID và job ID
     * Dùng query này thay vì findByCandidateAndJob để tránh phải load entity
     */
    @Query("SELECT a FROM Application a WHERE a.candidate.id = :candidateId AND a.job.id = :jobId")
    Optional<Application> findByCandidateIdAndJobId(@Param("candidateId") Long candidateId, @Param("jobId") Long jobId);

    /**
     * Lấy tất cả applications của một candidate
     */
    List<Application> findByCandidateOrderByCreatedDateDesc(Candidate candidate);

    /**
     * Lấy tất cả applications cho một job
     */
    List<Application> findByJobOrderByCreatedDateDesc(Job job);

    /**
     * Lấy applications theo candidate và status
     */
    List<Application> findByCandidateAndStatusOrderByCreatedDateDesc(Candidate candidate, ApplicationStatus status);

    /**
     * Đếm số lượng applications của candidate theo status
     */
    long countByCandidateAndStatus(Candidate candidate, ApplicationStatus status);

    /**
     * Đếm tổng số applications của candidate
     */
    long countByCandidate(Candidate candidate);

    /**
     * Đếm số lượng applications cho một job theo status
     */
    long countByJobAndStatus(Job job, ApplicationStatus status);

    /**
     * Đếm tổng số applications cho một job
     */
    long countByJob(Job job);

    /**
     * Lấy tất cả applications của các jobs thuộc về một company
     */
    @Query("SELECT a FROM Application a WHERE a.job.company.id = :companyId ORDER BY a.createdDate DESC")
    List<Application> findByCompanyIdOrderByCreatedDateDesc(@Param("companyId") Long companyId);

    /**
     * Đếm applications cho company theo status
     */
    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.company.id = :companyId AND a.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status);

    /**
     * Thống kê applications theo status cho một candidate
     */
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId GROUP BY a.status")
    List<Object[]> getStatisticsByCandidateId(@Param("candidateId") Long candidateId);

    /**
     * Thống kê applications theo status cho một job
     */
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.id = :jobId GROUP BY a.status")
    List<Object[]> getStatisticsByJobId(@Param("jobId") Long jobId);

    /**
     * Thống kê applications theo status cho một company
     */
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.company.id = :companyId GROUP BY a.status")
    List<Object[]> getStatisticsByCompanyId(@Param("companyId") Long companyId);
}
