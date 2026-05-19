package com.immo.payment.repository;

import com.immo.payment.entity.Payment;
import com.immo.payment.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    long countByStatut(PaymentStatus statut);
    List<Payment> findByTenantIdOrderByDateEcheanceDesc(String tenantId);

    @Query("select coalesce(sum(p.montant), 0) from Payment p where p.statut = com.immo.payment.entity.enums.PaymentStatus.PAYE")
    BigDecimal totalPaidAmount();
}
