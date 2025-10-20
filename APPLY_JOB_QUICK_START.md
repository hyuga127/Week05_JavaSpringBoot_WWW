# 🚀 Quick Start Guide - Apply Job Feature

## Tổng quan nhanh

Chức năng Apply Job đã được triển khai đầy đủ cho hệ thống tuyển dụng với 2 vai trò chính:

### 👤 Ứng viên (Candidate)

✅ Xem danh sách jobs phù hợp  
✅ Apply job (1 lần/job)  
✅ Withdraw application  
✅ Xem thống kê và lịch sử applications

### 🏢 Công ty (Company)

✅ Xem danh sách ứng viên đã apply  
✅ Quản lý trạng thái applications  
✅ Xem thống kê theo job và tổng thể

---

## 🗂️ Cấu trúc Code

### Backend

```
backend/
├── models/
│   ├── Application.java              // Entity chính
│   └── ApplicationStatus.java        // ENUM (PENDING, REVIEWING, ACCEPTED, REJECTED, WITHDRAWN)
├── repositories/
│   └── ApplicationRepository.java    // Data access với custom queries
├── services/
│   └── ApplicationService.java       // Business logic
```

### Frontend Controllers

```
frontend/controllers/
├── ApplicationController.java        // Xử lý apply, withdraw, view applications
├── JobViewController.java            // Hiển thị job detail
└── CompanyController.java            // Quản lý applicants (đã update)
```

### Views (Thymeleaf)

```
templates/
├── job-detail.html                   // Chi tiết job + nút Apply
├── candidate/
│   └── applications.html             // Dashboard applications của candidate
└── company/
    ├── job-applicants.html           // Applicants của 1 job cụ thể
    └── applications.html             // Tất cả applications của company
```

---

## 🎯 Các URL chính

### Candidate URLs

| URL                              | Method | Mô tả                      |
| -------------------------------- | ------ | -------------------------- |
| `/job/{id}`                      | GET    | Xem chi tiết job và apply  |
| `/applications/apply?jobId={id}` | POST   | Apply job                  |
| `/applications/my-applications`  | GET    | Xem danh sách applications |
| `/applications/{id}/withdraw`    | POST   | Rút đơn ứng tuyển          |

### Company URLs

| URL                                                | Method | Mô tả                    |
| -------------------------------------------------- | ------ | ------------------------ |
| `/company/job/{jobId}/applicants`                  | GET    | Xem applicants của 1 job |
| `/company/applications`                            | GET    | Xem tất cả applications  |
| `/applications/{id}/update-status?status={STATUS}` | POST   | Update trạng thái        |

---

## 🔄 Flow chính

### 1. Apply Job Flow

```
Candidate → View Job Detail → Click "Apply"
→ POST /applications/apply
→ ApplicationService.applyJob()
→ Create Application (status: PENDING)
→ Redirect back với success message
```

### 2. Withdraw Flow

```
Candidate → My Applications → Click "Withdraw"
→ Confirm
→ POST /applications/{id}/withdraw
→ ApplicationService.withdrawApplication()
→ Update status to WITHDRAWN
→ Redirect với success message
```

### 3. Company Update Status Flow

```
Company → Job Applicants → Click "Review/Accept/Reject"
→ POST /applications/{id}/update-status
→ ApplicationService.updateApplicationStatus()
→ Update status
→ Redirect với success message
```

---

## 📊 Application Status Lifecycle

```
        ┌─────────┐
        │ PENDING │ (Mới apply)
        └────┬────┘
             │
             ├─────────────┐
             │             │
             ▼             ▼
     ┌──────────┐    ┌──────────┐
     │REVIEWING │    │WITHDRAWN │ (Candidate rút đơn)
     └────┬─────┘    └──────────┘
          │
          ├──────────┐
          │          │
          ▼          ▼
    ┌─────────┐ ┌─────────┐
    │ACCEPTED │ │REJECTED │ (Final states)
    └─────────┘ └─────────┘
```

---

## 💡 Các tính năng chính

### 1. Validation & Security

- ✅ Không cho apply trùng (unique constraint)
- ✅ Chỉ withdraw khi PENDING/REVIEWING
- ✅ Không update application đã WITHDRAWN
- ✅ Kiểm tra quyền ownership

### 2. Statistics & Reporting

- ✅ Đếm số lượng theo từng status
- ✅ Biểu đồ trực quan (Chart.js)
- ✅ Thống kê theo candidate/job/company

### 3. UI/UX

- ✅ Bootstrap 5 components
- ✅ Color-coded status badges
- ✅ Responsive design
- ✅ Real-time alerts
- ✅ Confirmation dialogs

---

## 🎨 Status Colors

| Status    | Color     | Badge Class    |
| --------- | --------- | -------------- |
| PENDING   | 🟡 Yellow | `bg-warning`   |
| REVIEWING | 🔵 Blue   | `bg-info`      |
| ACCEPTED  | 🟢 Green  | `bg-success`   |
| REJECTED  | 🔴 Red    | `bg-danger`    |
| WITHDRAWN | ⚫ Gray   | `bg-secondary` |

---

## 🗃️ Database

### Application Table

```sql
CREATE TABLE application (
    app_id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL,
    app_status VARCHAR(20) NOT NULL,

    CONSTRAINT uq_candidate_job UNIQUE (candidate_id, job_id),
    CONSTRAINT fk_candidate FOREIGN KEY (candidate_id) REFERENCES candidate(can_id),
    CONSTRAINT fk_job FOREIGN KEY (job_id) REFERENCES job(job_id)
);
```

### Indexes

```sql
CREATE INDEX idx_application_candidate ON application(candidate_id);
CREATE INDEX idx_application_job ON application(job_id);
CREATE INDEX idx_application_status ON application(app_status);
CREATE INDEX idx_application_created_date ON application(created_date DESC);
```

---

## 🧪 Test Scenarios

### Candidate Tests

```java
✅ Apply job successfully
✅ Cannot apply twice to same job
✅ Withdraw PENDING application
✅ Withdraw REVIEWING application
❌ Cannot withdraw ACCEPTED application
❌ Cannot withdraw REJECTED application
✅ View my applications
✅ View statistics
```

### Company Tests

```java
✅ View applicants for my job
✅ Update PENDING → REVIEWING
✅ Update REVIEWING → ACCEPTED
✅ Update REVIEWING → REJECTED
❌ Cannot update WITHDRAWN application
❌ Cannot update other company's application
✅ View all applications
✅ View statistics
```

---

## 🎯 Usage Examples

### Candidate - Apply Job

1. Login as candidate
2. Go to `/candidate/home`
3. Click "View Details" on a job
4. Click "Apply for this Job" button
5. ✅ Success message appears

### Candidate - View Applications

1. Click "Applications" in navbar
2. Or go to `/applications/my-applications`
3. See dashboard with:
   - Statistics cards
   - Chart (Doughnut)
   - Applications table

### Company - Manage Applications

1. Login as company
2. Go to `/company/home`
3. Click "Applicants" on any job
4. See:
   - Statistics
   - Chart (Bar)
   - Applicants table with action buttons
5. Click "Review", "Accept", or "Reject"

---

## 🔧 Configuration

### application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db
spring.datasource.username=your_user
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Thymeleaf
spring.thymeleaf.cache=false
```

---

## 📦 Dependencies (build.gradle)

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.postgresql:postgresql'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

---

## 🚀 Build & Run

```bash
# Build
./gradlew clean build

# Run
./gradlew bootRun

# Access
http://localhost:8080
```

---

## 📝 Code Examples

### Apply Job (Service)

```java
@Transactional
public Application applyJob(Long candidateId, Long jobId) {
    Candidate candidate = candidateRepository.findById(candidateId)
        .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));
    Job job = jobRepository.findById(jobId)
        .orElseThrow(() -> new IllegalArgumentException("Job not found"));

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
```

### Statistics Query (Repository)

```java
@Query("SELECT a.status, COUNT(a) FROM Application a " +
       "WHERE a.candidate.id = :candidateId GROUP BY a.status")
List<Object[]> getStatisticsByCandidateId(@Param("candidateId") Long candidateId);
```

### Thymeleaf Template Example

```html
<!-- Apply Button -->
<form th:action="@{/applications/apply}" method="post">
  <input type="hidden" name="jobId" th:value="${job.id}" />
  <button type="submit" class="btn btn-success">
    <i class="fas fa-paper-plane"></i> Apply for this Job
  </button>
</form>

<!-- Status Badge -->
<span
  class="badge"
  th:classappend="${app.status.name() == 'PENDING'} ? 'bg-warning' : 
                      (${app.status.name() == 'ACCEPTED'} ? 'bg-success' : 'bg-danger')"
  th:text="${app.status}"
>
</span>
```

---

## ✅ Checklist

- [x] Entity: Application, ApplicationStatus
- [x] Repository: ApplicationRepository với queries
- [x] Service: ApplicationService với business logic
- [x] Controllers: ApplicationController, JobViewController, CompanyController
- [x] Views: job-detail, applications, job-applicants
- [x] Navigation: Header links updated
- [x] Validation: Duplicate check, status transitions
- [x] Security: Permission checks
- [x] UI: Bootstrap, Chart.js, responsive
- [x] Documentation: Complete

---

## 📚 Tài liệu chi tiết

Xem file `APPLY_JOB_FEATURE_DOCUMENTATION.md` để biết thêm chi tiết về:

- Architecture deep dive
- Complete API documentation
- Database schema details
- Testing strategies
- Deployment guide

---

## 🎉 Done!

Chức năng Apply Job đã sẵn sàng sử dụng! 🚀

**Happy Coding!** 💻
