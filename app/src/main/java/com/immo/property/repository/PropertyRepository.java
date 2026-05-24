package com.immo.property.repository;

import com.immo.property.entity.Property;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    List<Property> findByTenantIdOrderByTitreAsc(String tenantId);
    Optional<Property> findByTenantIdAndId(String tenantId, UUID id);
}
