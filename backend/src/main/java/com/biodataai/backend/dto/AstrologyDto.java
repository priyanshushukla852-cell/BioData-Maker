package com.biodataai.backend.dto;

import com.biodataai.backend.entity.Manglik;

public record AstrologyDto(
        String rashi,
        String nakshatra,
        Manglik manglik,
        // String (not LocalTime) so a malformed value can't fail Jackson binding for the whole
        // update payload — it's parsed leniently in the service and dropped to null if invalid.
        String birthTime,
        String birthPlace,
        String sunSign) {
}
