package vn.edu.iuh.fit.week05.backend.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SkillLevelConverter implements AttributeConverter<SkillLevel, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SkillLevel skillLevel) {
        return skillLevel != null ? skillLevel.getValue() : null;
    }

    @Override
    public SkillLevel convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        return SkillLevel.fromValue(value); // Dùng fromValue() để chuyển đổi
    }
}
