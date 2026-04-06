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

@Repository
public interface TestOrderRepository extends JpaRepository<TestOrder, UUID>,
                                             JpaSpecificationExecutor<TestOrder> {

    Optional<TestOrder> findByOrderNumber(String orderNumber);

    List<TestOrder> findByDonorId(UUID donorId);

    List<TestOrder> findByStatus(OrderStatusEnum status);

    Page<TestOrder> findByBranchId(UUID branchId, Pageable pageable);

    List<TestOrder> findByCollectionId(UUID collectionId);
}
