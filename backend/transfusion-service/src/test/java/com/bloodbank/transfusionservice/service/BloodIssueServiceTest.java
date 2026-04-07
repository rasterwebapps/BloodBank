package com.bloodbank.transfusionservice.service;

import com.bloodbank.common.exceptions.BusinessException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.transfusionservice.dto.BloodIssueCreateRequest;
import com.bloodbank.transfusionservice.dto.BloodIssueResponse;
import com.bloodbank.transfusionservice.dto.EmergencyIssueCreateRequest;
import com.bloodbank.transfusionservice.dto.EmergencyIssueResponse;
import com.bloodbank.transfusionservice.entity.BloodIssue;
import com.bloodbank.transfusionservice.entity.EmergencyIssue;
import com.bloodbank.transfusionservice.enums.EmergencyTypeEnum;
import com.bloodbank.transfusionservice.enums.IssueStatusEnum;
import com.bloodbank.transfusionservice.mapper.BloodIssueMapper;
import com.bloodbank.transfusionservice.mapper.EmergencyIssueMapper;
import com.bloodbank.transfusionservice.repository.BloodIssueRepository;
import com.bloodbank.transfusionservice.repository.EmergencyIssueRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodIssueServiceTest {

    @Mock private BloodIssueRepository bloodIssueRepository;
    @Mock private EmergencyIssueRepository emergencyIssueRepository;
    @Mock private BloodIssueMapper bloodIssueMapper;
    @Mock private EmergencyIssueMapper emergencyIssueMapper;
    @InjectMocks private BloodIssueService bloodIssueService;

    private UUID issueId;
    private UUID branchId;
    private UUID componentId;
    private BloodIssue bloodIssue;
    private BloodIssueResponse bloodIssueResponse;

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        componentId = UUID.randomUUID();

        bloodIssue = new BloodIssue();
        bloodIssue.setId(issueId);
        bloodIssue.setIssueNumber("BI-ABCD1234");
        bloodIssue.setComponentId(componentId);
        bloodIssue.setPatientName("Jane Doe");
        bloodIssue.setPatientId("PAT-002");
        bloodIssue.setIssuedTo("Ward A");
        bloodIssue.setIssueDate(Instant.now());
        bloodIssue.setStatus(IssueStatusEnum.ISSUED);
        bloodIssue.setBranchId(branchId);

        bloodIssueResponse = new BloodIssueResponse(
                issueId, "BI-ABCD1234", null, componentId,
                "Jane Doe", "PAT-002", null, "Ward A", null,
                Instant.now(), null, IssueStatusEnum.ISSUED,
                null, null, branchId, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested @DisplayName("issueBlood")
    class IssueBlood {
        @Test @DisplayName("Should issue blood successfully")
        void issueBlood_success() {
            var request = new BloodIssueCreateRequest(null, componentId, "Jane Doe", "PAT-002",
                    null, "Ward A", null, null, branchId);
            when(bloodIssueMapper.toEntity(request)).thenReturn(bloodIssue);
            when(bloodIssueRepository.save(any(BloodIssue.class))).thenReturn(bloodIssue);
            when(bloodIssueMapper.toResponse(bloodIssue)).thenReturn(bloodIssueResponse);

            var result = bloodIssueService.issueBlood(request);
            assertThat(result.patientName()).isEqualTo("Jane Doe");
            assertThat(result.status()).isEqualTo(IssueStatusEnum.ISSUED);
        }
    }

    @Nested @DisplayName("issueEmergencyBlood")
    class IssueEmergencyBlood {
        @Test @DisplayName("Should issue emergency blood successfully")
        void issueEmergencyBlood_success() {
            var request = new EmergencyIssueCreateRequest(componentId, "Jane Doe", "PAT-002", null,
                    "Ward A", null, EmergencyTypeEnum.TRAUMA, "Dr. Smith", "Major trauma", null, branchId);
            BloodIssue saved = new BloodIssue();
            saved.setId(issueId);
            saved.setBranchId(branchId);
            EmergencyIssue emergency = new EmergencyIssue();
            emergency.setBloodIssueId(issueId);
            emergency.setEmergencyType(EmergencyTypeEnum.TRAUMA);
            emergency.setBranchId(branchId);
            var resp = new EmergencyIssueResponse(UUID.randomUUID(), issueId, EmergencyTypeEnum.TRAUMA,
                    "Dr. Smith", Instant.now(), "Major trauma", false, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());

            when(bloodIssueRepository.save(any(BloodIssue.class))).thenReturn(saved);
            when(emergencyIssueRepository.save(any(EmergencyIssue.class))).thenReturn(emergency);
            when(emergencyIssueMapper.toResponse(emergency)).thenReturn(resp);

            var result = bloodIssueService.issueEmergencyBlood(request);
            assertThat(result.emergencyType()).isEqualTo(EmergencyTypeEnum.TRAUMA);
            verify(bloodIssueRepository).save(any(BloodIssue.class));
            verify(emergencyIssueRepository).save(any(EmergencyIssue.class));
        }
    }

    @Nested @DisplayName("returnBlood")
    class ReturnBlood {
        @Test @DisplayName("Should return blood successfully")
        void returnBlood_success() {
            var returnedResp = new BloodIssueResponse(issueId, "BI-ABCD1234", null, componentId,
                    "Jane Doe", "PAT-002", null, "Ward A", null,
                    Instant.now(), Instant.now(), IssueStatusEnum.RETURNED,
                    "Not needed", null, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(bloodIssueRepository.findById(issueId)).thenReturn(Optional.of(bloodIssue));
            when(bloodIssueRepository.save(any(BloodIssue.class))).thenReturn(bloodIssue);
            when(bloodIssueMapper.toResponse(bloodIssue)).thenReturn(returnedResp);

            var result = bloodIssueService.returnBlood(issueId, "Not needed");
            assertThat(result.status()).isEqualTo(IssueStatusEnum.RETURNED);
        }

        @Test @DisplayName("Should throw when not found")
        void returnBlood_notFound() {
            when(bloodIssueRepository.findById(issueId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> bloodIssueService.returnBlood(issueId, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test @DisplayName("Should throw when status is not ISSUED")
        void returnBlood_invalidStatus() {
            bloodIssue.setStatus(IssueStatusEnum.TRANSFUSED);
            when(bloodIssueRepository.findById(issueId)).thenReturn(Optional.of(bloodIssue));
            assertThatThrownBy(() -> bloodIssueService.returnBlood(issueId, "reason"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested @DisplayName("getById")
    class GetById {
        @Test @DisplayName("Should return blood issue when found")
        void success() {
            when(bloodIssueRepository.findById(issueId)).thenReturn(Optional.of(bloodIssue));
            when(bloodIssueMapper.toResponse(bloodIssue)).thenReturn(bloodIssueResponse);
            assertThat(bloodIssueService.getById(issueId).patientName()).isEqualTo("Jane Doe");
        }

        @Test @DisplayName("Should throw when not found")
        void notFound() {
            when(bloodIssueRepository.findById(issueId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> bloodIssueService.getById(issueId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("getByIssueNumber")
    class GetByIssueNumber {
        @Test @DisplayName("Should return blood issue by number")
        void success() {
            when(bloodIssueRepository.findByIssueNumber("BI-ABCD1234")).thenReturn(Optional.of(bloodIssue));
            when(bloodIssueMapper.toResponse(bloodIssue)).thenReturn(bloodIssueResponse);
            assertThat(bloodIssueService.getByIssueNumber("BI-ABCD1234")).isNotNull();
        }

        @Test @DisplayName("Should throw when not found by number")
        void notFound() {
            when(bloodIssueRepository.findByIssueNumber("INVALID")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> bloodIssueService.getByIssueNumber("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
