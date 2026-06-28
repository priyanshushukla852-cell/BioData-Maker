package com.biodataai.backend.dto;

public record FamilyDetailsDto(
        String fatherName,
        String fatherOccupation,
        String motherName,
        String motherOccupation,
        String siblings,
        String familyType,
        String familyValues,
        String familyStatus) {
}
