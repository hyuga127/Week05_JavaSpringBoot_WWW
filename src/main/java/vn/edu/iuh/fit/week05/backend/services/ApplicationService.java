package vn.edu.iuh.fit.week05.backend.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.week05.backend.models.Application;
import vn.edu.iuh.fit.week05.backend.models.ApplicationStatus;
import vn.edu.iuh.fit.week05.backend.models.Candidate;
import vn.edu.iuh.fit.week05.backend.models.Job;
import vn.edu.iuh.fit.week05.backend.repositories.ApplicationRepository;
import vn.edu.iuh.fit.week05.backend.repositories.CandidateRepository;
import vn.edu.iuh.fit.week05.backend.repositories.JobRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository, 
                            CandidateRepository candidateRepository, 
                            JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
    }

    /**
     * Ứng viên apply job
     * Kiểm tra xem đã apply chưa, nếu chưa thì tạo mới với status PENDING
     */
    @Transactional
    public Application applyJob(Long candidateId, Long jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with ID: " + jobId));

        // Kiểm tra xem đã apply chưa
        if (applicationRepository.existsByCandidateAndJob(candidate, job)) {
            throw new IllegalStateException("You have already applied for this job");
        }

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setCreatedDate(LocalDateTime.now());
        application.setStatus(ApplicationStatus.PENDING);

        return applicationRepository.save(application);
    }

    /**
     * Rút đơn ứng tuyển (withdraw)
     * Chỉ có thể withdraw khi status là PENDING hoặc REVIEWING
     */
    @Transactional
    public Application withdrawApplication(Long applicationId, Long candidateId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        // Kiểm tra quyền sở hữu
        if (!application.getCandidate().getId().equals(candidateId)) {
            throw new IllegalStateException("You don't have permission to withdraw this application");
        }

        // Chỉ có thể withdraw khi đang PENDING hoặc REVIEWING
        if (application.getStatus() != ApplicationStatus.PENDING && 
            application.getStatus() != ApplicationStatus.REVIEWING) {
            throw new IllegalStateException("Cannot withdraw application with status: " + application.getStatus());
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        return applicationRepository.save(application);
    }

    /**
     * Company cập nhật status của application
     */
    @Transactional
    public Application updateApplicationStatus(Long applicationId, Long companyId, ApplicationStatus newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        // Kiểm tra quyền: application phải thuộc về job của company này
        if (!application.getJob().getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("You don't have permission to update this application");
        }

        // Không cho phép thay đổi status của application đã WITHDRAWN
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot update status of withdrawn application");
        }

        application.setStatus(newStatus);
        return applicationRepository.save(application);
    }

    /**
     * Kiểm tra xem candidate đã apply job này chưa
     */
    public boolean hasApplied(Long candidateId, Long jobId) {
        return applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId);
    }

    /**
     * Lấy application của candidate cho một job
     * Sử dụng query by IDs để tránh phải tạo entity objects
     */
    public Optional<Application> getApplication(Long candidateId, Long jobId) {
        return applicationRepository.findByCandidateIdAndJobId(candidateId, jobId);
    }

    /**
     * Lấy tất cả applications của candidate
     */
    public List<Application> getCandidateApplications(Long candidateId) {
        Candidate candidate = new Candidate();
        candidate.setId(candidateId);
        return applicationRepository.findByCandidateOrderByCreatedDateDesc(candidate);
    }

    /**
     * Lấy applications của candidate theo status
     */
    public List<Application> getCandidateApplicationsByStatus(Long candidateId, ApplicationStatus status) {
        Candidate candidate = new Candidate();
        candidate.setId(candidateId);
        return applicationRepository.findByCandidateAndStatusOrderByCreatedDateDesc(candidate, status);
    }

    /**
     * Lấy tất cả applications cho một job
     */
    public List<Application> getJobApplications(Long jobId) {
        Job job = new Job();
        job.setId(jobId);
        return applicationRepository.findByJobOrderByCreatedDateDesc(job);
    }

    /**
     * Lấy tất cả applications của company
     */
    public List<Application> getCompanyApplications(Long companyId) {
        return applicationRepository.findByCompanyIdOrderByCreatedDateDesc(companyId);
    }

    /**
     * Thống kê applications của candidate
     * Trả về Map với key là ApplicationStatus, value là số lượng
     */
    public Map<ApplicationStatus, Long> getCandidateStatistics(Long candidateId) {
        Map<ApplicationStatus, Long> statistics = new HashMap<>();
        
        // Khởi tạo tất cả status với giá trị 0
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statistics.put(status, 0L);
        }

        // Lấy thống kê từ database
        List<Object[]> results = applicationRepository.getStatisticsByCandidateId(candidateId);
        for (Object[] result : results) {
            ApplicationStatus status = (ApplicationStatus) result[0];
            Long count = (Long) result[1];
            statistics.put(status, count);
        }

        return statistics;
    }

    /**
     * Thống kê applications cho một job
     */
    public Map<ApplicationStatus, Long> getJobStatistics(Long jobId) {
        Map<ApplicationStatus, Long> statistics = new HashMap<>();
        
        // Khởi tạo tất cả status với giá trị 0
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statistics.put(status, 0L);
        }

        // Lấy thống kê từ database
        List<Object[]> results = applicationRepository.getStatisticsByJobId(jobId);
        for (Object[] result : results) {
            ApplicationStatus status = (ApplicationStatus) result[0];
            Long count = (Long) result[1];
            statistics.put(status, count);
        }

        return statistics;
    }

    /**
     * Thống kê applications cho company
     */
    public Map<ApplicationStatus, Long> getCompanyStatistics(Long companyId) {
        Map<ApplicationStatus, Long> statistics = new HashMap<>();
        
        // Khởi tạo tất cả status với giá trị 0
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statistics.put(status, 0L);
        }

        // Lấy thống kê từ database
        List<Object[]> results = applicationRepository.getStatisticsByCompanyId(companyId);
        for (Object[] result : results) {
            ApplicationStatus status = (ApplicationStatus) result[0];
            Long count = (Long) result[1];
            statistics.put(status, count);
        }

        return statistics;
    }

    /**
     * Đếm tổng số applications của candidate
     */
    public long countCandidateApplications(Long candidateId) {
        Candidate candidate = new Candidate();
        candidate.setId(candidateId);
        return applicationRepository.countByCandidate(candidate);
    }

    /**
     * Đếm tổng số applications cho job
     */
    public long countJobApplications(Long jobId) {
        Job job = new Job();
        job.setId(jobId);
        return applicationRepository.countByJob(job);
    }

    /**
     * Lấy application by ID
     */
    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }
}
