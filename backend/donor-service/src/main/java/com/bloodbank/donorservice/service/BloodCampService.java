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
import com.bloodbank.donorservice.entity.CampCollection;
import com.bloodbank.donorservice.entity.CampDonor;
import com.bloodbank.donorservice.entity.CampResource;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BloodCampService {

    private static final Logger log = LoggerFactory.getLogger(BloodCampService.class);

    private final BloodCampRepository campRepository;
    private final CampResourceRepository resourceRepository;
    private final CampDonorRepository campDonorRepository;
    private final CampCollectionRepository campCollectionRepository;
    private final DonorRepository donorRepository;
    private final CollectionRepository collectionRepository;
    private final BloodCampMapper campMapper;
    private final EventPublisher eventPublisher;

    public BloodCampService(BloodCampRepository campRepository,
                            CampResourceRepository resourceRepository,
                            CampDonorRepository campDonorRepository,
                            CampCollectionRepository campCollectionRepository,
                            DonorRepository donorRepository,
                            CollectionRepository collectionRepository,
                            BloodCampMapper campMapper,
                            EventPublisher eventPublisher) {
        this.campRepository = campRepository;
        this.resourceRepository = resourceRepository;
        this.campDonorRepository = campDonorRepository;
        this.campCollectionRepository = campCollectionRepository;
        this.donorRepository = donorRepository;
        this.collectionRepository = collectionRepository;
        this.campMapper = campMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BloodCampResponse createCamp(BloodCampCreateRequest request) {
        log.info("Creating blood camp: {}", request.campName());

        BloodCamp camp = campMapper.toEntity(request);
        camp.setCampCode("CAMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        camp.setStatus(CampStatusEnum.PLANNED);
        camp.setTotalCollected(0);
        camp.setBranchId(request.branchId());

        camp = campRepository.save(camp);
        log.info("Blood camp created with code: {}", camp.getCampCode());
        return campMapper.toResponse(camp);
    }

    @Transactional
    public BloodCampResponse updateCamp(UUID id, BloodCampUpdateRequest request) {
        log.info("Updating blood camp: {}", id);

        BloodCamp camp = campRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodCamp", "id", id));

        if (request.campName() != null) camp.setCampName(request.campName());
        if (request.organizerName() != null) camp.setOrganizerName(request.organizerName());
        if (request.organizerContact() != null) camp.setOrganizerContact(request.organizerContact());
        if (request.venueName() != null) camp.setVenueName(request.venueName());
        if (request.venueAddress() != null) camp.setVenueAddress(request.venueAddress());
        if (request.cityId() != null) camp.setCityId(request.cityId());
        if (request.latitude() != null) camp.setLatitude(request.latitude());
        if (request.longitude() != null) camp.setLongitude(request.longitude());
        if (request.scheduledDate() != null) camp.setScheduledDate(request.scheduledDate());
        if (request.expectedDonors() != null) camp.setExpectedDonors(request.expectedDonors());
        if (request.coordinatorId() != null) camp.setCoordinatorId(request.coordinatorId());
        if (request.notes() != null) camp.setNotes(request.notes());

        camp = campRepository.save(camp);
        return campMapper.toResponse(camp);
    }

    public BloodCampResponse getCampById(UUID id) {
        log.debug("Fetching blood camp by id: {}", id);
        BloodCamp camp = campRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BloodCamp", "id", id));
        return campMapper.toResponse(camp);
    }

    public PagedResponse<BloodCampResponse> getCampsByStatus(CampStatusEnum status, Pageable pageable) {
        log.debug("Fetching blood camps by status: {}", status);
        Page<BloodCamp> page = campRepository.findByStatus(status, pageable);
        List<BloodCampResponse> content = campMapper.toResponseList(page.getContent());
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
    public BloodCampResponse startCamp(UUID campId) {
        log.info("Starting blood camp: {}", campId);

        BloodCamp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodCamp", "id", campId));

        camp.setStatus(CampStatusEnum.IN_PROGRESS);
        camp.setStartTime(Instant.now());

        camp = campRepository.save(camp);
        log.info("Blood camp {} started", camp.getCampCode());
        return campMapper.toResponse(camp);
    }

    @Transactional
    public BloodCampResponse completeCamp(UUID campId) {
        log.info("Completing blood camp: {}", campId);

        BloodCamp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodCamp", "id", campId));

        camp.setStatus(CampStatusEnum.COMPLETED);
        camp.setEndTime(Instant.now());

        camp = campRepository.save(camp);

        eventPublisher.publishCampCompleted(new CampCompletedEvent(
                camp.getId(),
                camp.getBranchId(),
                camp.getTotalCollected(),
                Instant.now()
        ));

        log.info("Blood camp {} completed with {} collections", camp.getCampCode(), camp.getTotalCollected());
        return campMapper.toResponse(camp);
    }

    @Transactional
    public CampResourceResponse addResource(CampResourceCreateRequest request) {
        log.info("Adding resource to camp: {}", request.campId());

        campRepository.findById(request.campId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodCamp", "id", request.campId()));

        CampResource resource = campMapper.toEntity(request);
        resource.setBranchId(request.branchId());

        resource = resourceRepository.save(resource);
        return campMapper.toResponse(resource);
    }

    public List<CampResourceResponse> getResources(UUID campId) {
        log.debug("Fetching resources for camp: {}", campId);
        List<CampResource> resources = resourceRepository.findByCampId(campId);
        return campMapper.toResourceResponseList(resources);
    }

    @Transactional
    public CampDonorResponse registerDonor(CampDonorCreateRequest request) {
        log.info("Registering donor {} for camp {}", request.donorId(), request.campId());

        campRepository.findById(request.campId())
                .orElseThrow(() -> new ResourceNotFoundException("BloodCamp", "id", request.campId()));
        donorRepository.findById(request.donorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor", "id", request.donorId()));

        if (campDonorRepository.existsByCampIdAndDonorId(request.campId(), request.donorId())) {
            throw new ConflictException("Donor is already registered for this camp");
        }

        CampDonor campDonor = new CampDonor();
        campDonor.setCampId(request.campId());
        campDonor.setDonorId(request.donorId());
        campDonor.setRegistrationTime(Instant.now());
        campDonor.setStatus(CampDonorStatusEnum.REGISTERED);
        campDonor.setBranchId(request.branchId());

        campDonor = campDonorRepository.save(campDonor);
        return campMapper.toResponse(campDonor);
    }

    public List<CampDonorResponse> getCampDonors(UUID campId) {
        log.debug("Fetching donors for camp: {}", campId);
        List<CampDonor> campDonors = campDonorRepository.findByCampId(campId);
        return campMapper.toCampDonorResponseList(campDonors);
    }

    @Transactional
    public void linkCollection(UUID campId, UUID collectionId) {
        log.info("Linking collection {} to camp {}", collectionId, campId);

        BloodCamp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("BloodCamp", "id", campId));
        collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", collectionId));

        if (campCollectionRepository.existsByCampIdAndCollectionId(campId, collectionId)) {
            throw new ConflictException("Collection is already linked to this camp");
        }

        CampCollection campCollection = new CampCollection();
        campCollection.setCampId(campId);
        campCollection.setCollectionId(collectionId);
        campCollection.setBranchId(camp.getBranchId());

        campCollectionRepository.save(campCollection);

        camp.setTotalCollected(camp.getTotalCollected() + 1);
        campRepository.save(camp);

        log.info("Collection {} linked to camp {}, total collected: {}", collectionId, campId, camp.getTotalCollected());
    }
}
