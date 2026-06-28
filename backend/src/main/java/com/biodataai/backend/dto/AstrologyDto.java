package com.biodataai.backend.dto;

import com.biodataai.backend.entity.Manglik;
import java.time.LocalTime;

public record AstrologyDto(
        String rashi,
        String nakshatra,
        Manglik manglik,
        LocalTime birthTime,
        String birthPlace,
        String sunSign) {
}
