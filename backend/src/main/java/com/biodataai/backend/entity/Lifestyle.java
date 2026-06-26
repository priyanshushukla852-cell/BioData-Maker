package com.biodataai.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "lifestyle")
@Getter
@Setter
@NoArgsConstructor
public class Lifestyle {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biodata_id", nullable = false, unique = true)
    private Biodata biodata;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Diet diet;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private HabitFrequency drinking;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private HabitFrequency smoking;

    @Column(columnDefinition = "TEXT")
    private String hobbies;

    @Column(name = "languages_spoken", columnDefinition = "TEXT")
    private String languagesSpoken;
}
