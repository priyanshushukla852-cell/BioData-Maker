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
@Table(name = "contact_info")
@Getter
@Setter
@NoArgsConstructor
public class ContactInfo {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biodata_id", nullable = false, unique = true)
    private Biodata biodata;

    // Nullable: drafts sync partially; completeness enforced at export (see V6 migration).
    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 50)
    private String country;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}
