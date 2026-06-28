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
@Table(name = "education_career")
@Getter
@Setter
@NoArgsConstructor
public class EducationCareer {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biodata_id", nullable = false, unique = true)
    private Biodata biodata;

    @Column(name = "highest_qualification", nullable = false, length = 100)
    private String highestQualification;

    @Column(length = 200)
    private String college;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(length = 200)
    private String company;

    @Column(name = "annual_income", length = 50)
    private String annualIncome;

    @Column(name = "work_location", length = 100)
    private String workLocation;

    @Column(name = "education_field", length = 100)
    private String educationField;
}
