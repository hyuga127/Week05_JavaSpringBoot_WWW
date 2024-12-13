package vn.edu.iuh.fit.week05.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class JobSkillId implements Serializable {
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    // equals & hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobSkillId that = (JobSkillId) o;
        return jobId.equals(that.jobId) && skillId.equals(that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, skillId);
    }
}
