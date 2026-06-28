package com.biodataai.backend.dto;

import com.biodataai.backend.entity.Diet;
import com.biodataai.backend.entity.HabitFrequency;

public record LifestyleDto(
        Diet diet,
        HabitFrequency drinking,
        HabitFrequency smoking,
        String hobbies,
        String languagesSpoken,
        String interests) {
}
