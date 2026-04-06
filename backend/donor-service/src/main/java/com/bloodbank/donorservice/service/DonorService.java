package com.bloodbank.donorservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.DonorStatusEnum;
import com.bloodbank.donorservice.dto.DonorConsentCreateRequest;
import com.bloodbank.donorservice.dto.DonorConsentResponse;
import com.bloodbank.donorservice.dto.DonorCreateRequest;
import com.bloodbank.donorservice.dto.DonorDeferralCreateRequest;
import com.bloodbank.donorservice.dto.DonorDeferralResponse;
import com.bloodbank.donorservice.dto.DonorHealthRecordCreateRequest;
import com.bloodbank.donorservice.dto.DonorHealthRecordResponse;
import com.bloodbank.donorservice.dto.DonorResponse;
import com.bloodbank.donorservice.dto.DonorUpdateRequest;
import com.bloodbank.donorservice.entity.Donor;
import com.bloodbank.donorservice.entity.DonorConsent;
import com.bloodbank.donorservice.entity.DonorDeferral;
import com.bloodbank.donorservice.entity.DonorHealthRecord;
import com.bloodbank.donorservice.enums.DeferralStatusEnum;
import com.bloodbank.donorservice.enums.DeferralTypeEnum;
import com.bloodbank.donorservice.mapper.DonorConsentMapper;
import com.bloodbank.donorservice.mapper.DonorDeferralMapper;
import com.bloodbank.donorservice.mapper.DonorHealthRecordMapper;
import com.bloodbank.donorservice.mapper.DonorMapper;
import com.bloodbank.donorservice.repository.DonorConsentRepository;
import com.bloodbank.donorservice.repository.DonorDeferralRepository;
import com.bloodbank.donorservice.repository.DonorHealthRecordRepository;
import com.bloodbank.donorservice.repository.DonorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DonorService {

    private static final Logger log = LoggerFactory.getLogger(DonorService.class);

    private static final BigDecimal MIN_HEMOGLOBIN = new BigDecimal("12.5");
    private static final BigDecimal MIN_WEIGHT = new BigDecimal("50");
    private static final int MIN_PULSE = 50;
    private static final int MAX_PULSE = 100;
    private static final BigDecimal MAX_TEMPERATURE = new BigDecimal("37.5");
    private static final int MIN_SYSTOLIC = 90;
    private static final int MAX_SYSTOLIC = 180;
    private static final int MIN_DIASTOLIC = 60;
    private static final int MAX_DIASTOLIC = 100;
    private static final int MIN_DAYS_BETWEEN_DONATIONS = 56;

    private final DonorRepository donorRepository;
    private final DonorHealthRecordRepository healthRecordRepository;
    private final DonorDeferralRepository deferralRepository;
    private final DonorConsentRepository consentRepository;
    private final DonorMapper donorMapper;
    private final DonorHealthRecordMapper healthRecordMapper;
    private final DonorDeferralMapper deferralMapper;
    private final DonorConsentMapper consentMapper;

    public DonorService(DonorRepository donorRepository,
                        DonorHealthRecordRepository healthRecordRepository,
                        DonorDeferralRepository deferralRepository,
                        DonorConsentRepository consentRepository,
                        DonorMapper donorMapper,
                        DonorHealthRecordMapper healthRecordMapper,
                        DonorDeferralMapper deferralMapper,
                        DonorConsentMapper consentMapper) {
        this.donorRepository = donorRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.deferralRepository = deferralRepository;
        this.consentRepository = consentRepository;
        this.donorMapper = donorMapper;
        this.healthRecordMapper = healthRecordMapper;
        this.deferralMapper = deferralMapper;
        this.consentMapper = consentMapper;
    }

    @Transactional
    public DonorResponse registerDonor(DonorCreateRequest request) {
        log.info("Registering new donor: {} {}", request.firstName(), request.lastName());

        if (request.email() != null && !request.email().isBlank()
                && donorRepository.existsByEmail(request.email())) {
            throw new ConflictException("Donor with email '" + request.email() + "' already exists");
        }
        if (request.nationalId() != null && !request.nationalId().isBlank()
                && donorRepository.existsByNationalId(request.nationalId())) {
            throw new ConflictException("Donor with national ID '" + request.nationalId() + "' already exists");
        }

        Donor donor = donorMapper.toEntity(request);
        donor.setDonorNumber("DN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donor.setStatus(DonorStatusEnum.ACTIVE);
        donor.setTotalDonations(0);
        donor.setRegistrationDate(LocalDate.now());
        donor.setBranchId(request.branchId());

        donor = donorRepository.save(donor);
        log.info("Donor registered with number: {}", donor.getDonorNumber());
        return donorMapper.toResponse(donor);
    }

    @Transactional
    public DonorResponse updateDonor(UUID id, DonorUpdateRequest request) {
        log.info("Updating donor: {}", id);

        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", id));

        if (request.firstName() != null) donor.setFirstName(request.firstName());
        if (request.lastName() != null) donor.setLastName(request.lastName());
        if (request.dateOfBirth() != null) donor.setDateOfBirth(request.dateOfBirth());
        if (request.gender() != null) donor.setGender(request.gender());
        if (request.bloodGroupId() != null) donor.setBloodGroupId(request.bloodGroupId());
        if (request.rhFactor() != null) donor.setRhFactor(request.rhFactor());
        if (request.email() != null) donor.setEmail(request.email());
        if (request.phone() != null) donor.setPhone(request.phone());
        if (request.addressLine1() != null) donor.setAddressLine1(request.addressLine1());
        if (request.addressLine2() != null) donor.setAddressLine2(request.addressLine2());
        if (request.cityId() != null) donor.setCityId(request.cityId());
        if (request.postalCode() != null) donor.setPostalCode(request.postalCode());
        if (request.nationalId() != null) donor.setNationalId(request.nationalId());
        if (request.nationality() != null) donor.setNationality(request.nationality());
        if (request.occupation() != null) donor.setOccupation(request.occupation());

        donor = donorRepository.save(donor);
        return donorMapper.toResponse(donor);
    }

    public DonorResponse getDonorById(UUID id) {
        log.debug("Fetching donor by id: {}", id);
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", id));
        return donorMapper.toResponse(donor);
    }

    public DonorResponse getDonorByDonorNumber(String donorNumber) {
        log.debug("Fetching donor by donorNumber: {}", donorNumber);
        Donor donor = donorRepository.findByDonorNumber(donorNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "donorNumber", donorNumber));
        return donorMapper.toResponse(donor);
    }

    public PagedResponse<DonorResponse> searchDonors(String query, Pageable pageable) {
        log.debug("Searching donors with query: {}", query);
        Page<Donor> page = donorRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query, pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<DonorResponse> getDonorsByStatus(DonorStatusEnum status, Pageable pageable) {
        log.debug("Fetching donors by status: {}", status);
        Page<Donor> page = donorRepository.findByStatus(status, pageable);
        return toPagedResponse(page);
    }

    @Transactional
    public DonorDeferralResponse deferDonor(DonorDeferralCreateRequest request) {
        log.info("Deferring donor: {}", request.donorId());

        Donor donor = donorRepository.findById(request.donorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", request.donorId()));

        DonorDeferral deferral = deferralMapper.toEntity(request);
        deferral.setDeferralDate(LocalDate.now());
        deferral.setStatus(DeferralStatusEnum.ACTIVE);
        deferral.setBranchId(request.branchId());

        if (request.deferralType() == DeferralTypeEnum.PERMANENT) {
            donor.setStatus(DonorStatusEnum.PERMANENTLY_DEFERRED);
        } else {
            donor.setStatus(DonorStatusEnum.DEFERRED);
        }

        donorRepository.save(donor);
        deferral = deferralRepository.save(deferral);
        log.info("Donor {} deferred with type: {}", donor.getDonorNumber(), request.deferralType());
        return deferralMapper.toResponse(deferral);
    }

    public DonorHealthRecordResponse checkEligibility(UUID donorId) {
        log.info("Checking eligibility for donor: {}", donorId);

        donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", donorId));

        DonorHealthRecord latestRecord = healthRecordRepository
                .findFirstByDonorIdOrderByScreeningDateDesc(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("DonorHealthRecord", "donorId", donorId));

        return healthRecordMapper.toResponse(latestRecord);
    }

    @Transactional
    public DonorHealthRecordResponse createHealthRecord(DonorHealthRecordCreateRequest request) {
        log.info("Creating health record for donor: {}", request.donorId());

        donorRepository.findById(request.donorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", request.donorId()));

        DonorHealthRecord record = healthRecordMapper.toEntity(request);
        record.setScreeningDate(Instant.now());
        record.setBranchId(request.branchId());

        boolean eligible = determineEligibility(request);
        record.setEligible(eligible);

        record = healthRecordRepository.save(record);
        log.info("Health record created for donor: {}, eligible: {}", request.donorId(), eligible);
        return healthRecordMapper.toResponse(record);
    }

    public List<DonorHealthRecordResponse> getHealthRecords(UUID donorId) {
        log.debug("Fetching health records for donor: {}", donorId);
        List<DonorHealthRecord> records = healthRecordRepository.findByDonorIdOrderByScreeningDateDesc(donorId);
        return healthRecordMapper.toResponseList(records);
    }

    @Transactional
    public DonorConsentResponse createConsent(DonorConsentCreateRequest request) {
        log.info("Creating consent for donor: {}", request.donorId());

        donorRepository.findById(request.donorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", request.donorId()));

        DonorConsent consent = consentMapper.toEntity(request);
        consent.setConsentDate(Instant.now());
        consent.setBranchId(request.branchId());

        consent = consentRepository.save(consent);
        return consentMapper.toResponse(consent);
    }

    public List<DonorConsentResponse> getConsents(UUID donorId) {
        log.debug("Fetching consents for donor: {}", donorId);
        List<DonorConsent> consents = consentRepository.findByDonorId(donorId);
        return consentMapper.toResponseList(consents);
    }

    private boolean determineEligibility(DonorHealthRecordCreateRequest request) {
        if (request.hemoglobinGdl() != null && request.hemoglobinGdl().compareTo(MIN_HEMOGLOBIN) < 0) {
            return false;
        }
        if (request.weightKg() != null && request.weightKg().compareTo(MIN_WEIGHT) < 0) {
            return false;
        }
        if (request.pulseRate() != null && (request.pulseRate() < MIN_PULSE || request.pulseRate() > MAX_PULSE)) {
            return false;
        }
        if (request.temperatureCelsius() != null && request.temperatureCelsius().compareTo(MAX_TEMPERATURE) > 0) {
            return false;
        }
        if (request.bloodPressureSystolic() != null
                && (request.bloodPressureSystolic() < MIN_SYSTOLIC || request.bloodPressureSystolic() > MAX_SYSTOLIC)) {
            return false;
        }
        if (request.bloodPressureDiastolic() != null
                && (request.bloodPressureDiastolic() < MIN_DIASTOLIC || request.bloodPressureDiastolic() > MAX_DIASTOLIC)) {
            return false;
        }
        return true;
    }

    private PagedResponse<DonorResponse> toPagedResponse(Page<Donor> page) {
        List<DonorResponse> content = donorMapper.toResponseList(page.getContent());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
