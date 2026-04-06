package com.bloodbank.transfusionservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.transfusionservice.dto.*;
import com.bloodbank.transfusionservice.entity.HemovigilanceReport;
import com.bloodbank.transfusionservice.entity.LookBackInvestigation;
import com.bloodbank.transfusionservice.entity.TransfusionReaction;
import com.bloodbank.transfusionservice.enums.*;
import com.bloodbank.transfusionservice.mapper.HemovigilanceReportMapper;
import com.bloodbank.transfusionservice.mapper.LookBackInvestigationMapper;
import com.bloodbank.transfusionservice.repository.HemovigilanceReportRepository;
import com.bloodbank.transfusionservice.repository.LookBackInvestigationRepository;
import com.bloodbank.transfusionservice.repository.TransfusionReactionRepository;

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
class HemovigilanceServiceTest {

    @Mock private HemovigilanceReportRepository reportRepository;
    @Mock private LookBackInvestigationRepository investigationRepository;
    @Mock private TransfusionReactionRepository reactionRepository;
    @Mock private HemovigilanceReportMapper reportMapper;
    @Mock private LookBackInvestigationMapper investigationMapper;
    @InjectMocks private HemovigilanceService hemovigilanceService;

    private UUID reportId, investigationId, reactionId, branchId, donorId;
    private HemovigilanceReport report;
    private HemovigilanceReportResponse reportResponse;
    private LookBackInvestigation investigation;
    private LookBackInvestigationResponse investigationResponse;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        investigationId = UUID.randomUUID();
        reactionId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        donorId = UUID.randomUUID();

        report = new HemovigilanceReport();
        report.setReportNumber("HV-ABCD1234");
        report.setTransfusionReactionId(reactionId);
        report.setReportDate(Instant.now());
        report.setReporterName("Dr. Smith");
        report.setStatus(HemovigilanceStatusEnum.OPEN);
        report.setBranchId(branchId);

        reportResponse = new HemovigilanceReportResponse(
                reportId, reactionId, "HV-ABCD1234", Instant.now(),
                ImputabilityEnum.PROBABLE, "Dr. Smith", "Doctor",
                null, null, false, null,
                HemovigilanceStatusEnum.OPEN, branchId, LocalDateTime.now(), LocalDateTime.now());

        investigation = new LookBackInvestigation();
        investigation.setInvestigationNumber("LB-ABCD1234");
        investigation.setDonorId(donorId);
        investigation.setInfectionType(InfectionTypeEnum.HBV);
        investigation.setStatus(LookBackStatusEnum.INITIATED);
        investigation.setBranchId(branchId);

        investigationResponse = new LookBackInvestigationResponse(
                investigationId, donorId, null, "LB-ABCD1234",
                Instant.now(), InfectionTypeEnum.HBV, 3, 2, 1,
                LookBackStatusEnum.INITIATED, null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested @DisplayName("createReport")
    class CreateReport {
        @Test @DisplayName("Should create hemovigilance report successfully")
        void success() {
            var request = new HemovigilanceReportCreateRequest(reactionId, ImputabilityEnum.PROBABLE,
                    "Dr. Smith", "Doctor", null, null, false, branchId);
            var reaction = new TransfusionReaction();
            when(reactionRepository.findById(reactionId)).thenReturn(Optional.of(reaction));
            when(reportMapper.toEntity(request)).thenReturn(report);
            when(reportRepository.save(any(HemovigilanceReport.class))).thenReturn(report);
            when(reportMapper.toResponse(report)).thenReturn(reportResponse);

            var result = hemovigilanceService.createReport(request);
            assertThat(result.reporterName()).isEqualTo("Dr. Smith");
            assertThat(result.status()).isEqualTo(HemovigilanceStatusEnum.OPEN);
        }

        @Test @DisplayName("Should throw when reaction not found")
        void reactionNotFound() {
            var request = new HemovigilanceReportCreateRequest(reactionId, ImputabilityEnum.PROBABLE,
                    "Dr. Smith", "Doctor", null, null, false, branchId);
            when(reactionRepository.findById(reactionId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> hemovigilanceService.createReport(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("updateReportStatus")
    class UpdateReportStatus {
        @Test @DisplayName("Should update report status successfully")
        void success() {
            var updatedResp = new HemovigilanceReportResponse(reportId, reactionId, "HV-ABCD1234", Instant.now(),
                    ImputabilityEnum.PROBABLE, "Dr. Smith", "Doctor", null, null, false, null,
                    HemovigilanceStatusEnum.UNDER_INVESTIGATION, branchId, LocalDateTime.now(), LocalDateTime.now());
            when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
            when(reportRepository.save(any(HemovigilanceReport.class))).thenReturn(report);
            when(reportMapper.toResponse(report)).thenReturn(updatedResp);

            var result = hemovigilanceService.updateReportStatus(reportId, "UNDER_INVESTIGATION");
            assertThat(result.status()).isEqualTo(HemovigilanceStatusEnum.UNDER_INVESTIGATION);
        }

        @Test @DisplayName("Should throw when report not found")
        void notFound() {
            when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> hemovigilanceService.updateReportStatus(reportId, "CLOSED"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("createLookBackInvestigation")
    class CreateLookBackInvestigation {
        @Test @DisplayName("Should create lookback investigation successfully")
        void success() {
            var request = new LookBackInvestigationCreateRequest(donorId, null, InfectionTypeEnum.HBV,
                    3, 2, 1, null, null, branchId);
            when(investigationMapper.toEntity(request)).thenReturn(investigation);
            when(investigationRepository.save(any(LookBackInvestigation.class))).thenReturn(investigation);
            when(investigationMapper.toResponse(investigation)).thenReturn(investigationResponse);

            var result = hemovigilanceService.createLookBackInvestigation(request);
            assertThat(result.infectionType()).isEqualTo(InfectionTypeEnum.HBV);
            assertThat(result.status()).isEqualTo(LookBackStatusEnum.INITIATED);
        }
    }

    @Nested @DisplayName("updateLookBackStatus")
    class UpdateLookBackStatus {
        @Test @DisplayName("Should update lookback status successfully")
        void success() {
            var updatedResp = new LookBackInvestigationResponse(investigationId, donorId, null, "LB-ABCD1234",
                    Instant.now(), InfectionTypeEnum.HBV, 3, 2, 1,
                    LookBackStatusEnum.IN_PROGRESS, "Findings", null, branchId,
                    LocalDateTime.now(), LocalDateTime.now());
            when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));
            when(investigationRepository.save(any(LookBackInvestigation.class))).thenReturn(investigation);
            when(investigationMapper.toResponse(investigation)).thenReturn(updatedResp);

            var result = hemovigilanceService.updateLookBackStatus(investigationId, "IN_PROGRESS", "Findings");
            assertThat(result.status()).isEqualTo(LookBackStatusEnum.IN_PROGRESS);
        }

        @Test @DisplayName("Should throw when investigation not found")
        void notFound() {
            when(investigationRepository.findById(investigationId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> hemovigilanceService.updateLookBackStatus(investigationId, "COMPLETED", null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("getReportById")
    class GetReportById {
        @Test @DisplayName("Should return report when found")
        void success() {
            when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
            when(reportMapper.toResponse(report)).thenReturn(reportResponse);
            assertThat(hemovigilanceService.getReportById(reportId)).isNotNull();
        }

        @Test @DisplayName("Should throw when not found")
        void notFound() {
            when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> hemovigilanceService.getReportById(reportId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested @DisplayName("getInvestigationById")
    class GetInvestigationById {
        @Test @DisplayName("Should return investigation when found")
        void success() {
            when(investigationRepository.findById(investigationId)).thenReturn(Optional.of(investigation));
            when(investigationMapper.toResponse(investigation)).thenReturn(investigationResponse);
            assertThat(hemovigilanceService.getInvestigationById(investigationId)).isNotNull();
        }

        @Test @DisplayName("Should throw when not found")
        void notFound() {
            when(investigationRepository.findById(investigationId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> hemovigilanceService.getInvestigationById(investigationId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
