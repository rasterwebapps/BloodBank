package com.bloodbank.branchservice.service;

import com.bloodbank.branchservice.dto.*;
import com.bloodbank.branchservice.entity.*;
import com.bloodbank.branchservice.mapper.BranchMapper;
import com.bloodbank.branchservice.repository.*;
import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchOperatingHoursRepository operatingHoursRepository;

    @Mock
    private BranchEquipmentRepository equipmentRepository;

    @Mock
    private BranchRegionRepository branchRegionRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private BranchMapper branchMapper;

    @InjectMocks
    private BranchService branchService;

    private UUID branchId;
    private UUID cityId;
    private UUID regionId;
    private UUID parentBranchId;
    private Branch branch;
    private City city;
    private Region region;
    private Country country;
    private BranchResponse branchResponse;

    @BeforeEach
    void setUp() {
        branchId = UUID.randomUUID();
        cityId = UUID.randomUUID();
        regionId = UUID.randomUUID();
        parentBranchId = UUID.randomUUID();

        country = new Country("US", "United States");
        country.setId(UUID.randomUUID());

        region = new Region(country, "CA", "California");
        region.setId(regionId);

        city = new City(region, "Los Angeles");
        city.setId(cityId);

        branch = new Branch("BR001", "Main Branch", "COLLECTION_CENTER", "123 Main St");
        branch.setId(branchId);
        branch.setCity(city);
        branch.setEmail("main@bloodbank.com");
        branch.setPhone("+1234567890");
        branch.setStatus("ACTIVE");

        branchResponse = new BranchResponse(
                branchId, "BR001", "Main Branch", "COLLECTION_CENTER",
                "123 Main St", null, cityId, "Los Angeles",
                null, "+1234567890", "main@bloodbank.com",
                null, null, null, null, "ACTIVE", null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("createBranch")
    class CreateBranch {

        private BranchCreateRequest createRequest;

        @BeforeEach
        void setUp() {
            createRequest = new BranchCreateRequest(
                    "BR001", "Main Branch", "COLLECTION_CENTER",
                    "123 Main St", null, cityId, "90001",
                    "+1234567890", "main@bloodbank.com",
                    "LIC-001", LocalDate.now().plusYears(1),
                    new BigDecimal("34.0522"), new BigDecimal("-118.2437"),
                    null
            );
        }

        @Test
        @DisplayName("should create branch successfully")
        void shouldCreateBranchSuccessfully() {
            when(branchRepository.existsByBranchCode("BR001")).thenReturn(false);
            when(branchRepository.existsByEmail("main@bloodbank.com")).thenReturn(false);
            when(branchMapper.toEntity(createRequest)).thenReturn(branch);
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
            when(branchRepository.save(any(Branch.class))).thenReturn(branch);
            when(branchMapper.toResponse(branch)).thenReturn(branchResponse);

            BranchResponse result = branchService.createBranch(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.branchCode()).isEqualTo("BR001");
            assertThat(result.branchName()).isEqualTo("Main Branch");
            verify(branchRepository).save(any(Branch.class));
        }

        @Test
        @DisplayName("should create branch with parent branch")
        void shouldCreateBranchWithParentBranch() {
            Branch parentBranch = new Branch("BR000", "Parent Branch", "MAIN", "456 Parent St");
            parentBranch.setId(parentBranchId);

            BranchCreateRequest requestWithParent = new BranchCreateRequest(
                    "BR002", "Child Branch", "SATELLITE",
                    "789 Child St", null, cityId, "90002",
                    "+1234567891", "child@bloodbank.com",
                    null, null, null, null, parentBranchId
            );

            Branch childBranch = new Branch("BR002", "Child Branch", "SATELLITE", "789 Child St");
            childBranch.setId(UUID.randomUUID());

            BranchResponse childResponse = new BranchResponse(
                    childBranch.getId(), "BR002", "Child Branch", "SATELLITE",
                    "789 Child St", null, cityId, "Los Angeles",
                    "90002", "+1234567891", "child@bloodbank.com",
                    null, null, null, null, "ACTIVE", parentBranchId,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(branchRepository.existsByBranchCode("BR002")).thenReturn(false);
            when(branchRepository.existsByEmail("child@bloodbank.com")).thenReturn(false);
            when(branchMapper.toEntity(requestWithParent)).thenReturn(childBranch);
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
            when(branchRepository.findById(parentBranchId)).thenReturn(Optional.of(parentBranch));
            when(branchRepository.save(any(Branch.class))).thenReturn(childBranch);
            when(branchMapper.toResponse(childBranch)).thenReturn(childResponse);

            BranchResponse result = branchService.createBranch(requestWithParent);

            assertThat(result).isNotNull();
            assertThat(result.parentBranchId()).isEqualTo(parentBranchId);
            verify(branchRepository).findById(parentBranchId);
        }

        @Test
        @DisplayName("should throw ConflictException when branch code already exists")
        void shouldThrowConflictExceptionWhenBranchCodeExists() {
            when(branchRepository.existsByBranchCode("BR001")).thenReturn(true);

            assertThatThrownBy(() -> branchService.createBranch(createRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("BR001");

            verify(branchRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ConflictException when email already exists")
        void shouldThrowConflictExceptionWhenEmailExists() {
            when(branchRepository.existsByBranchCode("BR001")).thenReturn(false);
            when(branchRepository.existsByEmail("main@bloodbank.com")).thenReturn(true);

            assertThatThrownBy(() -> branchService.createBranch(createRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("main@bloodbank.com");

            verify(branchRepository, never()).save(any());
        }

        @Test
        @DisplayName("should skip email check when email is null")
        void shouldSkipEmailCheckWhenEmailIsNull() {
            BranchCreateRequest requestNoEmail = new BranchCreateRequest(
                    "BR003", "No Email Branch", "COLLECTION_CENTER",
                    "321 Elm St", null, null, null,
                    null, null, null, null, null, null, null
            );

            Branch branchNoEmail = new Branch("BR003", "No Email Branch", "COLLECTION_CENTER", "321 Elm St");
            branchNoEmail.setId(UUID.randomUUID());

            BranchResponse responseNoEmail = new BranchResponse(
                    branchNoEmail.getId(), "BR003", "No Email Branch", "COLLECTION_CENTER",
                    "321 Elm St", null, null, null,
                    null, null, null, null, null, null, null, "ACTIVE", null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(branchRepository.existsByBranchCode("BR003")).thenReturn(false);
            when(branchMapper.toEntity(requestNoEmail)).thenReturn(branchNoEmail);
            when(branchRepository.save(any(Branch.class))).thenReturn(branchNoEmail);
            when(branchMapper.toResponse(branchNoEmail)).thenReturn(responseNoEmail);

            BranchResponse result = branchService.createBranch(requestNoEmail);

            assertThat(result).isNotNull();
            verify(branchRepository, never()).existsByEmail(any());
        }
    }

    @Nested
    @DisplayName("updateBranch")
    class UpdateBranch {

        @Test
        @DisplayName("should update branch successfully")
        void shouldUpdateBranchSuccessfully() {
            BranchUpdateRequest updateRequest = new BranchUpdateRequest(
                    "Updated Branch", "HOSPITAL", "456 Updated St", "Suite 200",
                    cityId, "90002", "+9876543210", "updated@bloodbank.com",
                    "LIC-002", LocalDate.now().plusYears(2),
                    new BigDecimal("34.0600"), new BigDecimal("-118.2500"),
                    null
            );

            BranchResponse updatedResponse = new BranchResponse(
                    branchId, "BR001", "Updated Branch", "HOSPITAL",
                    "456 Updated St", "Suite 200", cityId, "Los Angeles",
                    "90002", "+9876543210", "updated@bloodbank.com",
                    "LIC-002", LocalDate.now().plusYears(2),
                    new BigDecimal("34.0600"), new BigDecimal("-118.2500"),
                    "ACTIVE", null, LocalDateTime.now(), LocalDateTime.now()
            );

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
            when(branchRepository.save(any(Branch.class))).thenReturn(branch);
            when(branchMapper.toResponse(branch)).thenReturn(updatedResponse);

            BranchResponse result = branchService.updateBranch(branchId, updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.branchName()).isEqualTo("Updated Branch");
            verify(branchRepository).save(any(Branch.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when branch not found")
        void shouldThrowResourceNotFoundExceptionWhenBranchNotFound() {
            UUID unknownId = UUID.randomUUID();
            BranchUpdateRequest updateRequest = new BranchUpdateRequest(
                    "Updated", null, null, null, null, null, null,
                    null, null, null, null, null, null
            );

            when(branchRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.updateBranch(unknownId, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Branch");

            verify(branchRepository, never()).save(any());
        }

        @Test
        @DisplayName("should clear parent branch when parentBranchId is null")
        void shouldClearParentBranchWhenNull() {
            Branch branchWithParent = new Branch("BR001", "Main Branch", "COLLECTION_CENTER", "123 Main St");
            branchWithParent.setId(branchId);
            Branch parent = new Branch("BR000", "Parent", "MAIN", "Parent St");
            parent.setId(parentBranchId);
            branchWithParent.setParentBranch(parent);

            BranchUpdateRequest updateRequest = new BranchUpdateRequest(
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null
            );

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branchWithParent));
            when(branchRepository.save(any(Branch.class))).thenReturn(branchWithParent);
            when(branchMapper.toResponse(branchWithParent)).thenReturn(branchResponse);

            branchService.updateBranch(branchId, updateRequest);

            assertThat(branchWithParent.getParentBranch()).isNull();
        }
    }

    @Nested
    @DisplayName("getBranchById")
    class GetBranchById {

        @Test
        @DisplayName("should return branch when found")
        void shouldReturnBranchWhenFound() {
            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(branchMapper.toResponse(branch)).thenReturn(branchResponse);

            BranchResponse result = branchService.getBranchById(branchId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowResourceNotFoundExceptionWhenNotFound() {
            UUID unknownId = UUID.randomUUID();
            when(branchRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.getBranchById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Branch");
        }
    }

    @Nested
    @DisplayName("getBranchByCode")
    class GetBranchByCode {

        @Test
        @DisplayName("should return branch when found by code")
        void shouldReturnBranchWhenFoundByCode() {
            when(branchRepository.findByBranchCode("BR001")).thenReturn(Optional.of(branch));
            when(branchMapper.toResponse(branch)).thenReturn(branchResponse);

            BranchResponse result = branchService.getBranchByCode("BR001");

            assertThat(result).isNotNull();
            assertThat(result.branchCode()).isEqualTo("BR001");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when code not found")
        void shouldThrowResourceNotFoundExceptionWhenCodeNotFound() {
            when(branchRepository.findByBranchCode("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.getBranchByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Branch");
        }
    }

    @Nested
    @DisplayName("getAllBranches")
    class GetAllBranches {

        @Test
        @DisplayName("should return paged branches")
        void shouldReturnPagedBranches() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Branch> page = new PageImpl<>(List.of(branch), pageable, 1);

            when(branchRepository.findAll(pageable)).thenReturn(page);
            when(branchMapper.toResponseList(page.getContent())).thenReturn(List.of(branchResponse));

            PagedResponse<BranchResponse> result = branchService.getAllBranches(pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("should return empty page when no branches")
        void shouldReturnEmptyPageWhenNoBranches() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Branch> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(branchRepository.findAll(pageable)).thenReturn(emptyPage);
            when(branchMapper.toResponseList(List.of())).thenReturn(List.of());

            PagedResponse<BranchResponse> result = branchService.getAllBranches(pageable);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("searchBranches")
    class SearchBranches {

        @Test
        @DisplayName("should search branches by name")
        void shouldSearchBranchesByName() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Branch> page = new PageImpl<>(List.of(branch), pageable, 1);

            when(branchRepository.findByBranchNameContainingIgnoreCase("Main", pageable)).thenReturn(page);
            when(branchMapper.toResponseList(page.getContent())).thenReturn(List.of(branchResponse));

            PagedResponse<BranchResponse> result = branchService.searchBranches("Main", pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).branchName()).isEqualTo("Main Branch");
        }
    }

    @Nested
    @DisplayName("getBranchesByStatus")
    class GetBranchesByStatus {

        @Test
        @DisplayName("should return branches by status")
        void shouldReturnBranchesByStatus() {
            when(branchRepository.findByStatus("ACTIVE")).thenReturn(List.of(branch));
            when(branchMapper.toResponseList(List.of(branch))).thenReturn(List.of(branchResponse));

            List<BranchResponse> result = branchService.getBranchesByStatus("ACTIVE");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("getBranchesByType")
    class GetBranchesByType {

        @Test
        @DisplayName("should return branches by type")
        void shouldReturnBranchesByType() {
            when(branchRepository.findByBranchType("COLLECTION_CENTER")).thenReturn(List.of(branch));
            when(branchMapper.toResponseList(List.of(branch))).thenReturn(List.of(branchResponse));

            List<BranchResponse> result = branchService.getBranchesByType("COLLECTION_CENTER");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).branchType()).isEqualTo("COLLECTION_CENTER");
        }
    }

    @Nested
    @DisplayName("activateBranch / deactivateBranch")
    class ActivateDeactivateBranch {

        @Test
        @DisplayName("should activate branch")
        void shouldActivateBranch() {
            branch.setStatus("INACTIVE");
            BranchResponse activeResponse = new BranchResponse(
                    branchId, "BR001", "Main Branch", "COLLECTION_CENTER",
                    "123 Main St", null, cityId, "Los Angeles",
                    null, "+1234567890", "main@bloodbank.com",
                    null, null, null, null, "ACTIVE", null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(branchRepository.save(branch)).thenReturn(branch);
            when(branchMapper.toResponse(branch)).thenReturn(activeResponse);

            BranchResponse result = branchService.activateBranch(branchId);

            assertThat(result.status()).isEqualTo("ACTIVE");
            assertThat(branch.getStatus()).isEqualTo("ACTIVE");
            verify(branchRepository).save(branch);
        }

        @Test
        @DisplayName("should deactivate branch")
        void shouldDeactivateBranch() {
            BranchResponse inactiveResponse = new BranchResponse(
                    branchId, "BR001", "Main Branch", "COLLECTION_CENTER",
                    "123 Main St", null, cityId, "Los Angeles",
                    null, "+1234567890", "main@bloodbank.com",
                    null, null, null, null, "INACTIVE", null,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(branchRepository.save(branch)).thenReturn(branch);
            when(branchMapper.toResponse(branch)).thenReturn(inactiveResponse);

            BranchResponse result = branchService.deactivateBranch(branchId);

            assertThat(result.status()).isEqualTo("INACTIVE");
            assertThat(branch.getStatus()).isEqualTo("INACTIVE");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when activating non-existent branch")
        void shouldThrowResourceNotFoundWhenActivatingNonExistentBranch() {
            UUID unknownId = UUID.randomUUID();
            when(branchRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.activateBranch(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when deactivating non-existent branch")
        void shouldThrowResourceNotFoundWhenDeactivatingNonExistentBranch() {
            UUID unknownId = UUID.randomUUID();
            when(branchRepository.findById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.deactivateBranch(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Operating Hours")
    class OperatingHoursTests {

        @Test
        @DisplayName("should add operating hours")
        void shouldAddOperatingHours() {
            BranchOperatingHoursRequest request = new BranchOperatingHoursRequest(
                    "MONDAY", LocalTime.of(8, 0), LocalTime.of(17, 0), false
            );

            BranchOperatingHours hours = new BranchOperatingHours(
                    branch, "MONDAY", LocalTime.of(8, 0), LocalTime.of(17, 0)
            );
            hours.setId(UUID.randomUUID());

            BranchOperatingHoursResponse response = new BranchOperatingHoursResponse(
                    hours.getId(), branchId, "MONDAY", LocalTime.of(8, 0), LocalTime.of(17, 0), false
            );

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(branchMapper.toEntity(request)).thenReturn(hours);
            when(operatingHoursRepository.save(hours)).thenReturn(hours);
            when(branchMapper.toResponse(hours)).thenReturn(response);

            BranchOperatingHoursResponse result = branchService.addOperatingHours(branchId, request);

            assertThat(result).isNotNull();
            assertThat(result.dayOfWeek()).isEqualTo("MONDAY");
            assertThat(result.branchId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("should get operating hours for branch")
        void shouldGetOperatingHoursForBranch() {
            BranchOperatingHoursResponse response = new BranchOperatingHoursResponse(
                    UUID.randomUUID(), branchId, "MONDAY", LocalTime.of(8, 0), LocalTime.of(17, 0), false
            );

            BranchOperatingHours hours = new BranchOperatingHours(
                    branch, "MONDAY", LocalTime.of(8, 0), LocalTime.of(17, 0)
            );

            when(branchRepository.existsById(branchId)).thenReturn(true);
            when(operatingHoursRepository.findByBranchId(branchId)).thenReturn(List.of(hours));
            when(branchMapper.toOperatingHoursResponseList(List.of(hours))).thenReturn(List.of(response));

            List<BranchOperatingHoursResponse> result = branchService.getOperatingHours(branchId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).dayOfWeek()).isEqualTo("MONDAY");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when getting hours for non-existent branch")
        void shouldThrowResourceNotFoundWhenGettingHoursForNonExistentBranch() {
            UUID unknownId = UUID.randomUUID();
            when(branchRepository.existsById(unknownId)).thenReturn(false);

            assertThatThrownBy(() -> branchService.getOperatingHours(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Equipment")
    class EquipmentTests {

        @Test
        @DisplayName("should add equipment")
        void shouldAddEquipment() {
            BranchEquipmentRequest request = new BranchEquipmentRequest(
                    "Blood Analyzer", "ANALYZER", "SN-001",
                    "Sysmex", "XN-1000",
                    LocalDate.of(2023, 1, 15), LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 12, 1), null
            );

            BranchEquipment equipment = new BranchEquipment(branch, "Blood Analyzer", "ANALYZER");
            equipment.setId(UUID.randomUUID());

            BranchEquipmentResponse response = new BranchEquipmentResponse(
                    equipment.getId(), branchId, "Blood Analyzer", "ANALYZER",
                    "SN-001", "Sysmex", "XN-1000",
                    LocalDate.of(2023, 1, 15), LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 12, 1), "OPERATIONAL"
            );

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(branchMapper.toEntity(request)).thenReturn(equipment);
            when(equipmentRepository.save(equipment)).thenReturn(equipment);
            when(branchMapper.toResponse(equipment)).thenReturn(response);

            BranchEquipmentResponse result = branchService.addEquipment(branchId, request);

            assertThat(result).isNotNull();
            assertThat(result.equipmentName()).isEqualTo("Blood Analyzer");
            assertThat(result.status()).isEqualTo("OPERATIONAL");
        }

        @Test
        @DisplayName("should set default OPERATIONAL status when status is null")
        void shouldSetDefaultStatusWhenNull() {
            BranchEquipmentRequest request = new BranchEquipmentRequest(
                    "Centrifuge", "CENTRIFUGE", null, null, null,
                    null, null, null, null
            );

            BranchEquipment equipment = new BranchEquipment(branch, "Centrifuge", "CENTRIFUGE");
            equipment.setId(UUID.randomUUID());
            equipment.setStatus(null);

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(branchMapper.toEntity(request)).thenReturn(equipment);
            when(equipmentRepository.save(equipment)).thenReturn(equipment);
            when(branchMapper.toResponse(equipment)).thenReturn(
                    new BranchEquipmentResponse(equipment.getId(), branchId, "Centrifuge",
                            "CENTRIFUGE", null, null, null, null, null, null, "OPERATIONAL")
            );

            branchService.addEquipment(branchId, request);

            assertThat(equipment.getStatus()).isEqualTo("OPERATIONAL");
        }

        @Test
        @DisplayName("should get equipment for branch")
        void shouldGetEquipmentForBranch() {
            BranchEquipment equipment = new BranchEquipment(branch, "Blood Analyzer", "ANALYZER");
            BranchEquipmentResponse response = new BranchEquipmentResponse(
                    UUID.randomUUID(), branchId, "Blood Analyzer", "ANALYZER",
                    null, null, null, null, null, null, "OPERATIONAL"
            );

            when(branchRepository.existsById(branchId)).thenReturn(true);
            when(equipmentRepository.findByBranchId(branchId)).thenReturn(List.of(equipment));
            when(branchMapper.toEquipmentResponseList(List.of(equipment))).thenReturn(List.of(response));

            List<BranchEquipmentResponse> result = branchService.getEquipment(branchId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).equipmentName()).isEqualTo("Blood Analyzer");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when getting equipment for non-existent branch")
        void shouldThrowResourceNotFoundWhenGettingEquipmentForNonExistentBranch() {
            UUID unknownId = UUID.randomUUID();
            when(branchRepository.existsById(unknownId)).thenReturn(false);

            assertThatThrownBy(() -> branchService.getEquipment(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Branch Regions")
    class BranchRegionsTests {

        @Test
        @DisplayName("should add branch region")
        void shouldAddBranchRegion() {
            BranchRegionRequest request = new BranchRegionRequest(regionId, true);

            BranchRegion branchRegion = new BranchRegion(branch, region);
            branchRegion.setId(UUID.randomUUID());
            branchRegion.setPrimary(true);

            BranchRegionResponse response = new BranchRegionResponse(
                    branchRegion.getId(), branchId, regionId, "California", true
            );

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(branchRegionRepository.existsByBranchIdAndRegionId(branchId, regionId)).thenReturn(false);
            when(regionRepository.findById(regionId)).thenReturn(Optional.of(region));
            when(branchRegionRepository.save(any(BranchRegion.class))).thenReturn(branchRegion);
            when(branchMapper.toResponse(any(BranchRegion.class))).thenReturn(response);

            BranchRegionResponse result = branchService.addBranchRegion(branchId, request);

            assertThat(result).isNotNull();
            assertThat(result.regionName()).isEqualTo("California");
            assertThat(result.isPrimary()).isTrue();
        }

        @Test
        @DisplayName("should throw ConflictException when region already assigned")
        void shouldThrowConflictExceptionWhenRegionAlreadyAssigned() {
            BranchRegionRequest request = new BranchRegionRequest(regionId, false);

            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(branchRegionRepository.existsByBranchIdAndRegionId(branchId, regionId)).thenReturn(true);

            assertThatThrownBy(() -> branchService.addBranchRegion(branchId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(regionId.toString());

            verify(branchRegionRepository, never()).save(any());
        }

        @Test
        @DisplayName("should get branch regions")
        void shouldGetBranchRegions() {
            BranchRegion branchRegion = new BranchRegion(branch, region);
            BranchRegionResponse response = new BranchRegionResponse(
                    UUID.randomUUID(), branchId, regionId, "California", false
            );

            when(branchRepository.existsById(branchId)).thenReturn(true);
            when(branchRegionRepository.findByBranchId(branchId)).thenReturn(List.of(branchRegion));
            when(branchMapper.toBranchRegionResponseList(List.of(branchRegion))).thenReturn(List.of(response));

            List<BranchRegionResponse> result = branchService.getBranchRegions(branchId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).regionName()).isEqualTo("California");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when getting regions for non-existent branch")
        void shouldThrowResourceNotFoundWhenGettingRegionsForNonExistentBranch() {
            UUID unknownId = UUID.randomUUID();
            when(branchRepository.existsById(unknownId)).thenReturn(false);

            assertThatThrownBy(() -> branchService.getBranchRegions(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("should remove branch region")
        void shouldRemoveBranchRegion() {
            BranchRegion branchRegion = new BranchRegion(branch, region);
            branchRegion.setId(UUID.randomUUID());

            when(branchRegionRepository.findByBranchIdAndRegionId(branchId, regionId))
                    .thenReturn(Optional.of(branchRegion));

            branchService.removeBranchRegion(branchId, regionId);

            verify(branchRegionRepository).delete(branchRegion);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when removing non-existent branch region")
        void shouldThrowResourceNotFoundWhenRemovingNonExistentBranchRegion() {
            UUID unknownRegionId = UUID.randomUUID();
            when(branchRegionRepository.findByBranchIdAndRegionId(branchId, unknownRegionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.removeBranchRegion(branchId, unknownRegionId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("BranchRegion");
        }
    }
}
