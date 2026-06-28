package com.biodataai.backend.dto;

public record ContactInfoDto(
        String contactPhone,
        String contactEmail,
        String city,
        String state,
        String country,
        String address,
        String postalCode) {
}
