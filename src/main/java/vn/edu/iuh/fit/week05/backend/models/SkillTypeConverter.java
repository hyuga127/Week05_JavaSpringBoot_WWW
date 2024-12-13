package vn.edu.iuh.fit.week05.backend.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SkillTypeConverter implements AttributeConverter<SkillType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SkillType skillType) {
        if (skillType == null) {
            return null;
        }
        return skillType.getValue();
    }

    @Override
    public SkillType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return SkillType.fromValue(dbData);
    }
}
