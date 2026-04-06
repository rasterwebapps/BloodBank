package com.bloodbank.donorservice.service;

import com.bloodbank.common.dto.PagedResponse;
import com.bloodbank.common.events.DonationCompletedEvent;
import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.common.model.enums.CollectionStatusEnum;
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
import com.bloodbank.donorservice.enums.SampleStatusEnum;
import com.bloodbank.donorservice.event.EventPublisher;
import com.bloodbank.donorservice.mapper.CollectionMapper;
import com.bloodbank.donorservice.repository.CollectionAdverseReactionRepository;
import com.bloodbank.donorservice.repository.CollectionRepository;
import com.bloodbank.donorservice.repository.CollectionSampleRepository;
import com.bloodbank.donorservice.repository.DonorRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CollectionService {

    private static final Logger log = LoggerFactory.getLogger(CollectionService.class);

    private final CollectionRepository collectionRepository;
    private final CollectionAdverseReactionRepository adverseReactionRepository;
    private final CollectionSampleRepository sampleRepository;
    private final DonorRepository donorRepository;
    private final CollectionMapper collectionMapper;
    private final EventPublisher eventPublisher;

    public CollectionService(CollectionRepository collectionRepository,
                             CollectionAdverseReactionRepository adverseReactionRepository,
                             CollectionSampleRepository sampleRepository,
                             DonorRepository donorRepository,
                             CollectionMapper collectionMapper,
                             EventPublisher eventPublisher) {
        this.collectionRepository = collectionRepository;
        this.adverseReactionRepository = adverseReactionRepository;
        this.sampleRepository = sampleRepository;
        this.donorRepository = donorRepository;
        this.collectionMapper = collectionMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CollectionResponse startCollection(CollectionCreateRequest request) {
        log.info("Starting collection for donor: {}", request.donorId());

        donorRepository.findById(request.donorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", request.donorId()));

        Collection collection = collectionMapper.toEntity(request);
        collection.setCollectionNumber("COL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        collection.setCollectionDate(Instant.now());
        collection.setStartTime(Instant.now());
        collection.setStatus(CollectionStatusEnum.IN_PROGRESS);
        collection.setBranchId(request.branchId());

        collection = collectionRepository.save(collection);
        log.info("Collection started with number: {}", collection.getCollectionNumber());
        return collectionMapper.toResponse(collection);
    }

    @Transactional
    public CollectionResponse completeCollection(UUID collectionId, CollectionCompleteRequest request) {
        log.info("Completing collection: {}", collectionId);

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", collectionId));

        UUID donorId = collection.getDonorId();

        collection.setVolumeMl(request.volumeMl());
        collection.setEndTime(request.endTime() != null
                ? request.endTime().atZone(java.time.ZoneId.systemDefault()).toInstant()
                : Instant.now());
        collection.setStatus(CollectionStatusEnum.COMPLETED);

        if (request.notes() != null) {
            collection.setNotes(request.notes());
        }

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", donorId));
        donor.setTotalDonations(donor.getTotalDonations() + 1);
        donor.setLastDonationDate(LocalDate.now());
        donorRepository.save(donor);

        collection = collectionRepository.save(collection);

        eventPublisher.publishDonationCompleted(new DonationCompletedEvent(
                collection.getId(),
                collection.getDonorId(),
                collection.getBranchId(),
                Instant.now()
        ));

        log.info("Collection {} completed, donor total donations: {}", collection.getCollectionNumber(), donor.getTotalDonations());
        return collectionMapper.toResponse(collection);
    }

    public CollectionResponse getCollectionById(UUID id) {
        log.debug("Fetching collection by id: {}", id);
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", id));
        return collectionMapper.toResponse(collection);
    }

    public List<CollectionResponse> getCollectionsByDonor(UUID donorId) {
        log.debug("Fetching collections for donor: {}", donorId);
        List<Collection> collections = collectionRepository.findByDonorIdOrderByCollectionDateDesc(donorId);
        return collectionMapper.toResponseList(collections);
    }

    public PagedResponse<CollectionResponse> getCollectionsByStatus(CollectionStatusEnum status, Pageable pageable) {
        log.debug("Fetching collections by status: {}", status);
        Page<Collection> page = collectionRepository.findByStatus(status, pageable);
        List<CollectionResponse> content = collectionMapper.toResponseList(page.getContent());
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Transactional
    public AdverseReactionResponse recordAdverseReaction(AdverseReactionCreateRequest request) {
        log.info("Recording adverse reaction for collection: {}", request.collectionId());

        collectionRepository.findById(request.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", request.collectionId()));

        CollectionAdverseReaction reaction = collectionMapper.toEntity(request);
        reaction.setBranchId(request.branchId());

        reaction = adverseReactionRepository.save(reaction);
        return collectionMapper.toResponse(reaction);
    }

    public List<AdverseReactionResponse> getAdverseReactions(UUID collectionId) {
        log.debug("Fetching adverse reactions for collection: {}", collectionId);
        List<CollectionAdverseReaction> reactions = adverseReactionRepository.findByCollectionId(collectionId);
        return collectionMapper.toReactionResponseList(reactions);
    }

    @Transactional
    public CollectionSampleResponse addSample(CollectionSampleCreateRequest request) {
        log.info("Adding sample for collection: {}", request.collectionId());

        collectionRepository.findById(request.collectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", request.collectionId()));

        CollectionSample sample = collectionMapper.toEntity(request);
        sample.setSampleNumber("SMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        sample.setCollectedAt(Instant.now());
        sample.setStatus(SampleStatusEnum.COLLECTED);
        sample.setBranchId(request.branchId());

        sample = sampleRepository.save(sample);
        log.info("Sample added with number: {}", sample.getSampleNumber());
        return collectionMapper.toResponse(sample);
    }

    public List<CollectionSampleResponse> getSamples(UUID collectionId) {
        log.debug("Fetching samples for collection: {}", collectionId);
        List<CollectionSample> samples = sampleRepository.findByCollectionId(collectionId);
        return collectionMapper.toSampleResponseList(samples);
    }
}
