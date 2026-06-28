package com.biodataai.backend.service;

import com.biodataai.backend.dto.AstrologyDto;
import com.biodataai.backend.dto.BiodataCreateRequest;
import com.biodataai.backend.dto.BiodataResponse;
import com.biodataai.backend.dto.BiodataSummaryResponse;
import com.biodataai.backend.dto.BiodataUpdateRequest;
import com.biodataai.backend.dto.ContactInfoDto;
import com.biodataai.backend.dto.EducationCareerDto;
import com.biodataai.backend.dto.FamilyDetailsDto;
import com.biodataai.backend.dto.LifestyleDto;
import com.biodataai.backend.dto.PersonalDetailsDto;
import com.biodataai.backend.dto.PhotoDto;
import com.biodataai.backend.entity.Astrology;
import com.biodataai.backend.entity.Biodata;
import com.biodataai.backend.entity.ContactInfo;
import com.biodataai.backend.entity.EducationCareer;
import com.biodataai.backend.entity.FamilyDetails;
import com.biodataai.backend.entity.Lifestyle;
import com.biodataai.backend.entity.PersonalDetails;
import com.biodataai.backend.entity.Template;
import com.biodataai.backend.entity.User;
import com.biodataai.backend.exception.ResourceNotFoundException;
import com.biodataai.backend.repository.AstrologyRepository;
import com.biodataai.backend.repository.BiodataPhotoRepository;
import com.biodataai.backend.repository.BiodataRepository;
import com.biodataai.backend.repository.ContactInfoRepository;
import com.biodataai.backend.repository.EducationCareerRepository;
import com.biodataai.backend.repository.FamilyDetailsRepository;
import com.biodataai.backend.repository.LifestyleRepository;
import com.biodataai.backend.repository.PersonalDetailsRepository;
import com.biodataai.backend.repository.TemplateRepository;
import com.biodataai.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BiodataService {

    private final BiodataRepository biodataRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final PersonalDetailsRepository personalDetailsRepository;
    private final FamilyDetailsRepository familyDetailsRepository;
    private final EducationCareerRepository educationCareerRepository;
    private final LifestyleRepository lifestyleRepository;
    private final AstrologyRepository astrologyRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final BiodataPhotoRepository biodataPhotoRepository;

    public BiodataService(
            BiodataRepository biodataRepository,
            UserRepository userRepository,
            TemplateRepository templateRepository,
            PersonalDetailsRepository personalDetailsRepository,
            FamilyDetailsRepository familyDetailsRepository,
            EducationCareerRepository educationCareerRepository,
            LifestyleRepository lifestyleRepository,
            AstrologyRepository astrologyRepository,
            ContactInfoRepository contactInfoRepository,
            BiodataPhotoRepository biodataPhotoRepository) {
        this.biodataRepository = biodataRepository;
        this.userRepository = userRepository;
        this.templateRepository = templateRepository;
        this.personalDetailsRepository = personalDetailsRepository;
        this.familyDetailsRepository = familyDetailsRepository;
        this.educationCareerRepository = educationCareerRepository;
        this.lifestyleRepository = lifestyleRepository;
        this.astrologyRepository = astrologyRepository;
        this.contactInfoRepository = contactInfoRepository;
        this.biodataPhotoRepository = biodataPhotoRepository;
    }

    @Transactional
    public BiodataResponse create(UUID userId, BiodataCreateRequest request) {
        // Idempotent on a client-supplied id: a retried offline sync returns the existing row
        // rather than creating a duplicate.
        if (request.id() != null) {
            Optional<Biodata> existing = biodataRepository.findById(request.id());
            if (existing.isPresent()) {
                Biodata b = existing.get();
                if (!b.getUser().getId().equals(userId)) {
                    throw new ResourceNotFoundException("Biodata not found.");
                }
                return toResponse(b);
            }
        }
        User user = userRepository.getReferenceById(userId);
        Biodata biodata = new Biodata();
        biodata.setId(request.id() != null ? request.id() : UUID.randomUUID());
        biodata.setUser(user);
        biodata.setTitle(request.title() != null ? request.title() : "");
        biodata.setLanguage(request.language());
        if (request.templateId() != null) {
            Template template = templateRepository.findById(request.templateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found."));
            biodata.setTemplate(template);
        }
        Biodata saved = biodataRepository.save(biodata);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BiodataSummaryResponse> list(UUID userId) {
        return biodataRepository.findAllByUserId(userId).stream()
                .map(BiodataSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BiodataResponse getById(UUID userId, UUID biodataId) {
        return toResponse(findOwned(userId, biodataId));
    }

    @Transactional(readOnly = true)
    public Biodata getOwnedEntity(UUID userId, UUID biodataId) {
        return findOwned(userId, biodataId);
    }

    @Transactional
    public void attachAiSummary(UUID biodataId, String summaryText) {
        Biodata biodata = biodataRepository.getReferenceById(biodataId);
        biodata.setAiSummary(summaryText);
        biodataRepository.save(biodata);
    }

    @Transactional
    public BiodataResponse update(UUID userId, UUID biodataId, BiodataUpdateRequest request) {
        Biodata biodata = findOwned(userId, biodataId);

        if (request.title() != null) {
            biodata.setTitle(request.title());
        }
        if (request.language() != null) {
            biodata.setLanguage(request.language());
        }
        if (request.status() != null) {
            biodata.setStatus(request.status());
        }
        if (request.templateId() != null) {
            Template template = templateRepository.findById(request.templateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found."));
            biodata.setTemplate(template);
        }
        biodataRepository.save(biodata);

        if (request.personalDetails() != null) {
            upsertPersonalDetails(biodata, request.personalDetails());
        }
        if (request.familyDetails() != null) {
            upsertFamilyDetails(biodata, request.familyDetails());
        }
        if (request.educationCareer() != null) {
            upsertEducationCareer(biodata, request.educationCareer());
        }
        if (request.lifestyle() != null) {
            upsertLifestyle(biodata, request.lifestyle());
        }
        if (request.astrology() != null) {
            upsertAstrology(biodata, request.astrology());
        }
        if (request.contactInfo() != null) {
            upsertContactInfo(biodata, request.contactInfo());
        }

        return toResponse(biodata);
    }

    @Transactional
    public void softDelete(UUID userId, UUID biodataId) {
        Biodata biodata = findOwned(userId, biodataId);
        biodataRepository.delete(biodata);
    }

    private Biodata findOwned(UUID userId, UUID biodataId) {
        return biodataRepository
                .findByIdAndUserId(biodataId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Biodata not found."));
    }

    private void upsertPersonalDetails(Biodata biodata, PersonalDetailsDto dto) {
        PersonalDetails entity = personalDetailsRepository
                .findByBiodataId(biodata.getId())
                .orElseGet(() -> {
                    PersonalDetails created = new PersonalDetails();
                    created.setBiodata(biodata);
                    return created;
                });
        entity.setFullName(dto.fullName());
        entity.setDob(dto.dob());
        entity.setGender(dto.gender());
        entity.setReligion(dto.religion());
        entity.setCaste(dto.caste());
        entity.setGotra(dto.gotra());
        entity.setHeightCm(dto.heightCm());
        entity.setComplexion(dto.complexion());
        entity.setDisability(dto.disability());
        entity.setMaritalStatus(dto.maritalStatus());
        personalDetailsRepository.save(entity);
    }

    private void upsertFamilyDetails(Biodata biodata, FamilyDetailsDto dto) {
        FamilyDetails entity = familyDetailsRepository
                .findByBiodataId(biodata.getId())
                .orElseGet(() -> {
                    FamilyDetails created = new FamilyDetails();
                    created.setBiodata(biodata);
                    return created;
                });
        entity.setFatherName(dto.fatherName());
        entity.setFatherOccupation(dto.fatherOccupation());
        entity.setMotherName(dto.motherName());
        entity.setMotherOccupation(dto.motherOccupation());
        entity.setSiblings(dto.siblings());
        entity.setFamilyType(dto.familyType());
        entity.setFamilyValues(dto.familyValues());
        entity.setFamilyStatus(dto.familyStatus());
        familyDetailsRepository.save(entity);
    }

    private void upsertEducationCareer(Biodata biodata, EducationCareerDto dto) {
        EducationCareer entity = educationCareerRepository
                .findByBiodataId(biodata.getId())
                .orElseGet(() -> {
                    EducationCareer created = new EducationCareer();
                    created.setBiodata(biodata);
                    return created;
                });
        entity.setHighestQualification(dto.highestQualification());
        entity.setCollege(dto.college());
        entity.setJobTitle(dto.jobTitle());
        entity.setCompany(dto.company());
        entity.setAnnualIncome(dto.annualIncome());
        entity.setWorkLocation(dto.workLocation());
        entity.setEducationField(dto.educationField());
        educationCareerRepository.save(entity);
    }

    private void upsertLifestyle(Biodata biodata, LifestyleDto dto) {
        Lifestyle entity = lifestyleRepository
                .findByBiodataId(biodata.getId())
                .orElseGet(() -> {
                    Lifestyle created = new Lifestyle();
                    created.setBiodata(biodata);
                    return created;
                });
        entity.setDiet(dto.diet());
        entity.setDrinking(dto.drinking());
        entity.setSmoking(dto.smoking());
        entity.setHobbies(dto.hobbies());
        entity.setLanguagesSpoken(dto.languagesSpoken());
        entity.setInterests(dto.interests());
        lifestyleRepository.save(entity);
    }

    private void upsertAstrology(Biodata biodata, AstrologyDto dto) {
        Astrology entity = astrologyRepository
                .findByBiodataId(biodata.getId())
                .orElseGet(() -> {
                    Astrology created = new Astrology();
                    created.setBiodata(biodata);
                    return created;
                });
        entity.setRashi(dto.rashi());
        entity.setNakshatra(dto.nakshatra());
        entity.setManglik(dto.manglik());
        entity.setBirthTime(dto.birthTime());
        entity.setBirthPlace(dto.birthPlace());
        entity.setSunSign(dto.sunSign());
        astrologyRepository.save(entity);
    }

    private void upsertContactInfo(Biodata biodata, ContactInfoDto dto) {
        ContactInfo entity = contactInfoRepository
                .findByBiodataId(biodata.getId())
                .orElseGet(() -> {
                    ContactInfo created = new ContactInfo();
                    created.setBiodata(biodata);
                    return created;
                });
        entity.setContactPhone(dto.contactPhone());
        entity.setContactEmail(dto.contactEmail());
        entity.setCity(dto.city());
        entity.setState(dto.state());
        entity.setCountry(dto.country());
        entity.setAddress(dto.address());
        entity.setPostalCode(dto.postalCode());
        contactInfoRepository.save(entity);
    }

    private BiodataResponse toResponse(Biodata biodata) {
        PersonalDetailsDto personalDetails = personalDetailsRepository
                .findByBiodataId(biodata.getId())
                .map(p -> new PersonalDetailsDto(
                        p.getFullName(), p.getDob(), p.getGender(), p.getReligion(), p.getCaste(), p.getGotra(),
                        p.getHeightCm(), p.getComplexion(), p.getDisability(), p.getMaritalStatus()))
                .orElse(null);

        FamilyDetailsDto familyDetails = familyDetailsRepository
                .findByBiodataId(biodata.getId())
                .map(f -> new FamilyDetailsDto(
                        f.getFatherName(), f.getFatherOccupation(), f.getMotherName(), f.getMotherOccupation(),
                        f.getSiblings(), f.getFamilyType(), f.getFamilyValues(), f.getFamilyStatus()))
                .orElse(null);

        EducationCareerDto educationCareer = educationCareerRepository
                .findByBiodataId(biodata.getId())
                .map(e -> new EducationCareerDto(
                        e.getHighestQualification(), e.getCollege(), e.getJobTitle(), e.getCompany(),
                        e.getAnnualIncome(), e.getWorkLocation(), e.getEducationField()))
                .orElse(null);

        LifestyleDto lifestyle = lifestyleRepository
                .findByBiodataId(biodata.getId())
                .map(l -> new LifestyleDto(
                        l.getDiet(), l.getDrinking(), l.getSmoking(), l.getHobbies(), l.getLanguagesSpoken(),
                        l.getInterests()))
                .orElse(null);

        AstrologyDto astrology = astrologyRepository
                .findByBiodataId(biodata.getId())
                .map(a -> new AstrologyDto(
                        a.getRashi(), a.getNakshatra(), a.getManglik(), a.getBirthTime(), a.getBirthPlace(),
                        a.getSunSign()))
                .orElse(null);

        ContactInfoDto contactInfo = contactInfoRepository
                .findByBiodataId(biodata.getId())
                .map(c -> new ContactInfoDto(
                        c.getContactPhone(), c.getContactEmail(), c.getCity(), c.getState(), c.getCountry(),
                        c.getAddress(), c.getPostalCode()))
                .orElse(null);

        List<PhotoDto> photos = biodataPhotoRepository
                .findAllByBiodataIdOrderBySortOrderAsc(biodata.getId())
                .stream()
                .map(PhotoDto::from)
                .toList();

        return new BiodataResponse(
                biodata.getId(),
                biodata.getTitle(),
                biodata.getLanguage(),
                biodata.getStatus(),
                biodata.getTemplate() != null ? biodata.getTemplate().getId() : null,
                biodata.getAiSummary(),
                personalDetails,
                familyDetails,
                educationCareer,
                lifestyle,
                astrology,
                contactInfo,
                photos,
                biodata.getCreatedAt(),
                biodata.getUpdatedAt());
    }
}
