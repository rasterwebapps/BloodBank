package com.bloodbank.labservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.labservice.dto.TestPanelCreateRequest;
import com.bloodbank.labservice.dto.TestPanelResponse;
import com.bloodbank.labservice.entity.TestPanel;
import com.bloodbank.labservice.mapper.TestPanelMapper;
import com.bloodbank.labservice.repository.TestPanelRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TestPanelService {

    private static final Logger log = LoggerFactory.getLogger(TestPanelService.class);

    private final TestPanelRepository testPanelRepository;
    private final TestPanelMapper testPanelMapper;

    public TestPanelService(TestPanelRepository testPanelRepository,
                            TestPanelMapper testPanelMapper) {
        this.testPanelRepository = testPanelRepository;
        this.testPanelMapper = testPanelMapper;
    }

    @Transactional
    public TestPanelResponse createPanel(TestPanelCreateRequest request) {
        log.info("Creating test panel: code={}, name={}", request.panelCode(), request.panelName());
        TestPanel panel = testPanelMapper.toEntity(request);
        panel.setActive(true);
        panel = testPanelRepository.save(panel);
        return testPanelMapper.toResponse(panel);
    }

    public List<TestPanelResponse> getActivePanels() {
        return testPanelMapper.toResponseList(testPanelRepository.findByIsActiveTrue());
    }

    public List<TestPanelResponse> getMandatoryPanels() {
        return testPanelMapper.toResponseList(testPanelRepository.findByIsMandatoryTrue());
    }

    public TestPanelResponse getPanelById(UUID id) {
        TestPanel panel = testPanelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestPanel", "id", id));
        return testPanelMapper.toResponse(panel);
    }

    public TestPanelResponse getPanelByCode(String code) {
        TestPanel panel = testPanelRepository.findByPanelCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("TestPanel", "panelCode", code));
        return testPanelMapper.toResponse(panel);
    }
}
