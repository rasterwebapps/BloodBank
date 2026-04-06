package com.bloodbank.labservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.labservice.dto.LabInstrumentCreateRequest;
import com.bloodbank.labservice.dto.LabInstrumentResponse;
import com.bloodbank.labservice.entity.LabInstrument;
import com.bloodbank.labservice.enums.InstrumentStatusEnum;
import com.bloodbank.labservice.enums.InstrumentTypeEnum;
import com.bloodbank.labservice.mapper.LabInstrumentMapper;
import com.bloodbank.labservice.repository.LabInstrumentRepository;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabInstrumentServiceTest {

    @Mock
    private LabInstrumentRepository labInstrumentRepository;

    @Mock
    private LabInstrumentMapper labInstrumentMapper;

    @InjectMocks
    private LabInstrumentService labInstrumentService;

    private UUID instrumentId;
    private UUID branchId;
    private LabInstrument instrument;
    private LabInstrumentResponse instrumentResponse;
    private LabInstrumentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        instrumentId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        instrument = new LabInstrument("INS-001", "Blood Analyzer X1", InstrumentTypeEnum.ANALYZER);
        instrument.setId(instrumentId);
        instrument.setBranchId(branchId);
        instrument.setStatus(InstrumentStatusEnum.ACTIVE);

        instrumentResponse = new LabInstrumentResponse(
                instrumentId, "INS-001", "Blood Analyzer X1", InstrumentTypeEnum.ANALYZER,
                "Beckman Coulter", "DXH 900", "SN-12345",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 12, 1), InstrumentStatusEnum.ACTIVE,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );

        createRequest = new LabInstrumentCreateRequest(
                "Blood Analyzer X1", InstrumentTypeEnum.ANALYZER,
                "Beckman Coulter", "DXH 900", "SN-12345",
                LocalDate.of(2024, 1, 15), LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 12, 1), branchId
        );
    }

    @Nested
    @DisplayName("createInstrument")
    class CreateInstrument {

        @Test
        @DisplayName("should create instrument successfully")
        void shouldCreateInstrumentSuccessfully() {
            when(labInstrumentMapper.toEntity(createRequest)).thenReturn(instrument);
            when(labInstrumentRepository.save(any(LabInstrument.class))).thenReturn(instrument);
            when(labInstrumentMapper.toResponse(instrument)).thenReturn(instrumentResponse);

            LabInstrumentResponse result = labInstrumentService.createInstrument(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.instrumentName()).isEqualTo("Blood Analyzer X1");
            verify(labInstrumentRepository).save(any(LabInstrument.class));
        }

        @Test
        @DisplayName("should set instrument code, ACTIVE status and branchId")
        void shouldSetInstrumentCodeAndActiveStatus() {
            LabInstrument freshInstrument = new LabInstrument(null, "Blood Analyzer X1", InstrumentTypeEnum.ANALYZER);
            when(labInstrumentMapper.toEntity(createRequest)).thenReturn(freshInstrument);
            when(labInstrumentRepository.save(any(LabInstrument.class))).thenAnswer(inv -> inv.getArgument(0));
            when(labInstrumentMapper.toResponse(any(LabInstrument.class))).thenReturn(instrumentResponse);

            labInstrumentService.createInstrument(createRequest);

            assertThat(freshInstrument.getInstrumentCode()).startsWith("INS-");
            assertThat(freshInstrument.getStatus()).isEqualTo(InstrumentStatusEnum.ACTIVE);
            assertThat(freshInstrument.getBranchId()).isEqualTo(branchId);
        }
    }

    @Nested
    @DisplayName("getInstrumentsByBranch")
    class GetInstrumentsByBranch {

        @Test
        @DisplayName("should return instruments by branch")
        void shouldReturnInstrumentsByBranch() {
            List<LabInstrument> instruments = List.of(instrument);
            List<LabInstrumentResponse> responses = List.of(instrumentResponse);
            when(labInstrumentRepository.findByBranchId(branchId)).thenReturn(instruments);
            when(labInstrumentMapper.toResponseList(instruments)).thenReturn(responses);

            List<LabInstrumentResponse> result = labInstrumentService.getInstrumentsByBranch(branchId);

            assertThat(result).hasSize(1);
            verify(labInstrumentRepository).findByBranchId(branchId);
        }
    }

    @Nested
    @DisplayName("getInstrumentById")
    class GetInstrumentById {

        @Test
        @DisplayName("should return instrument when found")
        void shouldReturnInstrumentWhenFound() {
            when(labInstrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
            when(labInstrumentMapper.toResponse(instrument)).thenReturn(instrumentResponse);

            LabInstrumentResponse result = labInstrumentService.getInstrumentById(instrumentId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(instrumentId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundWhenNotFound() {
            when(labInstrumentRepository.findById(instrumentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> labInstrumentService.getInstrumentById(instrumentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateInstrumentStatus")
    class UpdateInstrumentStatus {

        @Test
        @DisplayName("should update instrument status successfully")
        void shouldUpdateInstrumentStatusSuccessfully() {
            when(labInstrumentRepository.findById(instrumentId)).thenReturn(Optional.of(instrument));
            when(labInstrumentRepository.save(any(LabInstrument.class))).thenReturn(instrument);
            when(labInstrumentMapper.toResponse(instrument)).thenReturn(instrumentResponse);

            LabInstrumentResponse result = labInstrumentService.updateInstrumentStatus(
                    instrumentId, InstrumentStatusEnum.UNDER_MAINTENANCE);

            assertThat(result).isNotNull();
            assertThat(instrument.getStatus()).isEqualTo(InstrumentStatusEnum.UNDER_MAINTENANCE);
            verify(labInstrumentRepository).save(instrument);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when instrument not found")
        void shouldThrowResourceNotFoundWhenInstrumentNotFound() {
            when(labInstrumentRepository.findById(instrumentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> labInstrumentService.updateInstrumentStatus(
                    instrumentId, InstrumentStatusEnum.RETIRED))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
