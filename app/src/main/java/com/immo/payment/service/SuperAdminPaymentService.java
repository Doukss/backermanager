package com.immo.payment.service;

import com.immo.payment.dto.PaymentStatsResponse;
import com.immo.payment.dto.SuperAdminPaymentResponse;
import com.immo.payment.entity.Payment;
import com.immo.payment.entity.enums.PaymentStatus;
import com.immo.payment.repository.PaymentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminPaymentService {
    private final PaymentRepository paymentRepository;

    public List<SuperAdminPaymentResponse> listPayments() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    public PaymentStatsResponse stats() {
        return PaymentStatsResponse.builder()
                .totalPayments(paymentRepository.count())
                .paidPayments(paymentRepository.countByStatut(PaymentStatus.PAYE))
                .pendingPayments(paymentRepository.countByStatut(PaymentStatus.EN_ATTENTE))
                .totalPaidAmount(paymentRepository.totalPaidAmount())
                .build();
    }

    private SuperAdminPaymentResponse toResponse(Payment payment) {
        return SuperAdminPaymentResponse.builder()
                .id(payment.getId())
                .tenantId(payment.getTenantId())
                .contractId(payment.getContractId())
                .montant(payment.getMontant())
                .dateEcheance(payment.getDateEcheance())
                .datePaiement(payment.getDatePaiement())
                .statut(payment.getStatut())
                .reference(payment.getReference())
                .build();
    }
}
