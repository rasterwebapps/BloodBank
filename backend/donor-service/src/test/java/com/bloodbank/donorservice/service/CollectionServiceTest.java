package com.bloodbank.donorservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.CollectionStatusEnum;
import com.bloodbank.common.model.enums.SeverityEnum;
import com.bloodbank.donorservice.dto.AdverseReactionCreateRequest;
import com.bloodbank.donorservice.dto.AdverseReactionResponse;
import com.bloodbank.donorservice.dto.CollectionCompleteRequest;
import com.bloodbank.donorservice.dto.CollectionCreateRequest;
import com.bloodbank.donorservice.dto.CollectionResponse;
import com.bloodbank.donorservice.dto.CollectionSampleCreateRequest;
import com.bloodbank.donorservice.dto.CollectionSampleResponse;
import com.bloodbank.donorservice.entity.Collection;
import com.bloodbank.donorservice.entity.CollectionAdverseReaction;
import com.bloodbank.donorservice.entity.CollectionSample;
import com.bloodbank.donorservice.entity.Donor;
import com.bloodbank.donorservice.enums.CollectionTypeEnum;
import com.bloodbank.donorservice.enums.DonationTypeEnum;
import com.bloodbank.donorservice.enums.ReactionOutcomeEnum;
import com.bloodbank.donorservice.enums.SampleStatusEnum;
import com.bloodbank.donorservice.enums.SampleTypeEnum;
import com.bloodbank.donorservice.event.EventPublisher;
import com.bloodbank.donorservice.mapper.CollectionMapper;
import com.bloodbank.donorservice.repository.CollectionAdverseReactionRepository;
import com.bloodbank.donorservice.repository.CollectionRepository;
import com.bloodbank.donorservice.repository.CollectionSampleRepository;
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

import java.time.Instant;
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
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CollectionAdverseReactionRepository adverseReactionRepository;

    @Mock
    private CollectionSampleRepository sampleRepository;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CollectionService collectionService;

    private UUID collectionId;
    private UUID donorId;
    private UUID branchId;
    private Collection collection;
    private Donor donor;
    private CollectionResponse collectionResponse;

    @BeforeEach
    void setUp() {
        collectionId = UUID.randomUUID();
        donorId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        collection = new Collection();
        collection.setId(collectionId);
        collection.setDonorId(donorId);
        collection.setCollectionNumber("COL-ABCD1234");
        collection.setCollectionDate(Instant.now());
        collection.setCollectionType(CollectionTypeEnum.WHOLE_BLOOD);
        collection.setDonationType(DonationTypeEnum.VOLUNTARY);
        collection.setStatus(CollectionStatusEnum.IN_PROGRESS);
        collection.setStartTime(Instant.now());
        collection.setBranchId(branchId);

        donor = new Donor();
        donor.setId(donorId);
        donor.setFirstName("John");
        donor.setLastName("Doe");
        donor.setTotalDonations(2);
        donor.setLastDonationDate(LocalDate.now().minusMonths(3));
        donor.setBranchId(branchId);

        collectionResponse = new CollectionResponse(
                collectionId, donorId, null, "COL-ABCD1234",
                LocalDateTime.now(), CollectionTypeEnum.WHOLE_BLOOD,
                DonationTypeEnum.VOLUNTARY, null, "TRIPLE", null,
                null, LocalDateTime.now(), null,
                CollectionStatusEnum.IN_PROGRESS, null, null,
                branchId, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("startCollection")
    class StartCollection {

        private CollectionCreateRequest createRequest;

        @BeforeEach
        void setUp() {
            createRequest = new CollectionCreateRequest(
                    donorId, null, CollectionTypeEnum.WHOLE_BLOOD,
                    DonationTypeEnum.VOLUNTARY, "TRIPLE", "LOT-001",
                    "PHLEB-001", "Initial notes", branchId
            );
        }

        @Test
        @DisplayName("should start collection successfully")
        void shouldStartCollectionSuccessfully() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(collectionMapper.toEntity(createRequest)).thenReturn(collection);
            when(collectionRepository.save(any(Collection.class))).thenReturn(collection);
            when(collectionMapper.toResponse(collection)).thenReturn(collectionResponse);

            CollectionResponse result = collectionService.startCollection(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.collectionNumber()).isEqualTo("COL-ABCD1234");
            assertThat(result.status()).isEqualTo(CollectionStatusEnum.IN_PROGRESS);
            verify(collectionRepository).save(any(Collection.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when donor not found")
        void shouldThrowWhenDonorNotFound() {
            when(donorRepository.findById(donorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> collectionService.startCollection(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(collectionRepository, never()).save(any(Collection.class));
        }
    }

    @Nested
    @DisplayName("completeCollection")
    class CompleteCollection {

        private CollectionCompleteRequest completeRequest;

        @BeforeEach
        void setUp() {
            completeRequest = new CollectionCompleteRequest(450, LocalDateTime.now(), "Completed without issues");
        }

        @Test
        @DisplayName("should complete collection successfully")
        void shouldCompleteCollectionSuccessfully() {
            Collection completedCollection = new Collection();
            completedCollection.setId(collectionId);
            completedCollection.setDonorId(donorId);
            completedCollection.setBranchId(branchId);
            completedCollection.setCollectionNumber("COL-ABCD1234");
            completedCollection.setStatus(CollectionStatusEnum.COMPLETED);
            completedCollection.setVolumeMl(450);

            CollectionResponse completedResponse = new CollectionResponse(
                    collectionId, donorId, null, "COL-ABCD1234",
                    LocalDateTime.now(), CollectionTypeEnum.WHOLE_BLOOD,
                    DonationTypeEnum.VOLUNTARY, 450, "TRIPLE", null,
                    null, LocalDateTime.now(), LocalDateTime.now(),
                    CollectionStatusEnum.COMPLETED, "Completed without issues", null,
                    branchId, LocalDateTime.now(), LocalDateTime.now()
            );

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(collectionRepository.save(any(Collection.class))).thenReturn(completedCollection);
            when(donorRepository.save(any(Donor.class))).thenReturn(donor);
            when(collectionMapper.toResponse(completedCollection)).thenReturn(completedResponse);

            CollectionResponse result = collectionService.completeCollection(collectionId, completeRequest);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(CollectionStatusEnum.COMPLETED);
            assertThat(result.volumeMl()).isEqualTo(450);
            verify(donorRepository).save(any(Donor.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when collection not found")
        void shouldThrowWhenCollectionNotFound() {
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> collectionService.completeCollection(collectionId, completeRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(eventPublisher, never()).publishDonationCompleted(any(DonationCompletedEvent.class));
        }

        @Test
        @DisplayName("should publish DonationCompletedEvent on completion")
        void shouldPublishDonationCompletedEvent() {
            Collection completedCollection = new Collection();
            completedCollection.setId(collectionId);
            completedCollection.setDonorId(donorId);
            completedCollection.setBranchId(branchId);
            completedCollection.setCollectionNumber("COL-ABCD1234");
            completedCollection.setStatus(CollectionStatusEnum.COMPLETED);

            CollectionResponse completedResponse = new CollectionResponse(
                    collectionId, donorId, null, "COL-ABCD1234",
                    LocalDateTime.now(), CollectionTypeEnum.WHOLE_BLOOD,
                    DonationTypeEnum.VOLUNTARY, 450, null, null,
                    null, LocalDateTime.now(), LocalDateTime.now(),
                    CollectionStatusEnum.COMPLETED, null, null,
                    branchId, LocalDateTime.now(), LocalDateTime.now()
            );

            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(donorRepository.findById(donorId)).thenReturn(Optional.of(donor));
            when(collectionRepository.save(any(Collection.class))).thenReturn(completedCollection);
            when(donorRepository.save(any(Donor.class))).thenReturn(donor);
            when(collectionMapper.toResponse(completedCollection)).thenReturn(completedResponse);

            collectionService.completeCollection(collectionId, completeRequest);

            ArgumentCaptor<DonationCompletedEvent> eventCaptor = ArgumentCaptor.forClass(DonationCompletedEvent.class);
            verify(eventPublisher).publishDonationCompleted(eventCaptor.capture());

            DonationCompletedEvent event = eventCaptor.getValue();
            assertThat(event.donationId()).isEqualTo(collectionId);
            assertThat(event.donorId()).isEqualTo(donorId);
            assertThat(event.branchId()).isEqualTo(branchId);
            assertThat(event.occurredAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getCollectionById")
    class GetCollectionById {

        @Test
        @DisplayName("should return collection when found")
        void shouldReturnCollectionWhenFound() {
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(collectionMapper.toResponse(collection)).thenReturn(collectionResponse);

            CollectionResponse result = collectionService.getCollectionById(collectionId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(collectionId);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> collectionService.getCollectionById(collectionId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCollectionsByDonor")
    class GetCollectionsByDonor {

        @Test
        @DisplayName("should return list of collections for donor")
        void shouldReturnCollectionsList() {
            List<Collection> collections = List.of(collection);
            List<CollectionResponse> responses = List.of(collectionResponse);

            when(collectionRepository.findByDonorIdOrderByCollectionDateDesc(donorId)).thenReturn(collections);
            when(collectionMapper.toResponseList(collections)).thenReturn(responses);

            List<CollectionResponse> result = collectionService.getCollectionsByDonor(donorId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).donorId()).isEqualTo(donorId);
        }
    }

    @Nested
    @DisplayName("getCollectionsByStatus")
    class GetCollectionsByStatus {

        @Test
        @DisplayName("should return paginated collections by status")
        void shouldReturnPaginatedResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Collection> page = new PageImpl<>(List.of(collection), pageable, 1);
            List<CollectionResponse> responses = List.of(collectionResponse);

            when(collectionRepository.findByStatus(CollectionStatusEnum.IN_PROGRESS, pageable)).thenReturn(page);
            when(collectionMapper.toResponseList(page.getContent())).thenReturn(responses);

            PagedResponse<CollectionResponse> result = collectionService.getCollectionsByStatus(
                    CollectionStatusEnum.IN_PROGRESS, pageable);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isZero();
        }
    }

    @Nested
    @DisplayName("recordAdverseReaction")
    class RecordAdverseReaction {

        private AdverseReactionCreateRequest reactionRequest;
        private CollectionAdverseReaction reaction;
        private AdverseReactionResponse reactionResponse;

        @BeforeEach
        void setUp() {
            reactionRequest = new AdverseReactionCreateRequest(
                    collectionId, "VASOVAGAL", SeverityEnum.MILD,
                    LocalDateTime.now(), "Faintness during collection",
                    "Rest and fluids", ReactionOutcomeEnum.RESOLVED,
                    "NURSE-001", branchId
            );

            reaction = new CollectionAdverseReaction();
            reaction.setId(UUID.randomUUID());
            reaction.setCollectionId(collectionId);
            reaction.setReactionType("VASOVAGAL");
            reaction.setSeverity(SeverityEnum.MILD);
            reaction.setBranchId(branchId);

            reactionResponse = new AdverseReactionResponse(
                    reaction.getId(), collectionId, "VASOVAGAL",
                    SeverityEnum.MILD, LocalDateTime.now(),
                    "Faintness during collection", "Rest and fluids",
                    ReactionOutcomeEnum.RESOLVED, "NURSE-001",
                    branchId, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("should record adverse reaction successfully")
        void shouldRecordAdverseReactionSuccessfully() {
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(collectionMapper.toEntity(reactionRequest)).thenReturn(reaction);
            when(adverseReactionRepository.save(any(CollectionAdverseReaction.class))).thenReturn(reaction);
            when(collectionMapper.toResponse(reaction)).thenReturn(reactionResponse);

            AdverseReactionResponse result = collectionService.recordAdverseReaction(reactionRequest);

            assertThat(result).isNotNull();
            assertThat(result.reactionType()).isEqualTo("VASOVAGAL");
            assertThat(result.severity()).isEqualTo(SeverityEnum.MILD);
            verify(adverseReactionRepository).save(any(CollectionAdverseReaction.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when collection not found")
        void shouldThrowWhenCollectionNotFound() {
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> collectionService.recordAdverseReaction(reactionRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(adverseReactionRepository, never()).save(any(CollectionAdverseReaction.class));
        }
    }

    @Nested
    @DisplayName("getAdverseReactions")
    class GetAdverseReactions {

        @Test
        @DisplayName("should return list of adverse reactions for collection")
        void shouldReturnReactionsList() {
            CollectionAdverseReaction reaction = new CollectionAdverseReaction();
            reaction.setId(UUID.randomUUID());
            reaction.setCollectionId(collectionId);
            reaction.setReactionType("VASOVAGAL");

            AdverseReactionResponse reactionResponse = new AdverseReactionResponse(
                    reaction.getId(), collectionId, "VASOVAGAL",
                    SeverityEnum.MILD, LocalDateTime.now(), null, null,
                    ReactionOutcomeEnum.RESOLVED, null, branchId, LocalDateTime.now()
            );

            List<CollectionAdverseReaction> reactions = List.of(reaction);
            List<AdverseReactionResponse> responses = List.of(reactionResponse);

            when(adverseReactionRepository.findByCollectionId(collectionId)).thenReturn(reactions);
            when(collectionMapper.toReactionResponseList(reactions)).thenReturn(responses);

            List<AdverseReactionResponse> result = collectionService.getAdverseReactions(collectionId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).reactionType()).isEqualTo("VASOVAGAL");
        }
    }

    @Nested
    @DisplayName("addSample")
    class AddSample {

        private CollectionSampleCreateRequest sampleRequest;
        private CollectionSample sample;
        private CollectionSampleResponse sampleResponse;

        @BeforeEach
        void setUp() {
            sampleRequest = new CollectionSampleCreateRequest(
                    collectionId, SampleTypeEnum.EDTA, "Sample notes", branchId
            );

            sample = new CollectionSample();
            sample.setId(UUID.randomUUID());
            sample.setCollectionId(collectionId);
            sample.setSampleNumber("SMP-ABCD1234");
            sample.setSampleType(SampleTypeEnum.EDTA);
            sample.setCollectedAt(Instant.now());
            sample.setStatus(SampleStatusEnum.COLLECTED);
            sample.setBranchId(branchId);

            sampleResponse = new CollectionSampleResponse(
                    sample.getId(), collectionId, "SMP-ABCD1234",
                    SampleTypeEnum.EDTA, LocalDateTime.now(),
                    SampleStatusEnum.COLLECTED, "Sample notes",
                    branchId, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("should add sample successfully")
        void shouldAddSampleSuccessfully() {
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
            when(collectionMapper.toEntity(sampleRequest)).thenReturn(sample);
            when(sampleRepository.save(any(CollectionSample.class))).thenReturn(sample);
            when(collectionMapper.toResponse(sample)).thenReturn(sampleResponse);

            CollectionSampleResponse result = collectionService.addSample(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.sampleNumber()).isEqualTo("SMP-ABCD1234");
            assertThat(result.status()).isEqualTo(SampleStatusEnum.COLLECTED);
            verify(sampleRepository).save(any(CollectionSample.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when collection not found")
        void shouldThrowWhenCollectionNotFound() {
            when(collectionRepository.findById(collectionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> collectionService.addSample(sampleRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(sampleRepository, never()).save(any(CollectionSample.class));
        }
    }

    @Nested
    @DisplayName("getSamples")
    class GetSamples {

        @Test
        @DisplayName("should return list of samples for collection")
        void shouldReturnSamplesList() {
            CollectionSample sample = new CollectionSample();
            sample.setId(UUID.randomUUID());
            sample.setCollectionId(collectionId);
            sample.setSampleNumber("SMP-ABCD1234");
            sample.setSampleType(SampleTypeEnum.EDTA);

            CollectionSampleResponse sampleResponse = new CollectionSampleResponse(
                    sample.getId(), collectionId, "SMP-ABCD1234",
                    SampleTypeEnum.EDTA, LocalDateTime.now(),
                    SampleStatusEnum.COLLECTED, null, branchId, LocalDateTime.now()
            );

            List<CollectionSample> samples = List.of(sample);
            List<CollectionSampleResponse> responses = List.of(sampleResponse);

            when(sampleRepository.findByCollectionId(collectionId)).thenReturn(samples);
            when(collectionMapper.toSampleResponseList(samples)).thenReturn(responses);

            List<CollectionSampleResponse> result = collectionService.getSamples(collectionId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).sampleNumber()).isEqualTo("SMP-ABCD1234");
        }
    }
}
