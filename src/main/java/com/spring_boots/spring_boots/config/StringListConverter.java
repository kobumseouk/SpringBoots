package com.spring_boots.spring_boots.config;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return String.join(",", attribute);  // List<String>을 콤마로 연결된 문자열로 변환
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return List.of();  // 비어있는 문자열일 경우 빈 리스트 반환
        }
        return Arrays.stream(dbData.split(","))
                .collect(Collectors.toList());  // 콤마로 구분된 문자열을 List<String>으로 변환
    }
}