package com.bloodbank.labservice.repository;

import com.bloodbank.labservice.entity.TestOrder;
import com.bloodbank.labservice.enums.OrderStatusEnum;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.QueryHint;

@Repository
public interface TestOrderRepository extends JpaRepository<TestOrder, UUID>,
                                             JpaSpecificationExecutor<TestOrder> {

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Optional<TestOrder> findByOrderNumber(String orderNumber);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TestOrder> findByDonorId(UUID donorId);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TestOrder> findByStatus(OrderStatusEnum status);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    Page<TestOrder> findByBranchId(UUID branchId, Pageable pageable);

    @QueryHints({@QueryHint(name = "org.hibernate.readOnly", value = "true")})
    List<TestOrder> findByCollectionId(UUID collectionId);
}
