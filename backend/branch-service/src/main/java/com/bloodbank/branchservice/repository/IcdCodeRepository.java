package com.bloodbank.branchservice.repository;

import com.bloodbank.branchservice.entity.IcdCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface IcdCodeRepository extends JpaRepository<IcdCode, UUID> {
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<IcdCode> findByIcdCode(String icdCode);
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<IcdCode> findByIsActiveTrue();
    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<IcdCode> findByCategory(String category);
    boolean existsByIcdCode(String icdCode);
}
