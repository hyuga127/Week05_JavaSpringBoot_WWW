package vn.edu.iuh.fit.week05.backend.models;

import lombok.Getter;

@Getter
public enum SkillLevel {
    BEGINNER(1, "Beginner"),
    INTERMEDIATE(2, "Intermediate"),
    ADVANCED(3, "Advanced"),
    PROFESSIONAL(4, "Professional"),
    MASTER(5, "Master");

    private final int value;
    private final String name;

    SkillLevel(int value, String name) {
        this.value = value;
        this.name = name;
    }

    // Phương thức chuyển giá trị từ số nguyên sang Enum
    public static SkillLevel fromValue(int value) {
        for (SkillLevel level : SkillLevel.values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid SkillLevel value: " + value);
    }
}
