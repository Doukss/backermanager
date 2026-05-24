package com.immo.dispute.repository;

import com.immo.dispute.entity.Dispute;
import com.immo.dispute.entity.enums.DisputeStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    long countByStatut(DisputeStatus statut);
    List<Dispute> findByTenantIdOrderByIdDesc(String tenantId);
    Optional<Dispute> findByTenantIdAndId(String tenantId, UUID id);
}
