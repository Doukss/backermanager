package com.immo.dispute.service;

import com.immo.dispute.dto.DisputeStatsResponse;
import com.immo.dispute.dto.SuperAdminDisputeResponse;
import com.immo.dispute.entity.Dispute;
import com.immo.dispute.entity.enums.DisputeStatus;
import com.immo.dispute.repository.DisputeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminDisputeService {
    private final DisputeRepository disputeRepository;

    public List<SuperAdminDisputeResponse> listDisputes() {
        return disputeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DisputeStatsResponse stats() {
        return DisputeStatsResponse.builder()
                .totalDisputes(disputeRepository.count())
                .openDisputes(disputeRepository.countByStatut(DisputeStatus.OUVERT))
                .inProgressDisputes(disputeRepository.countByStatut(DisputeStatus.EN_COURS))
                .resolvedDisputes(disputeRepository.countByStatut(DisputeStatus.RESOLU))
                .build();
    }

    private SuperAdminDisputeResponse toResponse(Dispute dispute) {
        return SuperAdminDisputeResponse.builder()
                .id(dispute.getId())
                .tenantId(dispute.getTenantId())
                .contractId(dispute.getContractId())
                .titre(dispute.getTitre())
                .description(dispute.getDescription())
                .statut(dispute.getStatut())
                .priorite(dispute.getPriorite())
                .resolution(dispute.getResolution())
                .build();
    }
}
