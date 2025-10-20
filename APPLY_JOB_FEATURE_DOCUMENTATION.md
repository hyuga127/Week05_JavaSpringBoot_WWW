# Tài liệu Chức năng Apply Job - Hệ thống Tuyển dụng

## 📋 Mục lục

1. [Tổng quan](#tổng-quan)
2. [Kiến trúc và Thiết kế](#kiến-trúc-và-thiết-kế)
3. [Chi tiết triển khai](#chi-tiết-triển-khai)
4. [Hướng dẫn sử dụng](#hướng-dẫn-sử-dụng)
5. [API Documentation](#api-documentation)
6. [Database Schema](#database-schema)
7. [Testing](#testing)

---

## 🎯 Tổng quan

### Mục tiêu

Hiện thực đầy đủ chức năng "Ứng tuyển công việc (Apply Job)" cho hệ thống tuyển dụng, cho phép:

- **Ứng viên (Candidate)**: Apply job, withdraw application, xem lịch sử và thống kê applications
- **Công ty (Company)**: Xem danh sách applicants, cập nhật trạng thái applications, xem thống kê

### Công nghệ sử dụng

- **Backend**: Java 17, Spring Boot 3.x
- **Framework**: Spring MVC, Spring Data JPA, Spring Security
- **Database**: PostgreSQL/MySQL
- **Template Engine**: Thymeleaf
- **Frontend**: Bootstrap 5, Chart.js, Font Awesome
- **Build Tool**: Gradle

### Các tính năng chính

✅ Apply job (chỉ 1 lần cho mỗi job)  
✅ Withdraw application (rút đơn ứng tuyển)  
✅ Xem danh sách applications với thống kê  
✅ Quản lý trạng thái application (PENDING → REVIEWING → ACCEPTED/REJECTED)  
✅ Dashboard với biểu đồ thống kê  
✅ Notification và validation

---

## 🏗️ Kiến trúc và Thiết kế

### Kiến trúc MVC

```
┌─────────────────┐
│   View Layer    │ (Thymeleaf Templates)
│  Templates/     │
└────────┬────────┘
         │
┌────────▼────────┐
│  Controller     │ (Frontend Controllers)
│  Layer          │  - ApplicationController
└────────┬────────┘  - JobViewController
         │           - CompanyController
┌────────▼────────┐
│  Service        │ (Business Logic)
│  Layer          │  - ApplicationService
└────────┬────────┘
         │
┌────────▼────────┐
│  Repository     │ (Data Access)
│  Layer          │  - ApplicationRepository
└────────┬────────┘
         │
┌────────▼────────┐
│  Database       │ (PostgreSQL/MySQL)
│  Layer          │
└─────────────────┘
```

### Entity Relationship

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│  Candidate   │         │ Application  │         │     Job      │
│──────────────│         │──────────────│         │──────────────│
│ id (PK)      │────────▶│ id (PK)      │◀────────│ id (PK)      │
│ fullName     │ 1     * │ candidate_id │ *     1 │ name         │
│ email        │         │ job_id       │         │ description  │
│ ...          │         │ status       │         │ company_id   │
└──────────────┘         │ createdDate  │         └──────────────┘
                         └──────────────┘                │
                                 │                       │
                                 │                  ┌────▼─────┐
                                 │                  │ Company  │
                                 ▼                  └──────────┘
                    ┌────────────────────┐
                    │ ApplicationStatus  │
                    │────────────────────│
                    │ - PENDING          │
                    │ - REVIEWING        │
                    │ - ACCEPTED         │
                    │ - REJECTED         │
                    │ - WITHDRAWN        │
                    └────────────────────┘
```

---

## 💻 Chi tiết triển khai

### 1. Entity Classes

#### Application.java

```java
@Entity
@Table(name = "application", schema = "works")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "app_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
}
```

#### ApplicationStatus.java (ENUM)

```java
public enum ApplicationStatus {
    PENDING,      // Đơn mới nộp, chờ xem xét
    REVIEWING,    // Đang được xem xét
    ACCEPTED,     // Đã chấp nhận
    REJECTED,     // Đã từ chối
    WITHDRAWN     // Ứng viên đã rút đơn
}
```

### 2. Repository Layer

#### ApplicationRepository.java

Các query methods quan trọng:

```java
// Kiểm tra đã apply chưa
boolean existsByCandidateAndJob(Candidate candidate, Job job);

// Lấy applications của candidate
List<Application> findByCandidateOrderByCreatedDateDesc(Candidate candidate);

// Lấy applications cho job
List<Application> findByJobOrderByCreatedDateDesc(Job job);

// Thống kê theo status
@Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId GROUP BY a.status")
List<Object[]> getStatisticsByCandidateId(@Param("candidateId") Long candidateId);
```

### 3. Service Layer

#### ApplicationService.java

Các method chính:

```java
// Apply job
public Application applyJob(Long candidateId, Long jobId)
// Kiểm tra: đã apply chưa, tạo application với status PENDING

// Withdraw application
public Application withdrawApplication(Long applicationId, Long candidateId)
// Kiểm tra: quyền sở hữu, status phải là PENDING/REVIEWING

// Update status (Company)
public Application updateApplicationStatus(Long applicationId, Long companyId, ApplicationStatus newStatus)
// Kiểm tra: quyền, không cho update WITHDRAWN

// Thống kê
public Map<ApplicationStatus, Long> getCandidateStatistics(Long candidateId)
public Map<ApplicationStatus, Long> getJobStatistics(Long jobId)
public Map<ApplicationStatus, Long> getCompanyStatistics(Long companyId)
```

### 4. Controller Layer

#### ApplicationController.java

```java
// Candidate apply job
POST /applications/apply?jobId={id}

// Candidate withdraw
POST /applications/{id}/withdraw

// Candidate xem applications
GET /applications/my-applications

// Company update status
POST /applications/{id}/update-status?status={STATUS}
```

#### JobViewController.java

```java
// Xem chi tiết job và apply
GET /job/{id}
```

#### CompanyController.java

```java
// Xem applicants cho một job
GET /company/job/{jobId}/applicants

// Xem tất cả applications
GET /company/applications
```

### 5. View Templates

#### job-detail.html

- Hiển thị chi tiết job
- Nút "Apply" (nếu chưa apply)
- Hiển thị status nếu đã apply
- Nút "Withdraw" (nếu có thể)

#### candidate/applications.html

- Dashboard với thống kê theo status
- Biểu đồ Chart.js (Doughnut)
- Bảng danh sách applications
- Chức năng withdraw

#### company/job-applicants.html

- Thống kê applicants theo status
- Biểu đồ Chart.js (Bar chart)
- Bảng danh sách applicants
- Buttons để update status

#### company/applications.html

- Tổng hợp tất cả applications
- Thống kê tổng thể
- Biểu đồ Pie chart

---

## 📖 Hướng dẫn sử dụng

### Dành cho Ứng viên (Candidate)

#### 1. Xem và Apply Job

1. Đăng nhập với tài khoản candidate
2. Tại trang Home (`/candidate/home`), xem danh sách jobs matching
3. Click "View Details" để xem chi tiết job
4. Click nút "Apply for this Job" màu xanh lá
5. Hệ thống sẽ tạo application với status `PENDING`

#### 2. Xem danh sách Applications

1. Click "Applications" trên navigation bar
2. Hoặc truy cập `/applications/my-applications`
3. Xem thống kê:
   - Total applications
   - Pending, Reviewing, Accepted, Rejected, Withdrawn
4. Xem biểu đồ phân bố
5. Xem bảng chi tiết từng application

#### 3. Withdraw Application

1. Tại trang "My Applications"
2. Tìm application cần withdraw (chỉ được withdraw khi status là PENDING hoặc REVIEWING)
3. Click nút "Withdraw" màu đỏ
4. Xác nhận trong dialog
5. Status sẽ chuyển sang `WITHDRAWN`

### Dành cho Công ty (Company)

#### 1. Xem Applicants cho Job

1. Đăng nhập với tài khoản company
2. Tại trang Home (`/company/home`), xem danh sách jobs
3. Click nút "Applicants" để xem ứng viên đã apply
4. Xem thống kê và danh sách chi tiết

#### 2. Quản lý Status

Tại trang Job Applicants:

- **Review**: Chuyển từ PENDING → REVIEWING
- **Accept**: Chuyển sang ACCEPTED (final)
- **Reject**: Chuyển sang REJECTED (final)

⚠️ **Lưu ý**:

- Không thể update application đã WITHDRAWN
- ACCEPTED và REJECTED là trạng thái final

#### 3. Xem tất cả Applications

1. Click "Applicants" trên navigation bar
2. Hoặc "View All Applicants" trong Quick Actions
3. Xem tổng hợp applications từ tất cả jobs
4. Click "View Job" để đến trang quản lý cụ thể

---

## 🔌 API Documentation

### Candidate APIs

#### Apply Job

```http
POST /applications/apply?jobId={jobId}
Content-Type: application/x-www-form-urlencoded

Response:
- Success: Redirect to /job/{jobId} with success message
- Error: Redirect to /job/{jobId} with error message

Errors:
- "You have already applied for this job"
- "Candidate not found"
- "Job not found"
```

#### Withdraw Application

```http
POST /applications/{applicationId}/withdraw
Content-Type: application/x-www-form-urlencoded

Response:
- Success: Redirect to /applications/my-applications with success message
- Error: Redirect with error message

Errors:
- "You don't have permission to withdraw this application"
- "Cannot withdraw application with status: {status}"
```

#### View My Applications

```http
GET /applications/my-applications

Response: HTML page with:
- List<Application> applications
- Map<ApplicationStatus, Long> statistics
- Long totalApplications
```

### Company APIs

#### View Job Applicants

```http
GET /company/job/{jobId}/applicants

Response: HTML page with:
- Job job
- List<Application> applications
- Map<ApplicationStatus, Long> statistics
- Long totalApplications
```

#### Update Application Status

```http
POST /applications/{applicationId}/update-status
Content-Type: application/x-www-form-urlencoded

Parameters:
- status: ApplicationStatus (PENDING, REVIEWING, ACCEPTED, REJECTED)

Response:
- Success: Redirect to /company/job/{jobId}/applicants
- Error: Redirect with error message

Errors:
- "You don't have permission to update this application"
- "Cannot update status of withdrawn application"
```

#### View All Applications

```http
GET /company/applications

Response: HTML page with:
- List<Application> applications
- Map<ApplicationStatus, Long> statistics
```

---

## 🗄️ Database Schema

### Application Table

```sql
CREATE TABLE application (
    app_id BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL,
    app_status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_candidate FOREIGN KEY (candidate_id)
        REFERENCES candidate(can_id),
    CONSTRAINT fk_job FOREIGN KEY (job_id)
        REFERENCES job(job_id),
    CONSTRAINT uq_candidate_job UNIQUE (candidate_id, job_id)
);

-- Indexes for performance
CREATE INDEX idx_application_candidate ON application(candidate_id);
CREATE INDEX idx_application_job ON application(job_id);
CREATE INDEX idx_application_status ON application(app_status);
CREATE INDEX idx_application_created_date ON application(created_date DESC);
```

### Quan hệ với các bảng khác

```sql
-- Candidate Table (existing)
candidate (can_id) ←─ application (candidate_id)

-- Job Table (existing)
job (job_id) ←─ application (job_id)

-- Job → Company
job (company_id) → company (comp_id)
```

---

## 🧪 Testing

### Test Scenarios

#### Candidate Tests

1. ✅ Apply job successfully
2. ✅ Cannot apply same job twice
3. ✅ Withdraw application (PENDING)
4. ✅ Withdraw application (REVIEWING)
5. ❌ Cannot withdraw ACCEPTED application
6. ❌ Cannot withdraw REJECTED application
7. ✅ View applications list
8. ✅ View statistics

#### Company Tests

1. ✅ View job applicants
2. ✅ Update status: PENDING → REVIEWING
3. ✅ Update status: REVIEWING → ACCEPTED
4. ✅ Update status: REVIEWING → REJECTED
5. ❌ Cannot update WITHDRAWN application
6. ❌ Cannot update other company's application
7. ✅ View all applications
8. ✅ View statistics

### Sample Test Cases (JUnit)

```java
@Test
void testApplyJobSuccess() {
    // Given
    Long candidateId = 1L;
    Long jobId = 1L;

    // When
    Application result = applicationService.applyJob(candidateId, jobId);

    // Then
    assertNotNull(result);
    assertEquals(ApplicationStatus.PENDING, result.getStatus());
    assertNotNull(result.getCreatedDate());
}

@Test
void testCannotApplyTwice() {
    // Given
    Long candidateId = 1L;
    Long jobId = 1L;
    applicationService.applyJob(candidateId, jobId);

    // When & Then
    assertThrows(IllegalStateException.class,
        () -> applicationService.applyJob(candidateId, jobId));
}

@Test
void testWithdrawSuccess() {
    // Given
    Application app = createTestApplication(ApplicationStatus.PENDING);

    // When
    Application result = applicationService.withdrawApplication(
        app.getId(), app.getCandidate().getId());

    // Then
    assertEquals(ApplicationStatus.WITHDRAWN, result.getStatus());
}
```

---

## 🎨 UI/UX Features

### Design Principles

- **Modern UI**: Bootstrap 5 với custom CSS
- **Responsive**: Mobile-friendly design
- **Interactive**: Real-time feedback với alerts
- **Visual**: Chart.js cho biểu đồ thống kê
- **User-friendly**: Clear navigation và CTAs

### Components sử dụng

- ✅ Bootstrap Cards
- ✅ Bootstrap Tables
- ✅ Bootstrap Badges (cho status)
- ✅ Bootstrap Alerts
- ✅ Bootstrap Buttons
- ✅ Chart.js (Doughnut, Bar, Pie)
- ✅ Font Awesome Icons

### Color Coding cho Status

- 🟡 **PENDING**: Warning (Yellow)
- 🔵 **REVIEWING**: Info (Blue)
- 🟢 **ACCEPTED**: Success (Green)
- 🔴 **REJECTED**: Danger (Red)
- ⚫ **WITHDRAWN**: Secondary (Gray)

---

## 🔐 Security & Validation

### Authorization

- **Candidate** chỉ được:
  - Apply job
  - Withdraw application của mình
  - Xem applications của mình
- **Company** chỉ được:
  - Xem applicants của jobs thuộc công ty mình
  - Update status applications của jobs mình
  - Xem thống kê của công ty mình

### Validation Rules

1. ✅ Không apply trùng (unique constraint)
2. ✅ Chỉ withdraw khi PENDING/REVIEWING
3. ✅ Không update WITHDRAWN applications
4. ✅ Kiểm tra quyền sở hữu
5. ✅ Validate status transitions

---

## 🚀 Deployment

### Prerequisites

```bash
# Java 17
java -version

# PostgreSQL or MySQL
psql --version

# Gradle
gradle --version
```

### Build & Run

```bash
# Build project
./gradlew clean build

# Run application
./gradlew bootRun

# Or run JAR
java -jar build/libs/week05-0.0.1-SNAPSHOT.jar
```

### Database Migration

```sql
-- Run migration script
psql -U your_user -d your_database -f db/migration/V1__create_application_table.sql
```

---

## 📝 Notes & Best Practices

### Code Quality

- ✅ Follow MVC pattern
- ✅ Use DTOs for data transfer (if needed)
- ✅ Implement proper exception handling
- ✅ Add logging (SLF4J)
- ✅ Use transactions (@Transactional)
- ✅ Validate input data

### Performance

- ✅ Use indexes on database
- ✅ Use EAGER fetch only when necessary
- ✅ Implement pagination (if needed)
- ✅ Cache frequently accessed data (if needed)

### Maintenance

- ✅ Clear naming conventions
- ✅ Comprehensive comments
- ✅ Unit tests coverage
- ✅ Documentation updated

---

## 📞 Support & Contact

Nếu gặp vấn đề hoặc cần hỗ trợ, vui lòng:

1. Check documentation này
2. Xem logs trong console
3. Debug với breakpoints
4. Kiểm tra database records

---

## 🎉 Kết luận

Chức năng "Apply Job" đã được triển khai đầy đủ với:

- ✅ Backend: Repository, Service, Controller
- ✅ Frontend: Templates với Bootstrap và Chart.js
- ✅ Database: Schema với indexes
- ✅ Security: Authorization và validation
- ✅ UX: Modern UI với real-time feedback
- ✅ Testing: Test scenarios ready

Hệ thống sẵn sàng để sử dụng và mở rộng!

---

**Version**: 1.0.0  
**Last Updated**: 2024  
**Author**: Development Team
