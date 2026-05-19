package com.immo.agency.repository;

import com.immo.agency.entity.Subscription;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByTenantId(String tenantId);
    List<Subscription> findByStatus(String status);

    @Query("select coalesce(sum(s.monthlyPrice), 0) from Subscription s where s.status in ('ACTIVE', 'TRIAL')")
    BigDecimal currentMonthlyRevenue();
}
