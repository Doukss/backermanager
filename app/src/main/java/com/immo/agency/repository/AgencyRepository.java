package com.immo.agency.repository;

import com.immo.agency.entity.Agency;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyRepository extends JpaRepository<Agency, UUID> {
    Optional<Agency> findByTenantId(String tenantId);
    long countByActive(boolean active);
}
