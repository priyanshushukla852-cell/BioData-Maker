package com.biodataai.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "family_details")
@Getter
@Setter
@NoArgsConstructor
public class FamilyDetails {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biodata_id", nullable = false, unique = true)
    private Biodata biodata;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "father_occupation", length = 100)
    private String fatherOccupation;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "mother_occupation", length = 100)
    private String motherOccupation;

    @Column(columnDefinition = "TEXT")
    private String siblings;

    @Column(name = "family_type", length = 20)
    private String familyType;

    @Column(name = "family_values", length = 50)
    private String familyValues;

    @Column(name = "family_status", length = 30)
    private String familyStatus;
}
