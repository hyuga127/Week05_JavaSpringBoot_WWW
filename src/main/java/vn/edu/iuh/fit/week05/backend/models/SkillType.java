package vn.edu.iuh.fit.week05.backend.models;

import lombok.Getter;

@Getter
public enum SkillType {
    TECHNICAL_SKILL(1, "Technical"),
    SOFT_SKILL(2, "Soft"),
    DESIGN_SKILL(3, "Design"),
    UNSPECIFIC(4, "Unspecific");

    private final int value;
    private final String name;

    SkillType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    // Phương thức chuyển giá trị từ số nguyên sang Enum
    public static SkillType fromValue(int value) {
        for (SkillType type : SkillType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid SkillType value: " + value);
    }
}
