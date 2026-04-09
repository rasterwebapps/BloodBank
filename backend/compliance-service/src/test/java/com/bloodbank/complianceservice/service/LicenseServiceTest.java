package com.bloodbank.complianceservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.complianceservice.dto.LicenseCreateRequest;
import com.bloodbank.complianceservice.dto.LicenseResponse;
import com.bloodbank.complianceservice.entity.License;
import com.bloodbank.complianceservice.enums.LicenseStatusEnum;
import com.bloodbank.complianceservice.enums.LicenseTypeEnum;
import com.bloodbank.complianceservice.mapper.LicenseMapper;
import com.bloodbank.complianceservice.repository.LicenseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private LicenseMapper licenseMapper;

    @InjectMocks
    private LicenseService licenseService;

    private UUID licenseId;
    private UUID branchId;
    private License license;
    private LicenseResponse licenseResponse;
    private LicenseCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        licenseId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        license = new License();
        license.setId(licenseId);
        license.setLicenseType(LicenseTypeEnum.BLOOD_BANK);
        license.setLicenseNumber("LIC-12345");
        license.setIssuingAuthority("Health Authority");
        license.setIssueDate(LocalDate.of(2024, 1, 1));
        license.setExpiryDate(LocalDate.of(2025, 12, 31));
        license.setStatus(LicenseStatusEnum.ACTIVE);
        license.setBranchId(branchId);

        licenseResponse = new LicenseResponse(
                licenseId, LicenseTypeEnum.BLOOD_BANK, "LIC-12345",
                "Health Authority", LocalDate.of(2024, 1, 1),
                LocalDate.of(2025, 12, 31), null, null, null,
                LicenseStatusEnum.ACTIVE, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new LicenseCreateRequest(
                LicenseTypeEnum.BLOOD_BANK, "LIC-12345", "Health Authority",
                LocalDate.of(2024, 1, 1), LocalDate.of(2025, 12, 31),
                null, null, null, branchId
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create license successfully")
        void shouldCreateLicenseSuccessfully() {
            when(licenseMapper.toEntity(createRequest)).thenReturn(license);
            when(licenseRepository.save(any(License.class))).thenReturn(license);
            when(licenseMapper.toResponse(license)).thenReturn(licenseResponse);

            LicenseResponse result = licenseService.create(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.licenseNumber()).isEqualTo("LIC-12345");
            assertThat(result.licenseType()).isEqualTo(LicenseTypeEnum.BLOOD_BANK);
            verify(licenseRepository).save(any(License.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return license when found")
        void shouldReturnLicenseWhenFound() {
            when(licenseRepository.findById(licenseId)).thenReturn(Optional.of(license));
            when(licenseMapper.toResponse(license)).thenReturn(licenseResponse);

            LicenseResponse result = licenseService.getById(licenseId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(licenseId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(licenseRepository.findById(licenseId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> licenseService.getById(licenseId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByLicenseNumber")
    class GetByLicenseNumber {

        @Test
        @DisplayName("should return license by number")
        void shouldReturnByNumber() {
            when(licenseRepository.findByLicenseNumber("LIC-12345")).thenReturn(Optional.of(license));
            when(licenseMapper.toResponse(license)).thenReturn(licenseResponse);

            LicenseResponse result = licenseService.getByLicenseNumber("LIC-12345");

            assertThat(result.licenseNumber()).isEqualTo("LIC-12345");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when number not found")
        void shouldThrowWhenNumberNotFound() {
            when(licenseRepository.findByLicenseNumber("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> licenseService.getByLicenseNumber("UNKNOWN"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("should return licenses by status")
        void shouldReturnByStatus() {
            List<License> licenses = List.of(license);
            List<LicenseResponse> responses = List.of(licenseResponse);
            when(licenseRepository.findByStatus(LicenseStatusEnum.ACTIVE)).thenReturn(licenses);
            when(licenseMapper.toResponseList(licenses)).thenReturn(responses);

            List<LicenseResponse> result = licenseService.getByStatus(LicenseStatusEnum.ACTIVE);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getByType")
    class GetByType {

        @Test
        @DisplayName("should return licenses by type")
        void shouldReturnByType() {
            List<License> licenses = List.of(license);
            List<LicenseResponse> responses = List.of(licenseResponse);
            when(licenseRepository.findByLicenseType(LicenseTypeEnum.BLOOD_BANK)).thenReturn(licenses);
            when(licenseMapper.toResponseList(licenses)).thenReturn(responses);

            List<LicenseResponse> result = licenseService.getByType(LicenseTypeEnum.BLOOD_BANK);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getExpiringSoon")
    class GetExpiringSoon {

        @Test
        @DisplayName("should return licenses expiring before date")
        void shouldReturnExpiringSoon() {
            LocalDate beforeDate = LocalDate.of(2025, 6, 1);
            List<License> licenses = List.of(license);
            List<LicenseResponse> responses = List.of(licenseResponse);
            when(licenseRepository.findByExpiryDateBefore(beforeDate)).thenReturn(licenses);
            when(licenseMapper.toResponseList(licenses)).thenReturn(responses);

            List<LicenseResponse> result = licenseService.getExpiringSoon(beforeDate);

            assertThat(result).hasSize(1);
            verify(licenseRepository).findByExpiryDateBefore(beforeDate);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("should update status successfully")
        void shouldUpdateStatusSuccessfully() {
            when(licenseRepository.findById(licenseId)).thenReturn(Optional.of(license));
            when(licenseRepository.save(any(License.class))).thenReturn(license);
            when(licenseMapper.toResponse(license)).thenReturn(licenseResponse);

            LicenseResponse result = licenseService.updateStatus(licenseId, LicenseStatusEnum.EXPIRED);

            assertThat(result).isNotNull();
            verify(licenseRepository).save(any(License.class));
        }

        @Test
        @DisplayName("should throw BusinessException when license is revoked")
        void shouldThrowWhenRevoked() {
            license.setStatus(LicenseStatusEnum.REVOKED);
            when(licenseRepository.findById(licenseId)).thenReturn(Optional.of(license));

            assertThatThrownBy(() -> licenseService.updateStatus(licenseId, LicenseStatusEnum.ACTIVE))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(licenseRepository.findById(licenseId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> licenseService.updateStatus(licenseId, LicenseStatusEnum.EXPIRED))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("renew")
    class Renew {

        @Test
        @DisplayName("should renew license successfully")
        void shouldRenewSuccessfully() {
            LocalDate newExpiryDate = LocalDate.of(2026, 12, 31);
            when(licenseRepository.findById(licenseId)).thenReturn(Optional.of(license));
            when(licenseRepository.save(any(License.class))).thenReturn(license);
            when(licenseMapper.toResponse(license)).thenReturn(licenseResponse);

            LicenseResponse result = licenseService.renew(licenseId, newExpiryDate);

            assertThat(result).isNotNull();
            assertThat(license.getExpiryDate()).isEqualTo(newExpiryDate);
            assertThat(license.getStatus()).isEqualTo(LicenseStatusEnum.ACTIVE);
            assertThat(license.getRenewalDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("should throw BusinessException when license is revoked")
        void shouldThrowWhenRevoked() {
            license.setStatus(LicenseStatusEnum.REVOKED);
            when(licenseRepository.findById(licenseId)).thenReturn(Optional.of(license));

            assertThatThrownBy(() -> licenseService.renew(licenseId, LocalDate.of(2026, 12, 31)))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(licenseRepository.findById(licenseId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> licenseService.renew(licenseId, LocalDate.of(2026, 12, 31)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
