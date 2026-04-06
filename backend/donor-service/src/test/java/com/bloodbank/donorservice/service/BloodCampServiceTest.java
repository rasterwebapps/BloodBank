package com.bloodbank.donorservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.CampCompletedEvent;
import com.bloodbank.common.exceptions.ConflictException;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.donorservice.dto.BloodCampCreateRequest;
import com.bloodbank.donorservice.dto.BloodCampResponse;
import com.bloodbank.donorservice.dto.BloodCampUpdateRequest;
import com.bloodbank.donorservice.dto.CampDonorCreateRequest;
import com.bloodbank.donorservice.dto.CampDonorResponse;
import com.bloodbank.donorservice.dto.CampResourceCreateRequest;
import com.bloodbank.donorservice.dto.CampResourceResponse;
import com.bloodbank.donorservice.entity.BloodCamp;
import com.bloodbank.donorservice.entity.CampDonor;
import com.bloodbank.donorservice.entity.CampResource;
import com.bloodbank.donorservice.entity.Collection;
import com.bloodbank.donorservice.entity.Donor;
import com.bloodbank.donorservice.enums.CampDonorStatusEnum;
import com.bloodbank.donorservice.enums.CampStatusEnum;
import com.bloodbank.donorservice.event.EventPublisher;
import com.bloodbank.donorservice.mapper.BloodCampMapper;
import com.bloodbank.donorservice.repository.BloodCampRepository;
import com.bloodbank.donorservice.repository.CampCollectionRepository;
import com.bloodbank.donorservice.repository.CampDonorRepository;
import com.bloodbank.donorservice.repository.CampResourceRepository;
import com.bloodbank.donorservice.repository.CollectionRepository;
import com.bloodbank.donorservice.repository.DonorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloodCampServiceTest {

    @Mock
    private BloodCampRepository campRepository;

    @Mock
    private CampResourceRepository resourceRepository;

    @Mock
    private CampDonorRepository campDonorRepository;

    @Mock
    private CampCollectionRepository campCollectionRepository;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private BloodCampMapper campMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private BloodCampService bloodCampService;

    private UUID campId;
    private UUID donorId;
    private UUID branchId;
    private BloodCamp camp;
    private BloodCampResponse campResponse;

    @BeforeEach
    void setUp() {
        campId = UUID.randomUUID();
        donorId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        camp = new BloodCamp();
        camp.setId(campId);
        camp.setCampCode("CAMP-ABCD1234");
        camp.setCampName("Community Blood Drive");
        camp.setOrganizerName("Red Cross");
        camp.setOrganizerContact("+1234567890");
        camp.setVenueName("City Hall");
        camp.setVenueAddress("123 Main Street");
        camp.setScheduledDate(LocalDate.now().plusDays(7));
        camp.setExpectedDonors(50);
        camp.setStatus(CampStatusEnum.PLANNED);
        camp.setTotalCollected(0);
        camp.setBranchId(branchId);

        campResponse = new BloodCampResponse(
                campId, "CAMP-ABCD1234", "Community Blood Drive",
                "Red Cross", "+1234567890", "City Hall",
                "123 Main Street", null, null, null,
                LocalDate.now().plusDays(7), null, null,
                50, null, 0, CampStatusEnum.PLANNED,
                null, null, branchId,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("createCamp")
    class CreateCamp {

        private BloodCampCreateRequest createRequest;

        @BeforeEach
        void setUp() {
            createRequest = new BloodCampCreateRequest(
                    "Community Blood Drive", "Red Cross", "+1234567890",
                    "City Hall", "123 Main Street", null,
                    null, null, LocalDate.now().plusDays(7),
                    50, null, null, branchId
            );
        }

        @Test
        @DisplayName("should create camp successfully")
        void shouldCreateCampSuccessfully() {
            when(campMapper.toEntity(createRequest)).thenReturn(camp);
            when(campRepository.save(any(BloodCamp.class))).thenReturn(camp);
            when(campMapper.toResponse(camp)).thenReturn(campResponse);

            BloodCampResponse result = bloodCampService.createCamp(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.campName()).isEqualTo("Community Blood Drive");
            assertThat(result.status()).isEqualTo(CampStatusEnum.PLANNED);
            verify(campRepository).save(any(BloodCamp.class));
        }
    }

    @Nested
    @DisplayName("updateCamp")
    class UpdateCamp {

        private BloodCampUpdateRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new BloodCampUpdateRequest(
                    "Updated Blood Drive", null, null,
                    "New Venue", null, null,
                    null, null, null,
                    100, null, "Updated notes"
            );
        }

        @Test
        @DisplayName("should update camp successfully")
        void shouldUpdateCampSuccessfully() {
            BloodCampResponse updatedResponse = new BloodCampResponse(
                    campId, "CAMP-ABCD1234", "Updated Blood Drive",
                    "Red Cross", "+1234567890", "New Venue",
                    "123 Main Street", null, null, null,
                    LocalDate.now().plusDays(7), null, null,
                    100, null, 0, CampStatusEnum.PLANNED,
                    null, "Updated notes", branchId,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(campRepository.save(any(BloodCamp.class))).thenReturn(camp);
            when(campMapper.toResponse(camp)).thenReturn(updatedResponse);

            BloodCampResponse result = bloodCampService.updateCamp(campId, updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.campName()).isEqualTo("Updated Blood Drive");
            assertThat(result.expectedDonors()).isEqualTo(100);
            verify(campRepository).save(any(BloodCamp.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when camp not found")
        void shouldThrowWhenCampNotFound() {
            when(campRepository.findById(campId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodCampService.updateCamp(campId, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(campRepository, never()).save(any(BloodCamp.class));
        }
    }

    @Nested
    @DisplayName("getCampById")
    class GetCampById {

        @Test
        @DisplayName("should return camp when found")
        void shouldReturnCampWhenFound() {
            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(campMapper.toResponse(camp)).thenReturn(campResponse);

            BloodCampResponse result = bloodCampService.getCampById(campId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(campId);
            assertThat(result.campName()).isEqualTo("Community Blood Drive");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(campRepository.findById(campId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodCampService.getCampById(campId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCampsByStatus")
    class GetCampsByStatus {

        @Test
        @DisplayName("should return paginated camps by status")
        void shouldReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<BloodCamp> page = new PageImpl<>(List.of(camp), pageable, 1);
            List<BloodCampResponse> responses = List.of(campResponse);

            when(campRepository.findByStatus(CampStatusEnum.PLANNED, pageable)).thenReturn(page);
            when(campMapper.toResponseList(page.getContent())).thenReturn(responses);

            PagedResponse<BloodCampResponse> result = bloodCampService.getCampsByStatus(
                    CampStatusEnum.PLANNED, pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isZero();
        }
    }

    @Nested
    @DisplayName("startCamp")
    class StartCamp {

        @Test
        @DisplayName("should start camp successfully")
        void shouldStartCampSuccessfully() {
            BloodCampResponse startedResponse = new BloodCampResponse(
                    campId, "CAMP-ABCD1234", "Community Blood Drive",
                    "Red Cross", "+1234567890", "City Hall",
                    "123 Main Street", null, null, null,
                    LocalDate.now().plusDays(7), LocalDateTime.now(), null,
                    50, null, 0, CampStatusEnum.IN_PROGRESS,
                    null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(campRepository.save(any(BloodCamp.class))).thenReturn(camp);
            when(campMapper.toResponse(camp)).thenReturn(startedResponse);

            BloodCampResponse result = bloodCampService.startCamp(campId);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(CampStatusEnum.IN_PROGRESS);
            verify(campRepository).save(any(BloodCamp.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when camp not found")
        void shouldThrowWhenCampNotFound() {
            when(campRepository.findById(campId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodCampService.startCamp(campId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("completeCamp")
    class CompleteCamp {

        @Test
        @DisplayName("should complete camp successfully")
        void shouldCompleteCampSuccessfully() {
            camp.setTotalCollected(15);

            BloodCampResponse completedResponse = new BloodCampResponse(
                    campId, "CAMP-ABCD1234", "Community Blood Drive",
                    "Red Cross", "+1234567890", "City Hall",
                    "123 Main Street", null, null, null,
                    LocalDate.now().plusDays(7), LocalDateTime.now(), LocalDateTime.now(),
                    50, null, 15, CampStatusEnum.COMPLETED,
                    null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(campRepository.save(any(BloodCamp.class))).thenReturn(camp);
            when(campMapper.toResponse(camp)).thenReturn(completedResponse);

            BloodCampResponse result = bloodCampService.completeCamp(campId);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(CampStatusEnum.COMPLETED);
            assertThat(result.totalCollected()).isEqualTo(15);
        }

        @Test
        @DisplayName("should publish CampCompletedEvent on completion")
        void shouldPublishCampCompletedEvent() {
            camp.setTotalCollected(10);

            BloodCampResponse completedResponse = new BloodCampResponse(
                    campId, "CAMP-ABCD1234", "Community Blood Drive",
                    "Red Cross", "+1234567890", "City Hall",
                    "123 Main Street", null, null, null,
                    LocalDate.now().plusDays(7), LocalDateTime.now(), LocalDateTime.now(),
                    50, null, 10, CampStatusEnum.COMPLETED,
                    null, null, branchId,
                    LocalDateTime.now(), LocalDateTime.now()
            );

            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(campRepository.save(any(BloodCamp.class))).thenReturn(camp);
            when(campMapper.toResponse(camp)).thenReturn(completedResponse);

            bloodCampService.completeCamp(campId);

            ArgumentCaptor<CampCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CampCompletedEvent.class);
            verify(eventPublisher).publishCampCompleted(eventCaptor.capture());

            CampCompletedEvent event = eventCaptor.getValue();
            assertThat(event.campId()).isEqualTo(campId);
            assertThat(event.branchId()).isEqualTo(branchId);
            assertThat(event.totalCollections()).isEqualTo(10);
            assertThat(event.occurredAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("addResource")
    class AddResource {

        private CampResourceCreateRequest resourceRequest;
        private CampResource resource;
        private CampResourceResponse resourceResponse;

        @BeforeEach
        void setUp() {
            resourceRequest = new CampResourceCreateRequest(
                    campId, "EQUIPMENT", "Blood Collection Chair",
                    5, "Portable chairs", branchId
            );

            resource = new CampResource();
            resource.setId(UUID.randomUUID());
            resource.setCampId(campId);
            resource.setResourceType("EQUIPMENT");
            resource.setResourceName("Blood Collection Chair");
            resource.setQuantity(5);
            resource.setBranchId(branchId);

            resourceResponse = new CampResourceResponse(
                    resource.getId(), campId, "EQUIPMENT",
                    "Blood Collection Chair", 5, "Portable chairs",
                    branchId, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("should add resource successfully")
        void shouldAddResourceSuccessfully() {
            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(campMapper.toEntity(resourceRequest)).thenReturn(resource);
            when(resourceRepository.save(any(CampResource.class))).thenReturn(resource);
            when(campMapper.toResponse(resource)).thenReturn(resourceResponse);

            CampResourceResponse result = bloodCampService.addResource(resourceRequest);

            assertThat(result).isNotNull();
            assertThat(result.resourceType()).isEqualTo("EQUIPMENT");
            assertThat(result.resourceName()).isEqualTo("Blood Collection Chair");
            verify(resourceRepository).save(any(CampResource.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when camp not found")
        void shouldThrowWhenCampNotFound() {
            when(campRepository.findById(campId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodCampService.addResource(resourceRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(resourceRepository, never()).save(any(CampResource.class));
        }
    }

    @Nested
    @DisplayName("getResources")
    class GetResources {

        @Test
        @DisplayName("should return list of resources for camp")
        void shouldReturnResourcesList() {
            CampResource resource = new CampResource();
            resource.setId(UUID.randomUUID());
            resource.setCampId(campId);
            resource.setResourceType("EQUIPMENT");
            resource.setResourceName("Blood Collection Chair");

            CampResourceResponse resourceResponse = new CampResourceResponse(
                    resource.getId(), campId, "EQUIPMENT",
                    "Blood Collection Chair", 5, null,
                    branchId, LocalDateTime.now()
            );

            List<CampResource> resources = List.of(resource);
            List<CampResourceResponse> responses = List.of(resourceResponse);

            when(resourceRepository.findByCampId(campId)).thenReturn(resources);
            when(campMapper.toResourceResponseList(resources)).thenReturn(responses);

            List<CampResourceResponse> result = bloodCampService.getResources(campId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).resourceName()).isEqualTo("Blood Collection Chair");
        }
    }

    @Nested
    @DisplayName("registerDonor")
    class RegisterDonor {

        private CampDonorCreateRequest campDonorRequest;
        private CampDonor campDonor;
        private CampDonorResponse campDonorResponse;
        private Donor donor;

        @BeforeEach
        void setUp() {
            campDonorRequest = new CampDonorCreateRequest(campId, donorId, branchId);

            donor = new Donor();
            donor.setId(donorId);
            donor.setFirstName("John");
            donor.setLastName("Doe");

            campDonor = new CampDonor();
            campDonor.setId(UUID.randomUUID());
            campDonor.setCampId(campId);
            campDonor.setDonorId(donorId);
            campDonor.setStatus(CampDonorStatusEnum.REGISTERED);
            campDonor.setBranchId(branchId);

            campDonorResponse = new CampDonorResponse(
                    campDonor.getId(), campId, donorId,
                    LocalDateTime.now(), CampDonorStatusEnum.REGISTERED,
                    branchId, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("should register donor successfully")
        void shouldRegisterDonorSuccessfully() {
            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(campDonorRepository.existsByCampIdAndDonorId(campId, donorId)).thenReturn(false);
            when(campDonorRepository.save(any(CampDonor.class))).thenReturn(campDonor);
            when(campMapper.toResponse(campDonor)).thenReturn(campDonorResponse);

            CampDonorResponse result = bloodCampService.registerDonor(campDonorRequest);

            assertThat(result).isNotNull();
            assertThat(result.campId()).isEqualTo(campId);
            assertThat(result.donorId()).isEqualTo(donorId);
            assertThat(result.status()).isEqualTo(CampDonorStatusEnum.REGISTERED);
            verify(campDonorRepository).save(any(CampDonor.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when donor not found")
        void shouldThrowWhenDonorNotFound() {
            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(donorRepository.findById(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodCampService.registerDonor(campDonorRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(campDonorRepository, never()).save(any(CampDonor.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when camp not found")
        void shouldThrowWhenCampNotFound() {
            when(campRepository.findById(campId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bloodCampService.registerDonor(campDonorRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(campDonorRepository, never()).save(any(CampDonor.class));
        }

        @Test
        @DisplayName("should throw ConflictException when donor already registered")
        void shouldThrowWhenDonorAlreadyRegistered() {
            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(campDonorRepository.existsByCampIdAndDonorId(campId, donorId)).thenReturn(true);

            assertThatThrownBy(() -> bloodCampService.registerDonor(campDonorRequest))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already registered");

            verify(campDonorRepository, never()).save(any(CampDonor.class));
        }
    }

    @Nested
    @DisplayName("getCampDonors")
    class GetCampDonors {

        @Test
        @DisplayName("should return list of camp donors")
        void shouldReturnCampDonorsList() {
            CampDonor campDonor = new CampDonor();
            campDonor.setId(UUID.randomUUID());
            campDonor.setCampId(campId);
            campDonor.setDonorId(donorId);
            campDonor.setStatus(CampDonorStatusEnum.REGISTERED);

            CampDonorResponse donorResponse = new CampDonorResponse(
                    campDonor.getId(), campId, donorId,
                    LocalDateTime.now(), CampDonorStatusEnum.REGISTERED,
                    branchId, LocalDateTime.now()
            );

            List<CampDonor> campDonors = List.of(campDonor);
            List<CampDonorResponse> responses = List.of(donorResponse);

            when(campDonorRepository.findByCampId(campId)).thenReturn(campDonors);
            when(campMapper.toCampDonorResponseList(campDonors)).thenReturn(responses);

            List<CampDonorResponse> result = bloodCampService.getCampDonors(campId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).donorId()).isEqualTo(donorId);
        }
    }

    @Nested
    @DisplayName("linkCollection")
    class LinkCollection {

        private UUID collectionId;

        @BeforeEach
        void setUp() {
            collectionId = UUID.randomUUID();
        }

        @Test
        @DisplayName("should link collection successfully")
        void shouldLinkCollectionSuccessfully() {
            Collection collection = new Collection();
            collection.setId(collectionId);

            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(campCollectionRepository.existsByCampIdAndCollectionId(campId, collectionId)).thenReturn(false);
            when(campRepository.save(any(BloodCamp.class))).thenReturn(camp);

            bloodCampService.linkCollection(campId, collectionId);

            verify(campCollectionRepository).save(any());
            verify(campRepository).save(any(BloodCamp.class));
        }

        @Test
        @DisplayName("should throw ConflictException when collection already linked")
        void shouldThrowWhenCollectionAlreadyLinked() {
            Collection collection = new Collection();
            collection.setId(collectionId);

            when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(campCollectionRepository.existsByCampIdAndCollectionId(campId, collectionId)).thenReturn(true);

            assertThatThrownBy(() -> bloodCampService.linkCollection(campId, collectionId))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already linked");
        }
    }
}
