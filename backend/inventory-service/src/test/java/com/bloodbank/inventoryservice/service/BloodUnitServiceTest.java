package com.bloodbank.inventoryservice.service;

import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.events.TestResultAvailableEvent;
import com.bloodbank.common.events.UnitReleasedEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.BloodUnitStatusEnum;
import com.bloodbank.inventoryservice.dto.BloodUnitCreateRequest;
import com.bloodbank.inventoryservice.dto.BloodUnitResponse;
import com.bloodbank.inventoryservice.entity.BloodUnit;
import com.bloodbank.inventoryservice.enums.TtiStatusEnum;
import com.bloodbank.inventoryservice.mapper.BloodUnitMapper;
import com.bloodbank.inventoryservice.repository.BloodUnitRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class BloodUnitServiceTest {

    @Mock
    private BloodUnitRepository bloodUnitRepository;

    @Mock
    private BloodUnitMapper bloodUnitMapper;

    @InjectMocks
    private BloodUnitService bloodUnitService;

    private UUID unitId;
    private UUID branchId;
    private UUID donorId;
    private UUID collectionId;
    private UUID bloodGroupId;
    private BloodUnit bloodUnit;
    private BloodUnitResponse bloodUnitResponse;

    @BeforeEach
    void setUp() {
        unitId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        donorId = UUID.randomUUID();
        collectionId = UUID.randomUUID();
        bloodGroupId = UUID.randomUUID();

        bloodUnit = new BloodUnit(collectionId, donorId, "BU-ABCD1234", bloodGroupId,
                "POSITIVE", 450, Instant.now(), Instant.now().plus(42, ChronoUnit.DAYS));
        bloodUnit.setId(unitId);
        bloodUnit.setBranchId(branchId);

        bloodUnitResponse = new BloodUnitResponse(
                unitId, collectionId, donorId, "BU-ABCD1234", bloodGroupId,
                "POSITIVE", 450, Instant.now(), Instant.now().plus(42, ChronoUnit.DAYS),
                BloodUnitStatusEnum.QUARANTINED, TtiStatusEnum.PENDING, null,
                branchId, LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("createBloodUnit")
    class CreateBloodUnit {

        @Test
        @DisplayName("should create blood unit successfully")
        void shouldCreateBloodUnit() {
            BloodUnitCreateRequest request = new BloodUnitCreateRequest(
                    collectionId, donorId, bloodGroupId, "POSITIVE", 450,
                    Instant.now(), Instant.now().plus(42, ChronoUnit.DAYS), null, branchId);

            when(bloodUnitMapper.toEntity(request)).thenReturn(bloodUnit);
            when(bloodUnitRepository.save(any(BloodUnit.class))).thenReturn(bloodUnit);
            when(bloodUnitMapper.toResponse(bloodUnit)).thenReturn(bloodUnitResponse);

            BloodUnitResponse result = bloodUnitService.createBloodUnit(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(unitId);
            verify(bloodUnitRepository).save(any(BloodUnit.class));
        }

        @Test
        @DisplayName("should set QUARANTINED status and PENDING TTI")
        void shouldSetInitialStatus() {
            BloodUnitCreateRequest request = new BloodUnitCreateRequest(
                    collectionId, donorId, bloodGroupId, "POSITIVE", 450,
                    Instant.now(), Instant.now().plus(42, ChronoUnit.DAYS), null, branchId);

            BloodUnit newUnit = new BloodUnit(collectionId, donorId, "BU-TEST", bloodGroupId,
                    "POSITIVE", 450, Instant.now(), Instant.now().plus(42, ChronoUnit.DAYS));
            when(bloodUnitMapper.toEntity(request)).thenReturn(newUnit);
            when(bloodUnitRepository.save(any(BloodUnit.class))).thenAnswer(inv -> inv.getArgument(0));
            when(bloodUnitMapper.toResponse(any(BloodUnit.class))).thenReturn(bloodUnitResponse);

            bloodUnitService.createBloodUnit(request);

            assertThat(newUnit.getStatus()).isEqualTo(BloodUnitStatusEnum.QUARANTINED);
            assertThat(newUnit.getTtiStatus()).isEqualTo(TtiStatusEnum.PENDING);
            assertThat(newUnit.getUnitNumber()).startsWith("BU-");
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return unit when found")
        void shouldReturnUnit() {
            when(bloodUnitRepository.findById(unitId)).thenReturn(Optional.of(bloodUnit));
            when(bloodUnitMapper.toResponse(bloodUnit)).thenReturn(bloodUnitResponse);

            BloodUnitResponse result = bloodUnitService.getById(unitId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(unitId);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrowWhenNotFound() {
            when(bloodUnitRepository.findById(unitId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodUnitService.getById(unitId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByUnitNumber")
    class GetByUnitNumber {

        @Test
        @DisplayName("should return unit by number")
        void shouldReturnByNumber() {
            when(bloodUnitRepository.findByUnitNumber("BU-ABCD1234")).thenReturn(Optional.of(bloodUnit));
            when(bloodUnitMapper.toResponse(bloodUnit)).thenReturn(bloodUnitResponse);

            BloodUnitResponse result = bloodUnitService.getByUnitNumber("BU-ABCD1234");

            assertThat(result.unitNumber()).isEqualTo("BU-ABCD1234");
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrowWhenNotFound() {
            when(bloodUnitRepository.findByUnitNumber("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodUnitService.getByUnitNumber("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByDonor")
    class GetByDonor {

        @Test
        @DisplayName("should return units by donor")
        void shouldReturnByDonor() {
            when(bloodUnitRepository.findByDonorId(donorId)).thenReturn(List.of(bloodUnit));
            when(bloodUnitMapper.toResponseList(List.of(bloodUnit))).thenReturn(List.of(bloodUnitResponse));

            List<BloodUnitResponse> result = bloodUnitService.getByDonor(donorId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getByStatus")
    class GetByStatus {

        @Test
        @DisplayName("should return units by status")
        void shouldReturnByStatus() {
            when(bloodUnitRepository.findByStatus(BloodUnitStatusEnum.AVAILABLE)).thenReturn(List.of(bloodUnit));
            when(bloodUnitMapper.toResponseList(List.of(bloodUnit))).thenReturn(List.of(bloodUnitResponse));

            List<BloodUnitResponse> result = bloodUnitService.getByStatus(BloodUnitStatusEnum.AVAILABLE);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("should update status successfully")
        void shouldUpdateStatus() {
            when(bloodUnitRepository.findById(unitId)).thenReturn(Optional.of(bloodUnit));
            when(bloodUnitRepository.save(any(BloodUnit.class))).thenReturn(bloodUnit);
            when(bloodUnitMapper.toResponse(bloodUnit)).thenReturn(bloodUnitResponse);

            BloodUnitResponse result = bloodUnitService.updateStatus(unitId, BloodUnitStatusEnum.AVAILABLE);

            assertThat(result).isNotNull();
            verify(bloodUnitRepository).save(bloodUnit);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrowWhenNotFound() {
            when(bloodUnitRepository.findById(unitId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodUnitService.updateStatus(unitId, BloodUnitStatusEnum.AVAILABLE))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createFromDonation")
    class CreateFromDonation {

        @Test
        @DisplayName("should create unit from donation event")
        void shouldCreateFromDonation() {
            DonationCompletedEvent event = new DonationCompletedEvent(
                    UUID.randomUUID(), donorId, branchId, Instant.now());

            when(bloodUnitRepository.save(any(BloodUnit.class))).thenAnswer(inv -> inv.getArgument(0));

            bloodUnitService.createFromDonation(event);

            ArgumentCaptor<BloodUnit> captor = ArgumentCaptor.forClass(BloodUnit.class);
            verify(bloodUnitRepository).save(captor.capture());

            BloodUnit saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(BloodUnitStatusEnum.QUARANTINED);
            assertThat(saved.getTtiStatus()).isEqualTo(TtiStatusEnum.PENDING);
            assertThat(saved.getBranchId()).isEqualTo(branchId);
            assertThat(saved.getDonorId()).isEqualTo(donorId);
            assertThat(saved.getUnitNumber()).startsWith("BU-");
        }
    }

    @Nested
    @DisplayName("updateTtiStatus")
    class UpdateTtiStatus {

        @Test
        @DisplayName("should update TTI status from test result event")
        void shouldUpdateTtiStatus() {
            TestResultAvailableEvent event = new TestResultAvailableEvent(
                    UUID.randomUUID(), unitId, branchId, Instant.now());

            when(bloodUnitRepository.findById(unitId)).thenReturn(Optional.of(bloodUnit));
            when(bloodUnitRepository.save(any(BloodUnit.class))).thenReturn(bloodUnit);

            bloodUnitService.updateTtiStatus(event);

            assertThat(bloodUnit.getTtiStatus()).isEqualTo(TtiStatusEnum.NEGATIVE);
            verify(bloodUnitRepository).save(bloodUnit);
        }
    }

    @Nested
    @DisplayName("markAsAvailable")
    class MarkAsAvailable {

        @Test
        @DisplayName("should mark unit as available from released event")
        void shouldMarkAsAvailable() {
            UnitReleasedEvent event = new UnitReleasedEvent(unitId, branchId, Instant.now());

            when(bloodUnitRepository.findById(unitId)).thenReturn(Optional.of(bloodUnit));
            when(bloodUnitRepository.save(any(BloodUnit.class))).thenReturn(bloodUnit);

            bloodUnitService.markAsAvailable(event);

            assertThat(bloodUnit.getStatus()).isEqualTo(BloodUnitStatusEnum.AVAILABLE);
            assertThat(bloodUnit.getTtiStatus()).isEqualTo(TtiStatusEnum.NEGATIVE);
            verify(bloodUnitRepository).save(bloodUnit);
        }
    }
}
