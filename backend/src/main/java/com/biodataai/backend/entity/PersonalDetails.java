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
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "personal_details")
@Getter
@Setter
@NoArgsConstructor
public class PersonalDetails {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biodata_id", nullable = false, unique = true)
    private Biodata biodata;

    // Nullable: drafts sync partially; completeness enforced at export (see V6 migration).
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 50)
    private String religion;

    @Column(length = 50)
    private String caste;

    @Column(length = 50)
    private String gotra;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(length = 30)
    private String complexion;

    @Column(length = 100)
    private String disability;

    @Column(name = "marital_status", length = 30)
    private String maritalStatus;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;
}
