package vn.edu.iuh.fit.week05.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "skill", schema = "works")
public class Skill {
    @Id
    @Column(name = "skill_id", nullable = false)
    private Long id;

    @Column(name = "skill_desc")
    private String skillDescription;

    @Column(name = "skill_name")
    private String skillName;

    @Column(name = "skill_type")
    @Convert(converter = SkillTypeConverter.class)
    private SkillType type;

    @OneToMany(mappedBy = "skill")
    private List<JobSkill> jobSkills;

}