# 🔧 Fix Application Query Error - Job Detail Page After Apply

## ❌ Lỗi gặp phải

Sau khi candidate apply job thành công, khi redirect về job-detail page, gặp lỗi:

```
TemplateInputException: An error happened during template parsing
(template: "class path resource [templates/job-detail.html]")
```

## 🔍 Nguyên nhân

### Vấn đề 1: Query không hoạt động đúng

Trong `ApplicationService.getApplication()`:

```java
// ❌ BAD - Tạo entity objects rỗng chỉ có ID
public Optional<Application> getApplication(Long candidateId, Long jobId) {
    Candidate candidate = new Candidate();
    candidate.setId(candidateId);  // Chỉ set ID
    Job job = new Job();
    job.setId(jobId);  // Chỉ set ID
    return applicationRepository.findByCandidateAndJob(candidate, job);
}
```

**Vấn đề:** Spring Data JPA's `findByCandidateAndJob` so sánh toàn bộ entity objects, không chỉ ID. Việc tạo objects rỗng làm query không tìm thấy application.

### Vấn đề 2: Template không có null checks

Template `job-detail.html` cố gắng truy cập `application.status` và `application.createdDate` mà không check null:

```html
<!-- ❌ Không có null check -->
<span
  th:text="${#temporals.format(application.createdDate, 'dd-MM-yyyy HH:mm')}"
></span>
```

Nếu application là null hoặc có vấn đề → Thymeleaf parsing error.

## ✅ Giải pháp đã áp dụng

### Fix 1: Thêm custom query trong `ApplicationRepository`

```java
/**
 * Tìm application bằng candidate ID và job ID
 * Dùng query này thay vì findByCandidateAndJob để tránh phải load entity
 */
@Query("SELECT a FROM Application a WHERE a.candidate.id = :candidateId AND a.job.id = :jobId")
Optional<Application> findByCandidateIdAndJobId(@Param("candidateId") Long candidateId, @Param("jobId") Long jobId);

/**
 * Kiểm tra xem candidate đã apply job này chưa bằng IDs
 */
@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Application a WHERE a.candidate.id = :candidateId AND a.job.id = :jobId")
boolean existsByCandidateIdAndJobId(@Param("candidateId") Long candidateId, @Param("jobId") Long jobId);
```

**Giải thích:**

- Query trực tiếp bằng IDs thay vì so sánh entity objects
- Không cần tạo Candidate và Job objects
- Query chính xác hơn và nhanh hơn

### Fix 2: Update `ApplicationService`

```java
// ✅ GOOD - Query trực tiếp bằng IDs
public Optional<Application> getApplication(Long candidateId, Long jobId) {
    return applicationRepository.findByCandidateIdAndJobId(candidateId, jobId);
}

public boolean hasApplied(Long candidateId, Long jobId) {
    return applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId);
}
```

### Fix 3: Thêm null checks trong template

```html
<!-- ✅ GOOD - Có null checks -->
<div th:if="${hasApplied and application != null}" class="alert alert-warning">
  <p class="mb-0 mt-2" th:if="${application.status != null}">
    Status: <span th:text="${application.status}">Status</span>
  </p>
  <p class="mb-0 text-muted" th:if="${application.createdDate != null}">
    Applied on:
    <span
      th:text="${#temporals.format(application.createdDate, 'dd-MM-yyyy HH:mm')}"
      >Date</span
    >
  </p>
</div>
```

### Fix 4: Thêm error handling và logging trong `JobViewController`

```java
try {
    Optional<Application> existingApplication = applicationService.getApplication(candidate.getId(), id);
    log.debug("Checking application for candidate {} and job {}: {}",
              candidate.getId(), id, existingApplication.isPresent());

    if (existingApplication.isPresent()) {
        Application app = existingApplication.get();
        log.debug("Application found: id={}, status={}, createdDate={}",
                  app.getId(), app.getStatus(), app.getCreatedDate());
        model.addAttribute("hasApplied", true);
        model.addAttribute("application", app);
    } else {
        model.addAttribute("hasApplied", false);
    }
} catch (Exception e) {
    log.error("Error getting application for candidate {} and job {}", candidate.getId(), id, e);
    model.addAttribute("hasApplied", false);
}
```

## 🎯 Kết quả

✅ Application được tìm thấy chính xác sau khi apply  
✅ Job detail page hiển thị đúng status của application  
✅ Template parse thành công không lỗi  
✅ Có logging để debug nếu cần  
✅ Null-safe template rendering

## 📝 Files đã thay đổi

1. ✅ `ApplicationRepository.java`

   - Added `findByCandidateIdAndJobId()` method
   - Added `existsByCandidateIdAndJobId()` method

2. ✅ `ApplicationService.java`

   - Updated `getApplication()` to use new query
   - Updated `hasApplied()` to use new query

3. ✅ `JobViewController.java`

   - Added try-catch for error handling
   - Added debug logging
   - More robust null checking

4. ✅ `job-detail.html`
   - Added null checks: `application != null`
   - Added conditional rendering: `th:if="${application.status != null}"`
   - Added conditional rendering: `th:if="${application.createdDate != null}"`

## 🚀 Test Flow

```bash
# 1. Start application
./gradlew bootRun

# 2. Login as candidate
http://localhost:8080/login

# 3. Go to job detail page
http://localhost:8080/job/1

# 4. Click "Apply for this Job"
✅ Should redirect back to job detail

# 5. Page should show:
✅ "You have already applied for this job"
✅ Status badge (PENDING)
✅ Applied date
✅ "Withdraw Application" button

# 6. Check logs for debug messages:
✅ "Checking application for candidate X and job Y: true"
✅ "Application found: id=X, status=PENDING, createdDate=..."
```

## 💡 Key Lessons

### ❌ BAD: Comparing entity objects with only IDs

```java
Candidate candidate = new Candidate();
candidate.setId(1L);
repository.findByCandidate(candidate);  // ❌ Won't work as expected
```

### ✅ GOOD: Query by IDs directly

```java
@Query("SELECT e FROM Entity e WHERE e.relation.id = :relationId")
Entity findByRelationId(@Param("relationId") Long relationId);  // ✅ Works perfectly
```

### ❌ BAD: No null checks in template

```html
<span th:text="${object.property}">
  <!-- ❌ NullPointerException if object is null --></span
>
```

### ✅ GOOD: Always check null

```html
<div th:if="${object != null}">
  <span th:text="${object.property}">  <!-- ✅ Safe -->
</div>
```

## 🔗 Related Fixes

Nếu gặp vấn đề tương tự với các relationships khác:

1. **Tạo custom query với IDs**

```java
@Query("SELECT e FROM Entity e WHERE e.parent.id = :parentId AND e.child.id = :childId")
Optional<Entity> findByParentIdAndChildId(@Param("parentId") Long parentId, @Param("childId") Long childId);
```

2. **Thêm null checks trong template**

```html
<div th:if="${entity != null and entity.property != null}">
  <!-- Safe to access entity.property -->
</div>
```

3. **Thêm error handling trong controller**

```java
try {
    Optional<Entity> result = service.getEntity(id);
    if (result.isPresent()) {
        model.addAttribute("entity", result.get());
    }
} catch (Exception e) {
    log.error("Error getting entity", e);
    model.addAttribute("error", "Could not load entity");
}
```

## ✅ Done!

Lỗi đã được fix hoàn toàn! Candidate có thể apply job và xem lại application status trên job detail page. 🎉

---

**Fixed by:** AI Assistant  
**Date:** October 20, 2025  
**Issue:** Application not found after apply, template parsing error  
**Solution:** Query by IDs + Null checks + Error handling
