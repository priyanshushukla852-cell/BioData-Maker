package com.biodataai.backend.dto;

import com.biodataai.backend.entity.Gender;
import java.time.LocalDate;

public record PersonalDetailsDto(
        String fullName,
        LocalDate dob,
        Gender gender,
        String religion,
        String caste,
        String gotra,
        Integer heightCm,
        String complexion,
        String disability,
        String maritalStatus,
        String bloodGroup) {
}
