package com.bloodbank.labservice.repository;

import com.bloodbank.labservice.entity.TestPanel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface TestPanelRepository extends JpaRepository<TestPanel, UUID> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<TestPanel> findByPanelCode(String panelCode);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TestPanel> findByIsActiveTrue();

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TestPanel> findByIsMandatoryTrue();
}
