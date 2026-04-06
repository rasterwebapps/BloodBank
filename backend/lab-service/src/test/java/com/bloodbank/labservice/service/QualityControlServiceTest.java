package com.bloodbank.labservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.labservice.dto.QualityControlCreateRequest;
import com.bloodbank.labservice.dto.QualityControlResponse;
import com.bloodbank.labservice.entity.LabInstrument;
import com.bloodbank.labservice.entity.QualityControlRecord;
import com.bloodbank.labservice.enums.InstrumentTypeEnum;
import com.bloodbank.labservice.enums.QcLevelEnum;
import com.bloodbank.labservice.enums.QcStatusEnum;
import com.bloodbank.labservice.mapper.QualityControlMapper;
import com.bloodbank.labservice.repository.LabInstrumentRepository;
import com.bloodbank.labservice.repository.QualityControlRecordRepository;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QualityControlServiceTest {

    @Mock
    private QualityControlRecordRepository qualityControlRecordRepository;

    @Mock
    private QualityControlMapper qualityControlMapper;

    @Mock
    private LabInstrumentRepository labInstrumentRepository;

    @InjectMocks
    private QualityControlService qualityControlService;

    private UUID recordId;
    private UUID instrumentId;
    private UUID branchId;
    private LabInstrument instrument;
    private QualityControlRecord record;
    private QualityControlResponse response;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        instrument = new LabInstrument("INS-001", "Analyzer-1", InstrumentTypeEnum.ANALYZER);
        instrument.setId(instrumentId);

        record = new QualityControlRecord(instrumentId, QcLevelEnum.NORMAL, "Hemoglobin", true);
        record.setId(recordId);
        record.setBranchId(branchId);

        response = new QualityControlResponse(
                recordId, instrumentId, Instant.now(), QcLevelEnum.NORMAL,
                "Hemoglobin", "14.0", "14.2", true, null, "tech1",
                QcStatusEnum.COMPLETED, branchId, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("createRecord")
    class CreateRecord {

        @Test
        @DisplayName("should create QC record with COMPLETED status when within range")
        void shouldCreateRecordWithCompletedStatusWhenWithinRange() {
            QualityControlCreateRequest request = new QualityControlCreateRequest(
                    instrumentId, QcLevelEnum.NORMAL, "Hemoglobin",
                    "14.0", "14.2", true, null, "tech1", branchId
            );

            when(labInstrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
            when(qualityControlMapper.toEntity(request)).thenReturn(record);
            when(qualityControlRecordRepository.save(any(QualityControlRecord.class))).thenReturn(record);
            when(qualityControlMapper.toResponse(record)).thenReturn(response);

            QualityControlResponse result = qualityControlService.createRecord(request);

            assertThat(result).isNotNull();
            assertThat(record.getStatus()).isEqualTo(QcStatusEnum.COMPLETED);
            verify(qualityControlRecordRepository).save(any(QualityControlRecord.class));
        }

        @Test
        @DisplayName("should create QC record with FAILED status when not within range")
        void shouldCreateRecordWithFailedStatusWhenNotWithinRange() {
            QualityControlCreateRequest request = new QualityControlCreateRequest(
                    instrumentId, QcLevelEnum.HIGH, "Hemoglobin",
                    "14.0", "18.5", false, "Recalibrate", "tech1", branchId
            );

            QualityControlRecord failedRecord = new QualityControlRecord(
                    instrumentId, QcLevelEnum.HIGH, "Hemoglobin", false);
            failedRecord.setId(recordId);

            QualityControlResponse failedResponse = new QualityControlResponse(
                    recordId, instrumentId, Instant.now(), QcLevelEnum.HIGH,
                    "Hemoglobin", "14.0", "18.5", false, "Recalibrate", "tech1",
                    QcStatusEnum.FAILED, branchId, LocalDateTime.now(), LocalDateTime.now()
            );

            when(labInstrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
            when(qualityControlMapper.toEntity(request)).thenReturn(failedRecord);
            when(qualityControlRecordRepository.save(any(QualityControlRecord.class))).thenReturn(failedRecord);
            when(qualityControlMapper.toResponse(failedRecord)).thenReturn(failedResponse);

            QualityControlResponse result = qualityControlService.createRecord(request);

            assertThat(result).isNotNull();
            assertThat(failedRecord.getStatus()).isEqualTo(QcStatusEnum.FAILED);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when instrument not found")
        void shouldThrowResourceNotFoundWhenInstrumentNotFound() {
            QualityControlCreateRequest request = new QualityControlCreateRequest(
                    instrumentId, QcLevelEnum.NORMAL, "Hemoglobin",
                    "14.0", "14.2", true, null, "tech1", branchId
            );

            when(labInstrumentRepository.findById(instrumentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> qualityControlService.createRecord(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getRecordsByInstrument")
    class GetRecordsByInstrument {

        @Test
        @DisplayName("should return records by instrument id")
        void shouldReturnRecordsByInstrument() {
            List<QualityControlRecord> records = List.of(record);
            List<QualityControlResponse> responses = List.of(response);
            when(qualityControlRecordRepository.findByInstrumentId(instrumentId)).thenReturn(records);
            when(qualityControlMapper.toResponseList(records)).thenReturn(responses);

            List<QualityControlResponse> result = qualityControlService.getRecordsByInstrument(instrumentId);

            assertThat(result).hasSize(1);
            verify(qualityControlRecordRepository).findByInstrumentId(instrumentId);
        }
    }

    @Nested
    @DisplayName("getRecordById")
    class GetRecordById {

        @Test
        @DisplayName("should return record when found")
        void shouldReturnRecordWhenFound() {
            when(qualityControlRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
            when(qualityControlMapper.toResponse(record)).thenReturn(response);

            QualityControlResponse result = qualityControlService.getRecordById(recordId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(recordId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(qualityControlRecordRepository.findById(recordId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> qualityControlService.getRecordById(recordId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
