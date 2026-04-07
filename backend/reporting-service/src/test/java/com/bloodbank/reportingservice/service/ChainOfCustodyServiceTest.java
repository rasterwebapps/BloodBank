package com.bloodbank.reportingservice.service;

import com.bloodbank.reportingservice.dto.ChainOfCustodyCreateRequest;
import com.bloodbank.reportingservice.dto.ChainOfCustodyResponse;
import com.bloodbank.reportingservice.entity.ChainOfCustody;
import com.bloodbank.reportingservice.enums.CustodyEventEnum;
import com.bloodbank.reportingservice.mapper.ChainOfCustodyMapper;
import com.bloodbank.reportingservice.repository.ChainOfCustodyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChainOfCustodyServiceTest {

    @Mock
    private ChainOfCustodyRepository chainOfCustodyRepository;

    @Mock
    private ChainOfCustodyMapper chainOfCustodyMapper;

    @InjectMocks
    private ChainOfCustodyService chainOfCustodyService;

    private UUID entityId;
    private UUID branchId;
    private ChainOfCustody custody;
    private ChainOfCustodyResponse custodyResponse;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        custody = new ChainOfCustody("BloodUnit", entityId, CustodyEventEnum.COLLECTED,
                "handler-1", Instant.now());
        custody.setId(UUID.randomUUID());
        custody.setBranchId(branchId);
        custody.setFromLocation("Collection Room");
        custody.setToLocation("Lab");
        custody.setTemperature(new BigDecimal("4.50"));

        custodyResponse = new ChainOfCustodyResponse(
                custody.getId(), branchId, "BloodUnit", entityId,
                CustodyEventEnum.COLLECTED, "Collection Room", "Lab",
                "handler-1", new BigDecimal("4.50"), Instant.now(),
                null, LocalDateTime.now());
    }

    @Nested
    @DisplayName("addEvent")
    class AddEvent {

        @Test
        @DisplayName("should add custody event successfully")
        void shouldAddEvent() {
            ChainOfCustodyCreateRequest request = new ChainOfCustodyCreateRequest(
                    branchId, "BloodUnit", entityId, CustodyEventEnum.COLLECTED,
                    "Collection Room", "Lab", "handler-1",
                    new BigDecimal("4.50"), null);

            when(chainOfCustodyMapper.toEntity(request)).thenReturn(custody);
            when(chainOfCustodyRepository.save(any(ChainOfCustody.class))).thenReturn(custody);
            when(chainOfCustodyMapper.toResponse(custody)).thenReturn(custodyResponse);

            ChainOfCustodyResponse result = chainOfCustodyService.addEvent(request);

            assertThat(result).isNotNull();
            assertThat(result.custodyEvent()).isEqualTo(CustodyEventEnum.COLLECTED);
            assertThat(result.entityType()).isEqualTo("BloodUnit");
            verify(chainOfCustodyRepository).save(any(ChainOfCustody.class));
        }
    }

    @Nested
    @DisplayName("getByEntityId")
    class GetByEntityId {

        @Test
        @DisplayName("should return custody chain by entity")
        void shouldReturnByEntity() {
            when(chainOfCustodyRepository.findByEntityTypeAndEntityIdOrderByEventTimeAsc("BloodUnit", entityId))
                    .thenReturn(List.of(custody));
            when(chainOfCustodyMapper.toResponseList(List.of(custody)))
                    .thenReturn(List.of(custodyResponse));

            List<ChainOfCustodyResponse> result = chainOfCustodyService.getByEntityId("BloodUnit", entityId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).entityType()).isEqualTo("BloodUnit");
        }
    }

    @Nested
    @DisplayName("getFullChain")
    class GetFullChain {

        @Test
        @DisplayName("should return full custody chain for blood unit")
        void shouldReturnFullChain() {
            when(chainOfCustodyRepository.findByEntityTypeAndEntityIdOrderByEventTimeAsc("BloodUnit", entityId))
                    .thenReturn(List.of(custody));
            when(chainOfCustodyMapper.toResponseList(List.of(custody)))
                    .thenReturn(List.of(custodyResponse));

            List<ChainOfCustodyResponse> result = chainOfCustodyService.getFullChain(entityId);

            assertThat(result).hasSize(1);
        }
    }
}
