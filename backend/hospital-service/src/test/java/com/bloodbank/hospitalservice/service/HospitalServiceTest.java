package com.bloodbank.hospitalservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.hospitalservice.dto.HospitalCreateRequest;
import com.bloodbank.hospitalservice.dto.HospitalResponse;
import com.bloodbank.hospitalservice.entity.Hospital;
import com.bloodbank.hospitalservice.enums.HospitalStatusEnum;
import com.bloodbank.hospitalservice.enums.HospitalTypeEnum;
import com.bloodbank.hospitalservice.mapper.HospitalMapper;
import com.bloodbank.hospitalservice.repository.HospitalRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private HospitalMapper hospitalMapper;

    @InjectMocks
    private HospitalService hospitalService;

    private UUID hospitalId;
    private UUID branchId;
    private UUID cityId;
    private Hospital hospital;
    private HospitalResponse hospitalResponse;
    private HospitalCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        cityId = UUID.randomUUID();

        hospital = new Hospital();
        hospital.setHospitalCode("HSP-ABCD1234");
        hospital.setHospitalName("City General Hospital");
        hospital.setHospitalType(HospitalTypeEnum.GOVERNMENT);
        hospital.setStatus(HospitalStatusEnum.ACTIVE);
        hospital.setAddressLine1("123 Main St");
        hospital.setPhone("+1234567890");
        hospital.setEmail("info@citygeneral.com");
        hospital.setContactPerson("Dr. Smith");
        hospital.setLicenseNumber("LIC-001");
        hospital.setBedCount(500);
        hospital.setHasBloodBank(true);
        hospital.setBranchId(branchId);

        hospitalResponse = new HospitalResponse(
                hospitalId, "HSP-ABCD1234", "City General Hospital",
                HospitalTypeEnum.GOVERNMENT, "123 Main St", null,
                cityId, "62701", "+1234567890", "info@citygeneral.com",
                "Dr. Smith", "LIC-001", 500, true,
                HospitalStatusEnum.ACTIVE, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new HospitalCreateRequest(
                "City General Hospital", HospitalTypeEnum.GOVERNMENT,
                "123 Main St", null, cityId, "62701",
                "+1234567890", "info@citygeneral.com", "Dr. Smith",
                "LIC-001", 500, true, branchId
        );
    }

    @Nested
    @DisplayName("createHospital")
    class CreateHospital {

        @Test
        @DisplayName("Should create hospital successfully")
        void shouldCreateHospitalSuccessfully() {
            when(hospitalMapper.toEntity(createRequest)).thenReturn(hospital);
            when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);
            when(hospitalMapper.toResponse(hospital)).thenReturn(hospitalResponse);

            HospitalResponse result = hospitalService.createHospital(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.hospitalName()).isEqualTo("City General Hospital");
            assertThat(result.hospitalType()).isEqualTo(HospitalTypeEnum.GOVERNMENT);
            assertThat(result.status()).isEqualTo(HospitalStatusEnum.ACTIVE);
            verify(hospitalRepository).save(any(Hospital.class));
            verify(hospitalMapper).toEntity(createRequest);
            verify(hospitalMapper).toResponse(hospital);
        }

        @Test
        @DisplayName("Should set hospital code and status on creation")
        void shouldSetHospitalCodeAndStatus() {
            Hospital capturedHospital = new Hospital();
            when(hospitalMapper.toEntity(createRequest)).thenReturn(capturedHospital);
            when(hospitalRepository.save(any(Hospital.class))).thenAnswer(inv -> inv.getArgument(0));
            when(hospitalMapper.toResponse(any(Hospital.class))).thenReturn(hospitalResponse);

            hospitalService.createHospital(createRequest);

            assertThat(capturedHospital.getHospitalCode()).startsWith("HSP-");
            assertThat(capturedHospital.getStatus()).isEqualTo(HospitalStatusEnum.ACTIVE);
            assertThat(capturedHospital.getBranchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("updateHospital")
    class UpdateHospital {

        @Test
        @DisplayName("Should update hospital successfully")
        void shouldUpdateHospitalSuccessfully() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);
            when(hospitalMapper.toResponse(hospital)).thenReturn(hospitalResponse);

            HospitalResponse result = hospitalService.updateHospital(hospitalId, createRequest);

            assertThat(result).isNotNull();
            assertThat(result.hospitalName()).isEqualTo("City General Hospital");
            verify(hospitalRepository).findById(hospitalId);
            verify(hospitalRepository).save(hospital);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when hospital not found")
        void shouldThrowWhenHospitalNotFound() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalService.updateHospital(hospitalId, createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should update all fields on the entity")
        void shouldUpdateAllFields() {
            Hospital existingHospital = new Hospital();
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(existingHospital));
            when(hospitalRepository.save(any(Hospital.class))).thenReturn(existingHospital);
            when(hospitalMapper.toResponse(existingHospital)).thenReturn(hospitalResponse);

            hospitalService.updateHospital(hospitalId, createRequest);

            assertThat(existingHospital.getHospitalName()).isEqualTo("City General Hospital");
            assertThat(existingHospital.getHospitalType()).isEqualTo(HospitalTypeEnum.GOVERNMENT);
            assertThat(existingHospital.getAddressLine1()).isEqualTo("123 Main St");
            assertThat(existingHospital.getPhone()).isEqualTo("+1234567890");
            assertThat(existingHospital.getEmail()).isEqualTo("info@citygeneral.com");
            assertThat(existingHospital.getContactPerson()).isEqualTo("Dr. Smith");
            assertThat(existingHospital.getLicenseNumber()).isEqualTo("LIC-001");
            assertThat(existingHospital.getBedCount()).isEqualTo(500);
            assertThat(existingHospital.isHasBloodBank()).isTrue();
        }
    }

    @Nested
    @DisplayName("getHospitalById")
    class GetHospitalById {

        @Test
        @DisplayName("Should return hospital when found")
        void shouldReturnHospitalWhenFound() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(hospitalMapper.toResponse(hospital)).thenReturn(hospitalResponse);

            HospitalResponse result = hospitalService.getHospitalById(hospitalId);

            assertThat(result).isNotNull();
            assertThat(result.hospitalName()).isEqualTo("City General Hospital");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalService.getHospitalById(hospitalId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getHospitalByCode")
    class GetHospitalByCode {

        @Test
        @DisplayName("Should return hospital when code found")
        void shouldReturnHospitalByCode() {
            when(hospitalRepository.findByHospitalCode("HSP-ABCD1234")).thenReturn(Optional.of(hospital));
            when(hospitalMapper.toResponse(hospital)).thenReturn(hospitalResponse);

            HospitalResponse result = hospitalService.getHospitalByCode("HSP-ABCD1234");

            assertThat(result).isNotNull();
            assertThat(result.hospitalCode()).isEqualTo("HSP-ABCD1234");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when code not found")
        void shouldThrowWhenCodeNotFound() {
            when(hospitalRepository.findByHospitalCode("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalService.getHospitalByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllHospitals")
    class GetAllHospitals {

        @Test
        @DisplayName("Should return paged response of hospitals")
        void shouldReturnPagedHospitals() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Hospital> page = new PageImpl<>(List.of(hospital), pageable, 1);
            when(hospitalRepository.findAll(pageable)).thenReturn(page);
            when(hospitalMapper.toResponseList(List.of(hospital))).thenReturn(List.of(hospitalResponse));

            PagedResponse<HospitalResponse> result = hospitalService.getAllHospitals(pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isEqualTo(0);
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should return empty paged response when no hospitals")
        void shouldReturnEmptyPagedResponse() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Hospital> page = new PageImpl<>(List.of(), pageable, 0);
            when(hospitalRepository.findAll(pageable)).thenReturn(page);
            when(hospitalMapper.toResponseList(List.of())).thenReturn(List.of());

            PagedResponse<HospitalResponse> result = hospitalService.getAllHospitals(pageable);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("searchHospitals")
    class SearchHospitals {

        @Test
        @DisplayName("Should return hospitals matching search query")
        void shouldReturnMatchingHospitals() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Hospital> page = new PageImpl<>(List.of(hospital), pageable, 1);
            when(hospitalRepository.findByHospitalNameContainingIgnoreCase("City", pageable)).thenReturn(page);
            when(hospitalMapper.toResponseList(List.of(hospital))).thenReturn(List.of(hospitalResponse));

            PagedResponse<HospitalResponse> result = hospitalService.searchHospitals("City", pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).hospitalName()).isEqualTo("City General Hospital");
        }
    }

    @Nested
    @DisplayName("getHospitalsByStatus")
    class GetHospitalsByStatus {

        @Test
        @DisplayName("Should return hospitals by status")
        void shouldReturnHospitalsByStatus() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Hospital> page = new PageImpl<>(List.of(hospital), pageable, 1);
            when(hospitalRepository.findByStatus(HospitalStatusEnum.ACTIVE, pageable)).thenReturn(page);
            when(hospitalMapper.toResponseList(List.of(hospital))).thenReturn(List.of(hospitalResponse));

            PagedResponse<HospitalResponse> result = hospitalService.getHospitalsByStatus(HospitalStatusEnum.ACTIVE, pageable);

            assertThat(result.content()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateHospitalStatus")
    class UpdateHospitalStatus {

        @Test
        @DisplayName("Should update hospital status successfully")
        void shouldUpdateStatusSuccessfully() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);
            when(hospitalMapper.toResponse(hospital)).thenReturn(hospitalResponse);

            HospitalResponse result = hospitalService.updateHospitalStatus(hospitalId, HospitalStatusEnum.INACTIVE);

            assertThat(result).isNotNull();
            verify(hospitalRepository).save(hospital);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when hospital not found for status update")
        void shouldThrowWhenHospitalNotFoundForStatusUpdate() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hospitalService.updateHospitalStatus(hospitalId, HospitalStatusEnum.INACTIVE))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should set new status on the entity")
        void shouldSetNewStatus() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(hospitalRepository.save(any(Hospital.class))).thenReturn(hospital);
            when(hospitalMapper.toResponse(hospital)).thenReturn(hospitalResponse);

            hospitalService.updateHospitalStatus(hospitalId, HospitalStatusEnum.SUSPENDED);

            assertThat(hospital.getStatus()).isEqualTo(HospitalStatusEnum.SUSPENDED);
        }
    }
}
