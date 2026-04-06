package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.ComponentStatusEnum;
import com.bloodbank.inventoryservice.dto.*;
import com.bloodbank.inventoryservice.entity.BloodComponent;
import com.bloodbank.inventoryservice.entity.ComponentLabel;
import com.bloodbank.inventoryservice.entity.ComponentProcessing;
import com.bloodbank.inventoryservice.entity.PooledComponent;
import com.bloodbank.inventoryservice.enums.LabelTypeEnum;
import com.bloodbank.inventoryservice.enums.ProcessResultEnum;
import com.bloodbank.inventoryservice.enums.ProcessTypeEnum;
import com.bloodbank.inventoryservice.mapper.BloodComponentMapper;
import com.bloodbank.inventoryservice.mapper.ComponentLabelMapper;
import com.bloodbank.inventoryservice.mapper.ComponentProcessingMapper;
import com.bloodbank.inventoryservice.mapper.PooledComponentMapper;
import com.bloodbank.inventoryservice.repository.BloodComponentRepository;
import com.bloodbank.inventoryservice.repository.ComponentLabelRepository;
import com.bloodbank.inventoryservice.repository.ComponentProcessingRepository;
import com.bloodbank.inventoryservice.repository.PooledComponentRepository;

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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloodComponentServiceTest {

    @Mock private BloodComponentRepository bloodComponentRepository;
    @Mock private ComponentProcessingRepository componentProcessingRepository;
    @Mock private ComponentLabelRepository componentLabelRepository;
    @Mock private PooledComponentRepository pooledComponentRepository;
    @Mock private BloodComponentMapper bloodComponentMapper;
    @Mock private ComponentProcessingMapper componentProcessingMapper;
    @Mock private ComponentLabelMapper componentLabelMapper;
    @Mock private PooledComponentMapper pooledComponentMapper;

    @InjectMocks
    private BloodComponentService bloodComponentService;

    private UUID componentId;
    private UUID branchId;
    private UUID bloodUnitId;
    private UUID componentTypeId;
    private UUID bloodGroupId;
    private BloodComponent bloodComponent;
    private BloodComponentResponse componentResponse;

    @BeforeEach
    void setUp() {
        componentId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        bloodUnitId = UUID.randomUUID();
        componentTypeId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();

        bloodComponent = new BloodComponent(bloodUnitId, componentTypeId, "BC-ABCD1234",
                bloodGroupId, 200, Instant.now().plus(35, ChronoUnit.DAYS));
        bloodComponent.setId(componentId);
        bloodComponent.setBranchId(branchId);

        componentResponse = new BloodComponentResponse(
                componentId, bloodUnitId, componentTypeId, "BC-ABCD1234",
                bloodGroupId, 200, BigDecimal.valueOf(220.5), Instant.now(),
                Instant.now().plus(35, ChronoUnit.DAYS), ComponentStatusEnum.QUARANTINED,
                null, false, false, branchId, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("createComponent")
    class CreateComponent {

        @Test
        @DisplayName("should create component successfully")
        void shouldCreate() {
            BloodComponentCreateRequest request = new BloodComponentCreateRequest(
                    bloodUnitId, componentTypeId, bloodGroupId, 200,
                    Instant.now().plus(35, ChronoUnit.DAYS), null, branchId);

            when(bloodComponentMapper.toEntity(request)).thenReturn(bloodComponent);
            when(bloodComponentRepository.save(any(BloodComponent.class))).thenReturn(bloodComponent);
            when(bloodComponentMapper.toResponse(bloodComponent)).thenReturn(componentResponse);

            BloodComponentResponse result = bloodComponentService.createComponent(request);

            assertThat(result).isNotNull();
            verify(bloodComponentRepository).save(any(BloodComponent.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return component when found")
        void shouldReturn() {
            when(bloodComponentRepository.findById(componentId)).thenReturn(Optional.of(bloodComponent));
            when(bloodComponentMapper.toResponse(bloodComponent)).thenReturn(componentResponse);

            BloodComponentResponse result = bloodComponentService.getById(componentId);

            assertThat(result.id()).isEqualTo(componentId);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(bloodComponentRepository.findById(componentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodComponentService.getById(componentId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByBloodUnit")
    class GetByBloodUnit {

        @Test
        @DisplayName("should return components")
        void shouldReturn() {
            when(bloodComponentRepository.findByBloodUnitId(bloodUnitId)).thenReturn(List.of(bloodComponent));
            when(bloodComponentMapper.toResponseList(List.of(bloodComponent))).thenReturn(List.of(componentResponse));

            List<BloodComponentResponse> result = bloodComponentService.getByBloodUnit(bloodUnitId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("should update status")
        void shouldUpdate() {
            when(bloodComponentRepository.findById(componentId)).thenReturn(Optional.of(bloodComponent));
            when(bloodComponentRepository.save(any(BloodComponent.class))).thenReturn(bloodComponent);
            when(bloodComponentMapper.toResponse(bloodComponent)).thenReturn(componentResponse);

            bloodComponentService.updateStatus(componentId, ComponentStatusEnum.AVAILABLE);

            verify(bloodComponentRepository).save(bloodComponent);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(bloodComponentRepository.findById(componentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodComponentService.updateStatus(componentId, ComponentStatusEnum.AVAILABLE))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("processComponent")
    class ProcessComponent {

        @Test
        @DisplayName("should process component")
        void shouldProcess() {
            ComponentProcessingCreateRequest request = new ComponentProcessingCreateRequest(
                    componentId, ProcessTypeEnum.SEPARATION, "tech1", "Centrifuge", null, branchId);

            ComponentProcessing processing = new ComponentProcessing(componentId, ProcessTypeEnum.SEPARATION, "tech1");
            processing.setId(UUID.randomUUID());

            ComponentProcessingResponse response = new ComponentProcessingResponse(
                    processing.getId(), componentId, ProcessTypeEnum.SEPARATION,
                    Instant.now(), "tech1", "Centrifuge", null,
                    ProcessResultEnum.SUCCESS, null, branchId, LocalDateTime.now());

            when(bloodComponentRepository.findById(componentId)).thenReturn(Optional.of(bloodComponent));
            when(componentProcessingMapper.toEntity(request)).thenReturn(processing);
            when(componentProcessingRepository.save(any(ComponentProcessing.class))).thenReturn(processing);
            when(componentProcessingMapper.toResponse(processing)).thenReturn(response);

            ComponentProcessingResponse result = bloodComponentService.processComponent(componentId, request);

            assertThat(result).isNotNull();
            verify(componentProcessingRepository).save(any(ComponentProcessing.class));
        }
    }

    @Nested
    @DisplayName("createLabel")
    class CreateLabel {

        @Test
        @DisplayName("should create label")
        void shouldCreate() {
            ComponentLabelCreateRequest request = new ComponentLabelCreateRequest(
                    componentId, LabelTypeEnum.ISBT128, "label-data", "tech1", branchId);

            ComponentLabel label = new ComponentLabel(componentId, LabelTypeEnum.ISBT128, "label-data", "tech1");
            label.setId(UUID.randomUUID());

            ComponentLabelResponse response = new ComponentLabelResponse(
                    label.getId(), componentId, LabelTypeEnum.ISBT128,
                    "label-data", Instant.now(), "tech1", 0, branchId, LocalDateTime.now());

            when(bloodComponentRepository.findById(componentId)).thenReturn(Optional.of(bloodComponent));
            when(componentLabelMapper.toEntity(request)).thenReturn(label);
            when(componentLabelRepository.save(any(ComponentLabel.class))).thenReturn(label);
            when(componentLabelMapper.toResponse(label)).thenReturn(response);

            ComponentLabelResponse result = bloodComponentService.createLabel(componentId, request);

            assertThat(result).isNotNull();
            verify(componentLabelRepository).save(any(ComponentLabel.class));
        }
    }

    @Nested
    @DisplayName("createPooledComponent")
    class CreatePooledComponent {

        @Test
        @DisplayName("should create pooled component")
        void shouldCreate() {
            PooledComponentCreateRequest request = new PooledComponentCreateRequest(
                    componentTypeId, bloodGroupId, 800, 4,
                    Instant.now().plus(5, ChronoUnit.DAYS), null, "tech1", null, branchId);

            PooledComponent pooled = new PooledComponent("PL-TEST", componentTypeId, bloodGroupId,
                    800, 4, Instant.now().plus(5, ChronoUnit.DAYS), "tech1");
            pooled.setId(UUID.randomUUID());

            PooledComponentResponse response = new PooledComponentResponse(
                    pooled.getId(), "PL-TEST", componentTypeId, bloodGroupId,
                    800, 4, Instant.now(), Instant.now().plus(5, ChronoUnit.DAYS),
                    ComponentStatusEnum.AVAILABLE, null, "tech1", null,
                    branchId, LocalDateTime.now());

            when(pooledComponentMapper.toEntity(request)).thenReturn(pooled);
            when(pooledComponentRepository.save(any(PooledComponent.class))).thenReturn(pooled);
            when(pooledComponentMapper.toResponse(pooled)).thenReturn(response);

            PooledComponentResponse result = bloodComponentService.createPooledComponent(request);

            assertThat(result).isNotNull();
            verify(pooledComponentRepository).save(any(PooledComponent.class));
        }
    }
}
