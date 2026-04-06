package com.bloodbank.donorservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.DonorStatusEnum;
import com.bloodbank.donorservice.dto.*;
import com.bloodbank.donorservice.entity.Donor;
import com.bloodbank.donorservice.entity.DonorConsent;
import com.bloodbank.donorservice.entity.DonorDeferral;
import com.bloodbank.donorservice.entity.DonorHealthRecord;
import com.bloodbank.donorservice.enums.DeferralStatusEnum;
import com.bloodbank.donorservice.enums.DeferralTypeEnum;
import com.bloodbank.donorservice.enums.DonorTypeEnum;
import com.bloodbank.donorservice.enums.GenderEnum;
import com.bloodbank.donorservice.mapper.DonorConsentMapper;
import com.bloodbank.donorservice.mapper.DonorDeferralMapper;
import com.bloodbank.donorservice.mapper.DonorHealthRecordMapper;
import com.bloodbank.donorservice.mapper.DonorMapper;
import com.bloodbank.donorservice.repository.DonorConsentRepository;
import com.bloodbank.donorservice.repository.DonorDeferralRepository;
import com.bloodbank.donorservice.repository.DonorHealthRecordRepository;
import com.bloodbank.donorservice.repository.DonorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private DonorHealthRecordRepository healthRecordRepository;

    @Mock
    private DonorDeferralRepository deferralRepository;

    @Mock
    private DonorConsentRepository consentRepository;

    @Mock
    private DonorMapper donorMapper;

    @Mock
    private DonorHealthRecordMapper healthRecordMapper;

    @Mock
    private DonorDeferralMapper deferralMapper;

    @Mock
    private DonorConsentMapper consentMapper;

    @InjectMocks
    private DonorService donorService;

    private UUID donorId;
    private UUID branchId;
    private UUID bloodGroupId;
    private UUID cityId;
    private Donor donor;
    private DonorResponse donorResponse;

    @BeforeEach
    void setUp() {
        donorId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();
        cityId = UUID.randomUUID();

        donor = new Donor();
        donor.setDonorNumber("DN-ABCD1234");
        donor.setFirstName("John");
        donor.setLastName("Doe");
        donor.setDateOfBirth(LocalDate.of(1990, 1, 15));
        donor.setGender(GenderEnum.MALE);
        donor.setBloodGroupId(bloodGroupId);
        donor.setRhFactor("POSITIVE");
        donor.setEmail("john.doe@example.com");
        donor.setPhone("+1234567890");
        donor.setNationalId("NAT-123456");
        donor.setDonorType(DonorTypeEnum.VOLUNTARY);
        donor.setStatus(DonorStatusEnum.ACTIVE);
        donor.setTotalDonations(0);
        donor.setRegistrationDate(LocalDate.now());
        donor.setBranchId(branchId);

        donorResponse = new DonorResponse(
                donorId, "DN-ABCD1234", "John", "Doe",
                LocalDate.of(1990, 1, 15), GenderEnum.MALE,
                bloodGroupId, "POSITIVE",
                "john.doe@example.com", "+1234567890",
                null, null, cityId, null,
                "NAT-123456", null, null,
                DonorTypeEnum.VOLUNTARY, DonorStatusEnum.ACTIVE,
                null, 0, LocalDate.now(), null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("registerDonor")
    class RegisterDonor {

        private DonorCreateRequest createRequest;

        @BeforeEach
        void setUp() {
            createRequest = new DonorCreateRequest(
                    "John", "Doe",
                    LocalDate.of(1990, 1, 15),
                    GenderEnum.MALE,
                    bloodGroupId, "POSITIVE",
                    "john.doe@example.com", "+1234567890",
                    "123 Main St", null, cityId, "90001",
                    "NAT-123456", "US", "Engineer",
                    DonorTypeEnum.VOLUNTARY, branchId
            );
        }

        @Test
        @DisplayName("should register donor successfully")
        void shouldRegisterDonorSuccessfully() {
            when(donorRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
            when(donorRepository.existsByNationalId("NAT-123456")).thenReturn(false);
            when(donorMapper.toEntity(createRequest)).thenReturn(donor);
            when(donorRepository.save(any(Donor.class))).thenReturn(donor);
            when(donorMapper.toResponse(donor)).thenReturn(donorResponse);

            DonorResponse result = donorService.registerDonor(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEqualTo("John");
            assertThat(result.lastName()).isEqualTo("Doe");
            verify(donorRepository).existsByEmail("john.doe@example.com");
            verify(donorRepository).existsByNationalId("NAT-123456");
            verify(donorRepository).save(any(Donor.class));
            verify(donorMapper).toResponse(donor);
        }

        @Test
        @DisplayName("should throw ConflictException when email already exists")
        void shouldThrowConflictExceptionWhenEmailExists() {
            when(donorRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            assertThatThrownBy(() -> donorService.registerDonor(createRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("john.doe@example.com");

            verify(donorRepository, never()).save(any(Donor.class));
        }

        @Test
        @DisplayName("should throw ConflictException when nationalId already exists")
        void shouldThrowConflictExceptionWhenNationalIdExists() {
            when(donorRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
            when(donorRepository.existsByNationalId("NAT-123456")).thenReturn(true);

            assertThatThrownBy(() -> donorService.registerDonor(createRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("NAT-123456");

            verify(donorRepository, never()).save(any(Donor.class));
        }

        @Test
        @DisplayName("should skip email check when email is null")
        void shouldSkipEmailCheckWhenEmailIsNull() {
            DonorCreateRequest requestNoEmail = new DonorCreateRequest(
                    "John", "Doe",
                    LocalDate.of(1990, 1, 15),
                    GenderEnum.MALE,
                    bloodGroupId, "POSITIVE",
                    null, "+1234567890",
                    "123 Main St", null, cityId, "90001",
                    "NAT-123456", "US", "Engineer",
                    DonorTypeEnum.VOLUNTARY, branchId
            );

            when(donorRepository.existsByNationalId("NAT-123456")).thenReturn(false);
            when(donorMapper.toEntity(requestNoEmail)).thenReturn(donor);
            when(donorRepository.save(any(Donor.class))).thenReturn(donor);
            when(donorMapper.toResponse(donor)).thenReturn(donorResponse);

            DonorResponse result = donorService.registerDonor(requestNoEmail);

            assertThat(result).isNotNull();
            verify(donorRepository, never()).existsByEmail(any());
        }
    }

    @Nested
    @DisplayName("updateDonor")
    class UpdateDonor {

        private DonorUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new DonorUpdateRequest(
                    "Jane", "Smith",
                    LocalDate.of(1991, 5, 20),
                    GenderEnum.FEMALE,
                    bloodGroupId, "NEGATIVE",
                    "jane.smith@example.com", "+9876543210",
                    "456 Oak Ave", "Apt 2", cityId, "10001",
                    "NAT-654321", "UK", "Doctor"
            );
        }

        @Test
        @DisplayName("should update donor successfully")
        void shouldUpdateDonorSuccessfully() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(donorRepository.save(any(Donor.class))).thenReturn(donor);
            when(donorMapper.toResponse(donor)).thenReturn(donorResponse);

            DonorResponse result = donorService.updateDonor(donorId, updateRequest);

            assertThat(result).isNotNull();
            verify(donorRepository).findById(donorId);
            verify(donorRepository).save(donor);
            verify(donorMapper).toResponse(donor);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when donor not found")
        void shouldThrowResourceNotFoundExceptionWhenDonorNotFound() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.updateDonor(donorId, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(donorRepository, never()).save(any(Donor.class));
        }

        @Test
        @DisplayName("should only update non-null fields")
        void shouldOnlyUpdateNonNullFields() {
            DonorUpdateRequest partialUpdate = new DonorUpdateRequest(
                    "UpdatedFirst", null,
                    null, null, null, null,
                    null, null, null, null,
                    null, null, null, null, null
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(donorRepository.save(any(Donor.class))).thenReturn(donor);
            when(donorMapper.toResponse(donor)).thenReturn(donorResponse);

            donorService.updateDonor(donorId, partialUpdate);

            assertThat(donor.getFirstName()).isEqualTo("UpdatedFirst");
            assertThat(donor.getLastName()).isEqualTo("Doe");
            verify(donorRepository).save(donor);
        }
    }

    @Nested
    @DisplayName("getDonorById")
    class GetDonorById {

        @Test
        @DisplayName("should return donor when found")
        void shouldReturnDonorWhenFound() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(donorMapper.toResponse(donor)).thenReturn(donorResponse);

            DonorResponse result = donorService.getDonorById(donorId);

            assertThat(result).isNotNull();
            assertThat(result.donorNumber()).isEqualTo("DN-ABCD1234");
            verify(donorRepository).findById(donorId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.getDonorById(donorId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getDonorByDonorNumber")
    class GetDonorByDonorNumber {

        @Test
        @DisplayName("should return donor when found")
        void shouldReturnDonorWhenFound() {
            when(donorRepository.findByDonorNumber("DN-ABCD1234")).thenReturn(Optional.of(donor));
            when(donorMapper.toResponse(donor)).thenReturn(donorResponse);

            DonorResponse result = donorService.getDonorByDonorNumber("DN-ABCD1234");

            assertThat(result).isNotNull();
            assertThat(result.donorNumber()).isEqualTo("DN-ABCD1234");
            verify(donorRepository).findByDonorNumber("DN-ABCD1234");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            when(donorRepository.findByDonorNumber("DN-INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.getDonorByDonorNumber("DN-INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("searchDonors")
    class SearchDonors {

        @Test
        @DisplayName("should return paginated results")
        void shouldReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Donor> page = new PageImpl<>(List.of(donor), pageable, 1);

            when(donorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    eq("John"), eq("John"), eq(pageable))).thenReturn(page);
            when(donorMapper.toResponseList(page.getContent())).thenReturn(List.of(donorResponse));

            PagedResponse<DonorResponse> result = donorService.searchDonors("John", pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("should return empty page when no matches")
        void shouldReturnEmptyPageWhenNoMatches() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Donor> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(donorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    eq("Unknown"), eq("Unknown"), eq(pageable))).thenReturn(emptyPage);
            when(donorMapper.toResponseList(emptyPage.getContent())).thenReturn(List.of());

            PagedResponse<DonorResponse> result = donorService.searchDonors("Unknown", pageable);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("getDonorsByStatus")
    class GetDonorsByStatus {

        @Test
        @DisplayName("should return paginated results")
        void shouldReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Donor> page = new PageImpl<>(List.of(donor), pageable, 1);

            when(donorRepository.findByStatus(DonorStatusEnum.ACTIVE, pageable)).thenReturn(page);
            when(donorMapper.toResponseList(page.getContent())).thenReturn(List.of(donorResponse));

            PagedResponse<DonorResponse> result = donorService.getDonorsByStatus(DonorStatusEnum.ACTIVE, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            verify(donorRepository).findByStatus(DonorStatusEnum.ACTIVE, pageable);
        }
    }

    @Nested
    @DisplayName("deferDonor")
    class DeferDonor {

        private DonorDeferralCreateRequest deferralRequest;
        private DonorDeferral deferral;
        private DonorDeferralResponse deferralResponse;
        private UUID deferralReasonId;

        @BeforeEach
        void setUp() {
            deferralReasonId = UUID.randomUUID();

            deferralRequest = new DonorDeferralCreateRequest(
                    donorId, deferralReasonId,
                    DeferralTypeEnum.TEMPORARY,
                    LocalDate.now().plusMonths(3),
                    "Low hemoglobin", "Dr. Smith", branchId
            );

            deferral = new DonorDeferral();
            deferral.setDonorId(donorId);
            deferral.setDeferralReasonId(deferralReasonId);
            deferral.setDeferralType(DeferralTypeEnum.TEMPORARY);
            deferral.setDeferralDate(LocalDate.now());
            deferral.setStatus(DeferralStatusEnum.ACTIVE);
            deferral.setBranchId(branchId);

            deferralResponse = new DonorDeferralResponse(
                    UUID.randomUUID(), donorId, deferralReasonId,
                    DeferralTypeEnum.TEMPORARY, LocalDate.now(),
                    LocalDate.now().plusMonths(3), "Low hemoglobin",
                    "Dr. Smith", DeferralStatusEnum.ACTIVE,
                    branchId, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("should defer donor successfully with TEMPORARY type")
        void shouldDeferDonorSuccessfully() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(deferralMapper.toEntity(deferralRequest)).thenReturn(deferral);
            when(deferralRepository.save(any(DonorDeferral.class))).thenReturn(deferral);
            when(donorRepository.save(any(Donor.class))).thenReturn(donor);
            when(deferralMapper.toResponse(deferral)).thenReturn(deferralResponse);

            DonorDeferralResponse result = donorService.deferDonor(deferralRequest);

            assertThat(result).isNotNull();
            assertThat(result.deferralType()).isEqualTo(DeferralTypeEnum.TEMPORARY);
            assertThat(donor.getStatus()).isEqualTo(DonorStatusEnum.DEFERRED);
            verify(donorRepository).save(donor);
            verify(deferralRepository).save(any(DonorDeferral.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when donor not found")
        void shouldThrowResourceNotFoundExceptionWhenDonorNotFound() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.deferDonor(deferralRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(deferralRepository, never()).save(any(DonorDeferral.class));
        }

        @Test
        @DisplayName("should set PERMANENTLY_DEFERRED for permanent deferral")
        void shouldSetPermanentlyDeferredForPermanentDeferral() {
            DonorDeferralCreateRequest permanentRequest = new DonorDeferralCreateRequest(
                    donorId, deferralReasonId,
                    DeferralTypeEnum.PERMANENT,
                    null, "HIV positive", "Dr. Smith", branchId
            );

            DonorDeferral permanentDeferral = new DonorDeferral();
            permanentDeferral.setDonorId(donorId);
            permanentDeferral.setDeferralType(DeferralTypeEnum.PERMANENT);
            permanentDeferral.setBranchId(branchId);

            DonorDeferralResponse permanentResponse = new DonorDeferralResponse(
                    UUID.randomUUID(), donorId, deferralReasonId,
                    DeferralTypeEnum.PERMANENT, LocalDate.now(),
                    null, "HIV positive", "Dr. Smith",
                    DeferralStatusEnum.ACTIVE, branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(deferralMapper.toEntity(permanentRequest)).thenReturn(permanentDeferral);
            when(deferralRepository.save(any(DonorDeferral.class))).thenReturn(permanentDeferral);
            when(donorRepository.save(any(Donor.class))).thenReturn(donor);
            when(deferralMapper.toResponse(permanentDeferral)).thenReturn(permanentResponse);

            DonorDeferralResponse result = donorService.deferDonor(permanentRequest);

            assertThat(result).isNotNull();
            assertThat(donor.getStatus()).isEqualTo(DonorStatusEnum.PERMANENTLY_DEFERRED);
            verify(donorRepository).save(donor);
        }
    }

    @Nested
    @DisplayName("checkEligibility")
    class CheckEligibility {

        private DonorHealthRecord healthRecord;
        private DonorHealthRecordResponse healthRecordResponse;

        @BeforeEach
        void setUp() {
            healthRecord = new DonorHealthRecord();
            healthRecord.setDonorId(donorId);
            healthRecord.setScreeningDate(Instant.now());
            healthRecord.setHemoglobinGdl(new BigDecimal("14.0"));
            healthRecord.setWeightKg(new BigDecimal("70"));
            healthRecord.setPulseRate(72);
            healthRecord.setTemperatureCelsius(new BigDecimal("36.5"));
            healthRecord.setBloodPressureSystolic(120);
            healthRecord.setBloodPressureDiastolic(80);
            healthRecord.setEligible(true);
            healthRecord.setBranchId(branchId);

            healthRecordResponse = new DonorHealthRecordResponse(
                    UUID.randomUUID(), donorId,
                    LocalDateTime.now(),
                    new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 72,
                    new BigDecimal("36.5"), new BigDecimal("14.0"),
                    true, null, "Nurse Jane",
                    branchId, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("should return health record with eligibility")
        void shouldReturnHealthRecordWithEligibility() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordRepository.findFirstByDonorIdOrderByScreeningDateDesc(donorId))
                    .thenReturn(Optional.of(healthRecord));
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            DonorHealthRecordResponse result = donorService.checkEligibility(donorId);

            assertThat(result).isNotNull();
            assertThat(result.isEligible()).isTrue();
            verify(healthRecordRepository).findFirstByDonorIdOrderByScreeningDateDesc(donorId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when donor not found")
        void shouldThrowResourceNotFoundExceptionWhenDonorNotFound() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.checkEligibility(donorId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when no health records exist")
        void shouldThrowResourceNotFoundExceptionWhenNoHealthRecords() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordRepository.findFirstByDonorIdOrderByScreeningDateDesc(donorId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.checkEligibility(donorId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createHealthRecord")
    class CreateHealthRecord {

        private DonorHealthRecord healthRecord;
        private DonorHealthRecordResponse healthRecordResponse;

        @BeforeEach
        void setUp() {
            healthRecord = new DonorHealthRecord();
            healthRecord.setDonorId(donorId);
            healthRecord.setBranchId(branchId);
        }

        @Test
        @DisplayName("should create health record with eligibility calculation")
        void shouldCreateHealthRecordWithEligibilityCalculation() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 72, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), "All clear", "Nurse Jane", branchId
            );

            healthRecordResponse = new DonorHealthRecordResponse(
                    UUID.randomUUID(), donorId, LocalDateTime.now(),
                    new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 72,
                    new BigDecimal("36.5"), new BigDecimal("14.0"),
                    true, "All clear", "Nurse Jane",
                    branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordMapper.toEntity(request)).thenReturn(healthRecord);
            when(healthRecordRepository.save(any(DonorHealthRecord.class))).thenReturn(healthRecord);
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            DonorHealthRecordResponse result = donorService.createHealthRecord(request);

            assertThat(result).isNotNull();
            assertThat(result.isEligible()).isTrue();
            verify(healthRecordRepository).save(any(DonorHealthRecord.class));
        }

        @Test
        @DisplayName("should mark as eligible when all criteria met")
        void shouldMarkAsEligibleWhenAllCriteriaMet() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("65"), new BigDecimal("170"),
                    130, 85, 75, new BigDecimal("37.0"),
                    new BigDecimal("13.5"), null, "Nurse Jane", branchId
            );

            healthRecordResponse = new DonorHealthRecordResponse(
                    UUID.randomUUID(), donorId, LocalDateTime.now(),
                    new BigDecimal("65"), new BigDecimal("170"),
                    130, 85, 75,
                    new BigDecimal("37.0"), new BigDecimal("13.5"),
                    true, null, "Nurse Jane",
                    branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordMapper.toEntity(request)).thenReturn(healthRecord);
            when(healthRecordRepository.save(any(DonorHealthRecord.class))).thenReturn(healthRecord);
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            DonorHealthRecordResponse result = donorService.createHealthRecord(request);

            assertThat(result).isNotNull();
            assertThat(healthRecord.isEligible()).isTrue();
        }

        @Test
        @DisplayName("should mark as not eligible when hemoglobin is low")
        void shouldMarkAsNotEligibleWhenHemoglobinIsLow() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 72, new BigDecimal("36.5"),
                    new BigDecimal("10.0"), null, "Nurse Jane", branchId
            );

            healthRecordResponse = new DonorHealthRecordResponse(
                    UUID.randomUUID(), donorId, LocalDateTime.now(),
                    new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 72,
                    new BigDecimal("36.5"), new BigDecimal("10.0"),
                    false, null, "Nurse Jane",
                    branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordMapper.toEntity(request)).thenReturn(healthRecord);
            when(healthRecordRepository.save(any(DonorHealthRecord.class))).thenReturn(healthRecord);
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            donorService.createHealthRecord(request);

            assertThat(healthRecord.isEligible()).isFalse();
        }

        @Test
        @DisplayName("should mark as not eligible when weight is low")
        void shouldMarkAsNotEligibleWhenWeightIsLow() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("45"), new BigDecimal("160"),
                    120, 80, 72, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), null, "Nurse Jane", branchId
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordMapper.toEntity(request)).thenReturn(healthRecord);
            when(healthRecordRepository.save(any(DonorHealthRecord.class))).thenReturn(healthRecord);
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            donorService.createHealthRecord(request);

            assertThat(healthRecord.isEligible()).isFalse();
        }

        @Test
        @DisplayName("should mark as not eligible when pulse is out of range")
        void shouldMarkAsNotEligibleWhenPulseIsOutOfRange() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 110, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), null, "Nurse Jane", branchId
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordMapper.toEntity(request)).thenReturn(healthRecord);
            when(healthRecordRepository.save(any(DonorHealthRecord.class))).thenReturn(healthRecord);
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            donorService.createHealthRecord(request);

            assertThat(healthRecord.isEligible()).isFalse();
        }

        @Test
        @DisplayName("should mark as not eligible when temperature is high")
        void shouldMarkAsNotEligibleWhenTemperatureIsHigh() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 72, new BigDecimal("38.5"),
                    new BigDecimal("14.0"), null, "Nurse Jane", branchId
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordMapper.toEntity(request)).thenReturn(healthRecord);
            when(healthRecordRepository.save(any(DonorHealthRecord.class))).thenReturn(healthRecord);
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            donorService.createHealthRecord(request);

            assertThat(healthRecord.isEligible()).isFalse();
        }

        @Test
        @DisplayName("should mark as not eligible when systolic BP is out of range")
        void shouldMarkAsNotEligibleWhenSystolicBpOutOfRange() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("70"), new BigDecimal("175"),
                    200, 80, 72, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), null, "Nurse Jane", branchId
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordMapper.toEntity(request)).thenReturn(healthRecord);
            when(healthRecordRepository.save(any(DonorHealthRecord.class))).thenReturn(healthRecord);
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            donorService.createHealthRecord(request);

            assertThat(healthRecord.isEligible()).isFalse();
        }

        @Test
        @DisplayName("should mark as not eligible when diastolic BP is out of range")
        void shouldMarkAsNotEligibleWhenDiastolicBpOutOfRange() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("70"), new BigDecimal("175"),
                    120, 110, 72, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), null, "Nurse Jane", branchId
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(healthRecordMapper.toEntity(request)).thenReturn(healthRecord);
            when(healthRecordRepository.save(any(DonorHealthRecord.class))).thenReturn(healthRecord);
            when(healthRecordMapper.toResponse(healthRecord)).thenReturn(healthRecordResponse);

            donorService.createHealthRecord(request);

            assertThat(healthRecord.isEligible()).isFalse();
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when donor not found")
        void shouldThrowResourceNotFoundExceptionWhenDonorNotFound() {
            DonorHealthRecordCreateRequest request = new DonorHealthRecordCreateRequest(
                    donorId, new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 72, new BigDecimal("36.5"),
                    new BigDecimal("14.0"), null, "Nurse Jane", branchId
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.createHealthRecord(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(healthRecordRepository, never()).save(any(DonorHealthRecord.class));
        }
    }

    @Nested
    @DisplayName("createConsent")
    class CreateConsent {

        @Test
        @DisplayName("should create consent successfully")
        void shouldCreateConsentSuccessfully() {
            DonorConsentCreateRequest request = new DonorConsentCreateRequest(
                    donorId, "BLOOD_DONATION", true,
                    "I consent to donate blood.", "SIG-REF-001",
                    "192.168.1.1", branchId
            );

            DonorConsent consent = new DonorConsent();
            consent.setDonorId(donorId);
            consent.setConsentType("BLOOD_DONATION");
            consent.setConsentGiven(true);
            consent.setBranchId(branchId);

            DonorConsentResponse consentResponse = new DonorConsentResponse(
                    UUID.randomUUID(), donorId, "BLOOD_DONATION",
                    true, LocalDateTime.now(), null,
                    "I consent to donate blood.", "SIG-REF-001",
                    "192.168.1.1", null, branchId, LocalDateTime.now()
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(consentMapper.toEntity(request)).thenReturn(consent);
            when(consentRepository.save(any(DonorConsent.class))).thenReturn(consent);
            when(consentMapper.toResponse(consent)).thenReturn(consentResponse);

            DonorConsentResponse result = donorService.createConsent(request);

            assertThat(result).isNotNull();
            assertThat(result.consentType()).isEqualTo("BLOOD_DONATION");
            assertThat(result.consentGiven()).isTrue();
            verify(consentRepository).save(any(DonorConsent.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when donor not found")
        void shouldThrowResourceNotFoundExceptionWhenDonorNotFound() {
            DonorConsentCreateRequest request = new DonorConsentCreateRequest(
                    donorId, "BLOOD_DONATION", true,
                    "I consent.", null, null, branchId
            );

            when(donorRepository.findById(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> donorService.createConsent(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(consentRepository, never()).save(any(DonorConsent.class));
        }
    }

    @Nested
    @DisplayName("getConsents")
    class GetConsents {

        @Test
        @DisplayName("should return list of consents")
        void shouldReturnListOfConsents() {
            DonorConsent consent = new DonorConsent();
            consent.setDonorId(donorId);
            consent.setConsentType("BLOOD_DONATION");

            DonorConsentResponse consentResponse = new DonorConsentResponse(
                    UUID.randomUUID(), donorId, "BLOOD_DONATION",
                    true, LocalDateTime.now(), null,
                    null, null, null, null,
                    branchId, LocalDateTime.now()
            );

            when(consentRepository.findByDonorId(donorId)).thenReturn(List.of(consent));
            when(consentMapper.toResponseList(List.of(consent))).thenReturn(List.of(consentResponse));

            List<DonorConsentResponse> result = donorService.getConsents(donorId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).consentType()).isEqualTo("BLOOD_DONATION");
            verify(consentRepository).findByDonorId(donorId);
        }

        @Test
        @DisplayName("should return empty list when no consents")
        void shouldReturnEmptyListWhenNoConsents() {
            when(consentRepository.findByDonorId(donorId)).thenReturn(List.of());
            when(consentMapper.toResponseList(List.of())).thenReturn(List.of());

            List<DonorConsentResponse> result = donorService.getConsents(donorId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getHealthRecords")
    class GetHealthRecords {

        @Test
        @DisplayName("should return list of health records")
        void shouldReturnListOfHealthRecords() {
            DonorHealthRecord record = new DonorHealthRecord();
            record.setDonorId(donorId);
            record.setEligible(true);

            DonorHealthRecordResponse recordResponse = new DonorHealthRecordResponse(
                    UUID.randomUUID(), donorId, LocalDateTime.now(),
                    new BigDecimal("70"), new BigDecimal("175"),
                    120, 80, 72,
                    new BigDecimal("36.5"), new BigDecimal("14.0"),
                    true, null, "Nurse Jane",
                    branchId, LocalDateTime.now()
            );

            when(healthRecordRepository.findByDonorIdOrderByScreeningDateDesc(donorId))
                    .thenReturn(List.of(record));
            when(healthRecordMapper.toResponseList(List.of(record)))
                    .thenReturn(List.of(recordResponse));

            List<DonorHealthRecordResponse> result = donorService.getHealthRecords(donorId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isEligible()).isTrue();
            verify(healthRecordRepository).findByDonorIdOrderByScreeningDateDesc(donorId);
        }

        @Test
        @DisplayName("should return empty list when no health records")
        void shouldReturnEmptyListWhenNoHealthRecords() {
            when(healthRecordRepository.findByDonorIdOrderByScreeningDateDesc(donorId))
                    .thenReturn(List.of());
            when(healthRecordMapper.toResponseList(List.of())).thenReturn(List.of());

            List<DonorHealthRecordResponse> result = donorService.getHealthRecords(donorId);

            assertThat(result).isEmpty();
        }
    }
}
