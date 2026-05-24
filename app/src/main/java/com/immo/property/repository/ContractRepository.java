package com.immo.property.repository;

import com.immo.property.entity.Contract;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findByTenantIdOrderByDateDebutDesc(String tenantId);
    Optional<Contract> findByTenantIdAndId(String tenantId, UUID id);
}
