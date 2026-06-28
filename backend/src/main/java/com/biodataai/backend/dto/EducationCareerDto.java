package com.biodataai.backend.dto;

public record EducationCareerDto(
        String highestQualification,
        String college,
        String jobTitle,
        String company,
        String annualIncome,
        String workLocation,
        String educationField) {
}
