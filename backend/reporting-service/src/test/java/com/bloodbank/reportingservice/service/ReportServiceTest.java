package com.bloodbank.reportingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.reportingservice.dto.ReportMetadataCreateRequest;
import com.bloodbank.reportingservice.dto.ReportMetadataResponse;
import com.bloodbank.reportingservice.entity.ReportMetadata;
import com.bloodbank.reportingservice.enums.OutputFormatEnum;
import com.bloodbank.reportingservice.enums.ReportTypeEnum;
import com.bloodbank.reportingservice.mapper.ReportMetadataMapper;
import com.bloodbank.reportingservice.repository.ReportMetadataRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class ReportServiceTest {

    @Mock
    private ReportMetadataRepository reportMetadataRepository;

    @Mock
    private ReportMetadataMapper reportMetadataMapper;

    @InjectMocks
    private ReportService reportService;

    private UUID reportId;
    private ReportMetadata report;
    private ReportMetadataResponse reportResponse;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();

        report = new ReportMetadata("RPT-001", "Daily Collection Report", ReportTypeEnum.OPERATIONAL);
        report.setId(reportId);
        report.setDescription("Daily report of blood collections");
        report.setOutputFormat(OutputFormatEnum.PDF);

        reportResponse = new ReportMetadataResponse(
                reportId, null, "RPT-001", "Daily Collection Report",
                ReportTypeEnum.OPERATIONAL, "Daily report of blood collections",
                null, null, OutputFormatEnum.PDF, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create report metadata successfully")
        void shouldCreate() {
            ReportMetadataCreateRequest request = new ReportMetadataCreateRequest(
                    null, "RPT-001", "Daily Collection Report",
                    ReportTypeEnum.OPERATIONAL, "Daily report", null, null, OutputFormatEnum.PDF);

            when(reportMetadataMapper.toEntity(request)).thenReturn(report);
            when(reportMetadataRepository.save(any(ReportMetadata.class))).thenReturn(report);
            when(reportMetadataMapper.toResponse(report)).thenReturn(reportResponse);

            ReportMetadataResponse result = reportService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.reportCode()).isEqualTo("RPT-001");
            verify(reportMetadataRepository).save(any(ReportMetadata.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return report when found")
        void shouldReturn() {
            when(reportMetadataRepository.findById(reportId)).thenReturn(Optional.of(report));
            when(reportMetadataMapper.toResponse(report)).thenReturn(reportResponse);

            ReportMetadataResponse result = reportService.getById(reportId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(reportId);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(reportMetadataRepository.findById(reportId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reportService.getById(reportId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByCode")
    class GetByCode {

        @Test
        @DisplayName("should return report by code")
        void shouldReturnByCode() {
            when(reportMetadataRepository.findByReportCode("RPT-001")).thenReturn(Optional.of(report));
            when(reportMetadataMapper.toResponse(report)).thenReturn(reportResponse);

            ReportMetadataResponse result = reportService.getByCode("RPT-001");

            assertThat(result.reportCode()).isEqualTo("RPT-001");
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(reportMetadataRepository.findByReportCode("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reportService.getByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByType")
    class GetByType {

        @Test
        @DisplayName("should return reports by type")
        void shouldReturnByType() {
            when(reportMetadataRepository.findByReportTypeAndActiveTrue(ReportTypeEnum.OPERATIONAL))
                    .thenReturn(List.of(report));
            when(reportMetadataMapper.toResponseList(List.of(report)))
                    .thenReturn(List.of(reportResponse));

            List<ReportMetadataResponse> result = reportService.getByType(ReportTypeEnum.OPERATIONAL);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAllActive")
    class GetAllActive {

        @Test
        @DisplayName("should return all active reports")
        void shouldReturnActive() {
            when(reportMetadataRepository.findByActiveTrue()).thenReturn(List.of(report));
            when(reportMetadataMapper.toResponseList(List.of(report)))
                    .thenReturn(List.of(reportResponse));

            List<ReportMetadataResponse> result = reportService.getAllActive();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update report successfully")
        void shouldUpdate() {
            ReportMetadataCreateRequest request = new ReportMetadataCreateRequest(
                    null, "RPT-001", "Updated Report",
                    ReportTypeEnum.OPERATIONAL, "Updated desc", null, null, OutputFormatEnum.EXCEL);

            when(reportMetadataRepository.findById(reportId)).thenReturn(Optional.of(report));
            when(reportMetadataRepository.save(any(ReportMetadata.class))).thenReturn(report);
            when(reportMetadataMapper.toResponse(report)).thenReturn(reportResponse);

            ReportMetadataResponse result = reportService.update(reportId, request);

            assertThat(result).isNotNull();
            verify(reportMetadataMapper).updateEntity(request, report);
            verify(reportMetadataRepository).save(report);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            ReportMetadataCreateRequest request = new ReportMetadataCreateRequest(
                    null, "RPT-001", "Updated Report",
                    ReportTypeEnum.OPERATIONAL, "Updated desc", null, null, OutputFormatEnum.EXCEL);

            when(reportMetadataRepository.findById(reportId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reportService.update(reportId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should soft delete report")
        void shouldDelete() {
            when(reportMetadataRepository.findById(reportId)).thenReturn(Optional.of(report));
            when(reportMetadataRepository.save(any(ReportMetadata.class))).thenReturn(report);

            reportService.delete(reportId);

            assertThat(report.isActive()).isFalse();
            verify(reportMetadataRepository).save(report);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(reportMetadataRepository.findById(reportId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reportService.delete(reportId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
