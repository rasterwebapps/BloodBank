package com.bloodbank.labservice.repository;

import com.bloodbank.labservice.entity.TestPanel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestPanelRepository extends JpaRepository<TestPanel, UUID> {

    Optional<TestPanel> findByPanelCode(String panelCode);

    List<TestPanel> findByIsActiveTrue();

    List<TestPanel> findByIsMandatoryTrue();
}
