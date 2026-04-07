package com.bloodbank.reportingservice.service;

import com.bloodbank.reportingservice.dto.AuditLogCreateRequest;
import com.bloodbank.reportingservice.dto.AuditLogResponse;
import com.bloodbank.reportingservice.entity.AuditLog;
import com.bloodbank.reportingservice.enums.AuditActionEnum;
import com.bloodbank.reportingservice.mapper.AuditLogMapper;
import com.bloodbank.reportingservice.repository.AuditLogRepository;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditService auditService;

    private UUID entityId;
    private UUID branchId;
    private AuditLog auditLog;
    private AuditLogResponse auditLogResponse;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        auditLog = new AuditLog("Donation", entityId, AuditActionEnum.CREATE,
                "user-1", "Donation created", Instant.now());
        auditLog.setId(UUID.randomUUID());
        auditLog.setBranchId(branchId);

        auditLogResponse = new AuditLogResponse(
                auditLog.getId(), branchId, "Donation", entityId,
                AuditActionEnum.CREATE, "user-1", "Test User", "AUDITOR",
                "127.0.0.1", null, null, "Donation created",
                Instant.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("createAuditLog")
    class CreateAuditLog {

        @Test
        @DisplayName("should create audit log successfully")
        void shouldCreateAuditLog() {
            AuditLogCreateRequest request = new AuditLogCreateRequest(
                    branchId, "Donation", entityId, AuditActionEnum.CREATE,
                    "user-1", "Test User", "AUDITOR", "127.0.0.1",
                    null, null, "Donation created");

            when(auditLogMapper.toEntity(request)).thenReturn(auditLog);
            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
            when(auditLogMapper.toResponse(auditLog)).thenReturn(auditLogResponse);

            AuditLogResponse result = auditService.createAuditLog(request);

            assertThat(result).isNotNull();
            assertThat(result.entityType()).isEqualTo("Donation");
            assertThat(result.action()).isEqualTo(AuditActionEnum.CREATE);
            verify(auditLogRepository).save(any(AuditLog.class));
        }
    }

    @Nested
    @DisplayName("getByEntityId")
    class GetByEntityId {

        @Test
        @DisplayName("should return audit logs by entity")
        void shouldReturnByEntity() {
            when(auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc("Donation", entityId))
                    .thenReturn(List.of(auditLog));
            when(auditLogMapper.toResponseList(List.of(auditLog)))
                    .thenReturn(List.of(auditLogResponse));

            List<AuditLogResponse> result = auditService.getByEntityId("Donation", entityId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).entityType()).isEqualTo("Donation");
        }
    }

    @Nested
    @DisplayName("getByActorId")
    class GetByActorId {

        @Test
        @DisplayName("should return audit logs by actor")
        void shouldReturnByActor() {
            when(auditLogRepository.findByActorIdOrderByTimestampDesc("user-1"))
                    .thenReturn(List.of(auditLog));
            when(auditLogMapper.toResponseList(List.of(auditLog)))
                    .thenReturn(List.of(auditLogResponse));

            List<AuditLogResponse> result = auditService.getByActorId("user-1");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getByAction")
    class GetByAction {

        @Test
        @DisplayName("should return audit logs by action")
        void shouldReturnByAction() {
            when(auditLogRepository.findByActionOrderByTimestampDesc(AuditActionEnum.CREATE))
                    .thenReturn(List.of(auditLog));
            when(auditLogMapper.toResponseList(List.of(auditLog)))
                    .thenReturn(List.of(auditLogResponse));

            List<AuditLogResponse> result = auditService.getByAction(AuditActionEnum.CREATE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).action()).isEqualTo(AuditActionEnum.CREATE);
        }
    }
}
