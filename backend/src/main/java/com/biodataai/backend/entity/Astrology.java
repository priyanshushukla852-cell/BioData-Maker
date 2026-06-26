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
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "astrology")
@Getter
@Setter
@NoArgsConstructor
public class Astrology {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biodata_id", nullable = false, unique = true)
    private Biodata biodata;

    @Column(length = 50)
    private String rashi;

    @Column(length = 50)
    private String nakshatra;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Manglik manglik;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Column(name = "birth_place", length = 100)
    private String birthPlace;
}
