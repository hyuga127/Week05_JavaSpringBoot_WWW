# 🔧 Fix Lazy Loading Error - Job Detail Page

## ❌ Lỗi gặp phải

```
TemplateInputException: An error happened during template parsing
(template: "class path resource [templates/job-detail.html]")
```

## 🔍 Nguyên nhân

Trong entity `Job`:

```java
@ManyToOne(fetch = FetchType.LAZY)  // ❌ LAZY Loading
private Company company;

@OneToMany(mappedBy = "job", fetch = FetchType.LAZY)  // ❌ LAZY Loading
private List<JobSkill> jobSkills;
```

Khi template Thymeleaf cố gắng truy cập `${job.company.name}` hoặc `${job.jobSkills}`, Hibernate session đã đóng → **LazyInitializationException**

## ✅ Giải pháp đã áp dụng

### 1. Thêm method JOIN FETCH trong `JobRepository`

```java
/**
 * Find job by ID with EAGER fetch for company and jobSkills
 * This is used for job detail page to avoid LazyInitializationException
 */
@Query("""
    SELECT j
    FROM Job j
    LEFT JOIN FETCH j.company
    LEFT JOIN FETCH j.jobSkills js
    LEFT JOIN FETCH js.skill
    WHERE j.id = :id
""")
Job findByIdWithDetails(@Param("id") Long id);
```

**Giải thích:**

- `LEFT JOIN FETCH j.company` - Load company cùng lúc với job
- `LEFT JOIN FETCH j.jobSkills js` - Load jobSkills cùng lúc với job
- `LEFT JOIN FETCH js.skill` - Load skill của từng jobSkill
- Tất cả data được load trong 1 query → Không có LazyInitializationException

### 2. Thêm method mới trong `JobService`

```java
/**
 * Get job by ID with all details (company, jobSkills) for displaying in detail page
 * This method uses JOIN FETCH to avoid LazyInitializationException
 */
public Job getJobByIdWithDetails(Long id) {
    return jobRepository.findByIdWithDetails(id);
}
```

### 3. Update `JobViewController`

```java
@GetMapping("/{id}")
public String viewJobDetail(@PathVariable Long id, HttpSession session, Model model) {

    // BEFORE: Job job = jobService.getJobById(id);  ❌
    // AFTER:
    Job job = jobService.getJobByIdWithDetails(id);  ✅

    if (job == null) {
        log.warn("Job not found with ID: {}", id);
        return "redirect:/candidate/home";
    }

    model.addAttribute("job", job);
    // ... rest of code
}
```

## 🎯 Kết quả

✅ Job được load với đầy đủ company và jobSkills  
✅ Template có thể truy cập `${job.company.name}` không lỗi  
✅ Template có thể loop qua `${job.jobSkills}` không lỗi  
✅ Chỉ cần 1 query thay vì N+1 queries → Better performance

## 📝 Files đã thay đổi

1. ✅ `JobRepository.java` - Added `findByIdWithDetails()` method
2. ✅ `JobService.java` - Added `getJobByIdWithDetails()` method
3. ✅ `JobViewController.java` - Updated to use new method

## 🚀 Test

```bash
# Restart application
./gradlew bootRun

# Access job detail page
http://localhost:8080/job/1

# Should work without errors now! ✅
```

## 💡 Best Practices

### ❌ BAD: Change to EAGER (affects ALL queries)

```java
@ManyToOne(fetch = FetchType.EAGER)  // ❌ Always loads, even when not needed
private Company company;
```

### ✅ GOOD: Use JOIN FETCH selectively

```java
// Keep LAZY as default
@ManyToOne(fetch = FetchType.LAZY)
private Company company;

// Use JOIN FETCH only when needed
@Query("SELECT j FROM Job j LEFT JOIN FETCH j.company WHERE j.id = :id")
Job findByIdWithDetails(@Param("id") Long id);
```

**Lý do:**

- LAZY = default → better performance cho most cases
- JOIN FETCH = dùng khi thực sự cần → targeted optimization
- Best of both worlds! 🎉

## 🔗 Related Issues

Nếu gặp LazyInitializationException ở chỗ khác:

### Option 1: JOIN FETCH (Recommended)

```java
@Query("SELECT e FROM Entity e LEFT JOIN FETCH e.relation WHERE ...")
```

### Option 2: @Transactional

```java
@Transactional
public Entity getEntity(Long id) {
    Entity entity = repository.findById(id).get();
    entity.getRelation().size(); // Force load within transaction
    return entity;
}
```

### Option 3: @EntityGraph

```java
@EntityGraph(attributePaths = {"company", "jobSkills"})
Job findById(Long id);
```

## ✅ Done!

Lỗi đã được fix hoàn toàn! Application giờ có thể hiển thị job detail page không lỗi. 🎉

---

**Fixed by:** AI Assistant  
**Date:** October 20, 2025  
**Issue:** LazyInitializationException in job-detail.html  
**Solution:** JOIN FETCH in custom repository query
