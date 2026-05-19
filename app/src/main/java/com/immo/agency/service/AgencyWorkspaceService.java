package com.immo.agency.service;

import com.immo.agency.dto.AgencyDashboardData;
import com.immo.agency.entity.Agency;
import com.immo.agency.repository.AgencyRepository;
import com.immo.dispute.entity.Dispute;
import com.immo.dispute.entity.enums.DisputeStatus;
import com.immo.dispute.repository.DisputeRepository;
import com.immo.payment.entity.Payment;
import com.immo.payment.entity.enums.PaymentStatus;
import com.immo.payment.repository.PaymentRepository;
import com.immo.property.entity.Contract;
import com.immo.property.entity.Property;
import com.immo.property.entity.enums.ContractStatus;
import com.immo.property.repository.ContractRepository;
import com.immo.property.repository.PropertyRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgencyWorkspaceService {
    private final AgencyRepository agencyRepository;
    private final PropertyRepository propertyRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final DisputeRepository disputeRepository;

    public AgencyDashboardData dashboard(String tenantId) {
        Agency agency = agencyRepository.findByTenantId(tenantId).orElse(null);
        List<Property> properties = propertyRepository.findByTenantIdOrderByTitreAsc(tenantId);
        List<Contract> contracts = contractRepository.findByTenantIdOrderByDateDebutDesc(tenantId);
        List<Payment> payments = paymentRepository.findByTenantIdOrderByDateEcheanceDesc(tenantId);
        List<Dispute> disputes = disputeRepository.findByTenantIdOrderByIdDesc(tenantId);
        Map<UUID, Property> propertiesById = properties.stream()
                .collect(Collectors.toMap(Property::getId, Function.identity()));
        Map<UUID, Contract> contractsById = contracts.stream()
                .collect(Collectors.toMap(Contract::getId, Function.identity()));

        return new AgencyDashboardData(
                new AgencyDashboardData.AgencyInfo(
                        agency == null ? tenantId : agency.getNom(),
                        agency == null ? "" : agency.getEmail(),
                        agency == null ? "STARTER" : agency.getPlan()),
                properties.stream().map(this::toProperty).toList(),
                contracts.stream().map(contract -> toTenant(contract, propertiesById)).toList(),
                payments.stream().map(payment -> toPayment(payment, contractsById, propertiesById)).toList(),
                disputes.stream().map(dispute -> toDispute(dispute, contractsById)).toList(),
                contracts.stream().map(contract -> toContract(contract, propertiesById)).toList(),
                payments.stream()
                        .filter(payment -> payment.getStatut() == PaymentStatus.PAYE)
                        .map(payment -> toReceipt(payment, contractsById, propertiesById))
                        .toList(),
                activities(properties, contracts, payments, disputes),
                monthlyRevenue(payments));
    }

    private AgencyDashboardData.AgencyPropertyItem toProperty(Property property) {
        return new AgencyDashboardData.AgencyPropertyItem(
                property.getId(),
                property.getTitre(),
                joinAddress(property.getAdresse(), property.getVille()),
                property.isDisponible() ? "available" : "occupied",
                property.getLoyerMensuel());
    }

    private AgencyDashboardData.AgencyTenantItem toTenant(Contract contract, Map<UUID, Property> propertiesById) {
        return new AgencyDashboardData.AgencyTenantItem(
                contract.getId(),
                contract.getLocataireNom(),
                contract.getLocataireEmail(),
                "",
                propertyName(contract.getPropertyId(), propertiesById),
                contract.getStatut() == ContractStatus.ACTIF,
                contract.getDateDebut(),
                contract.getStatut() == ContractStatus.ACTIF ? "active" : "ended",
                true);
    }

    private AgencyDashboardData.AgencyPaymentItem toPayment(
            Payment payment,
            Map<UUID, Contract> contractsById,
            Map<UUID, Property> propertiesById) {
        Contract contract = contractsById.get(payment.getContractId());
        return new AgencyDashboardData.AgencyPaymentItem(
                payment.getId(),
                contract == null ? "-" : contract.getLocataireNom(),
                contract == null ? "-" : propertyName(contract.getPropertyId(), propertiesById),
                payment.getMontant(),
                payment.getDatePaiement() == null ? payment.getDateEcheance() : payment.getDatePaiement(),
                switch (payment.getStatut()) {
                    case PAYE -> "paid";
                    case EN_RETARD -> "late";
                    default -> "pending";
                });
    }

    private AgencyDashboardData.AgencyDisputeItem toDispute(Dispute dispute, Map<UUID, Contract> contractsById) {
        Contract contract = contractsById.get(dispute.getContractId());
        return new AgencyDashboardData.AgencyDisputeItem(
                dispute.getId(),
                contract == null ? "-" : contract.getLocataireNom(),
                dispute.getTitre(),
                LocalDate.now(),
                dispute.getStatut() == DisputeStatus.RESOLU || dispute.getStatut() == DisputeStatus.FERME
                        ? "resolved"
                        : "ongoing");
    }

    private AgencyDashboardData.AgencyContractItem toContract(Contract contract, Map<UUID, Property> propertiesById) {
        return new AgencyDashboardData.AgencyContractItem(
                contract.getId(),
                contract.getLocataireNom(),
                propertyName(contract.getPropertyId(), propertiesById),
                contract.getDateDebut(),
                contract.getDateFin(),
                contract.getLoyerMensuel(),
                contract.getStatut() == ContractStatus.ACTIF ? "active" : "expired",
                true);
    }

    private AgencyDashboardData.AgencyReceiptItem toReceipt(
            Payment payment,
            Map<UUID, Contract> contractsById,
            Map<UUID, Property> propertiesById) {
        Contract contract = contractsById.get(payment.getContractId());
        LocalDate issuedAt = payment.getDatePaiement() == null ? payment.getDateEcheance() : payment.getDatePaiement();
        return new AgencyDashboardData.AgencyReceiptItem(
                payment.getId(),
                contract == null ? "-" : contract.getLocataireNom(),
                contract == null ? "-" : propertyName(contract.getPropertyId(), propertiesById),
                issuedAt.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + issuedAt.getYear(),
                payment.getMontant(),
                issuedAt,
                "generated");
    }

    private List<AgencyDashboardData.AgencyActivityItem> activities(
            List<Property> properties,
            List<Contract> contracts,
            List<Payment> payments,
            List<Dispute> disputes) {
        return List.of(
                new AgencyDashboardData.AgencyActivityItem(1, "Biens suivis", properties.size() + " biens dans votre parc.", "Maintenant", "contract"),
                new AgencyDashboardData.AgencyActivityItem(2, "Locataires actifs", contracts.size() + " contrats enregistres.", "Maintenant", "tenant"),
                new AgencyDashboardData.AgencyActivityItem(3, "Paiements", payments.size() + " paiements suivis.", "Maintenant", "payment"),
                new AgencyDashboardData.AgencyActivityItem(4, "Litiges", disputes.size() + " dossiers litiges.", "Maintenant", "dispute"));
    }

    private List<AgencyDashboardData.AgencyRevenuePoint> monthlyRevenue(List<Payment> payments) {
        return payments.stream()
                .filter(payment -> payment.getStatut() == PaymentStatus.PAYE)
                .collect(Collectors.groupingBy(
                        payment -> {
                            LocalDate date = payment.getDatePaiement() == null ? payment.getDateEcheance() : payment.getDatePaiement();
                            return LocalDate.of(date.getYear(), date.getMonth(), 1);
                        },
                        Collectors.reducing(BigDecimal.ZERO, Payment::getMontant, BigDecimal::add)))
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> new AgencyDashboardData.AgencyRevenuePoint(
                        entry.getKey().getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH),
                        entry.getValue()))
                .toList();
    }

    private String propertyName(UUID propertyId, Map<UUID, Property> propertiesById) {
        Property property = propertiesById.get(propertyId);
        return property == null ? "-" : property.getTitre();
    }

    private String joinAddress(String address, String city) {
        if (address == null || address.isBlank()) {
            return city == null ? "" : city;
        }
        if (city == null || city.isBlank()) {
            return address;
        }
        return address + ", " + city;
    }
}
