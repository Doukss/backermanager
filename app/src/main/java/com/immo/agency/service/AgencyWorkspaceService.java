package com.immo.agency.service;

import com.immo.agency.dto.*;
import com.immo.agency.entity.Agency;
import com.immo.agency.repository.AgencyRepository;
import com.immo.common.exception.ResourceNotFoundException;
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

    public List<AgencyPropertyResponse> listProperties(String tenantId) {
        requireTenant(tenantId);
        return propertyRepository.findByTenantIdOrderByTitreAsc(tenantId).stream()
                .map(this::toPropertyResponse)
                .toList();
    }

    public AgencyPropertyResponse getProperty(String tenantId, UUID id) {
        return toPropertyResponse(findProperty(tenantId, id));
    }

    public AgencyPropertyResponse createProperty(String tenantId, AgencyPropertyRequest request) {
        requireTenant(tenantId);
        Property property = Property.builder()
                .tenantId(tenantId)
                .titre(request.getTitre())
                .adresse(request.getAdresse())
                .ville(request.getVille())
                .type(request.getType())
                .loyerMensuel(request.getLoyerMensuel())
                .surface(request.getSurface())
                .nombrePieces(request.getNombrePieces())
                .disponible(request.isDisponible())
                .build();
        return toPropertyResponse(propertyRepository.save(property));
    }

    public AgencyPropertyResponse updateProperty(String tenantId, UUID id, AgencyPropertyRequest request) {
        Property property = findProperty(tenantId, id);
        property.setTitre(request.getTitre());
        property.setAdresse(request.getAdresse());
        property.setVille(request.getVille());
        property.setType(request.getType());
        property.setLoyerMensuel(request.getLoyerMensuel());
        property.setSurface(request.getSurface());
        property.setNombrePieces(request.getNombrePieces());
        property.setDisponible(request.isDisponible());
        return toPropertyResponse(propertyRepository.save(property));
    }

    public void deleteProperty(String tenantId, UUID id) {
        propertyRepository.delete(findProperty(tenantId, id));
    }

    public List<AgencyContractResponse> listContracts(String tenantId) {
        requireTenant(tenantId);
        Map<UUID, Property> propertiesById = propertyRepository.findByTenantIdOrderByTitreAsc(tenantId).stream()
                .collect(Collectors.toMap(Property::getId, Function.identity()));
        return contractRepository.findByTenantIdOrderByDateDebutDesc(tenantId).stream()
                .map(contract -> toContractResponse(contract, propertiesById))
                .toList();
    }

    public AgencyContractResponse getContract(String tenantId, UUID id) {
        Contract contract = findContract(tenantId, id);
        return toContractResponse(contract, propertiesByTenant(tenantId));
    }

    public AgencyContractResponse createContract(String tenantId, AgencyContractRequest request) {
        requireTenant(tenantId);
        Property property = findProperty(tenantId, request.getPropertyId());
        Contract contract = Contract.builder()
                .tenantId(tenantId)
                .propertyId(property.getId())
                .locataireNom(request.getLocataireNom())
                .locataireEmail(request.getLocataireEmail())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .loyerMensuel(request.getLoyerMensuel())
                .depot(request.getDepot())
                .statut(request.getStatut())
                .build();
        Contract saved = contractRepository.save(contract);
        syncPropertyAvailability(property, saved.getStatut());
        return toContractResponse(saved, propertiesByTenant(tenantId));
    }

    public AgencyContractResponse updateContract(String tenantId, UUID id, AgencyContractRequest request) {
        Contract contract = findContract(tenantId, id);
        Property property = findProperty(tenantId, request.getPropertyId());
        contract.setPropertyId(property.getId());
        contract.setLocataireNom(request.getLocataireNom());
        contract.setLocataireEmail(request.getLocataireEmail());
        contract.setDateDebut(request.getDateDebut());
        contract.setDateFin(request.getDateFin());
        contract.setLoyerMensuel(request.getLoyerMensuel());
        contract.setDepot(request.getDepot());
        contract.setStatut(request.getStatut());
        Contract saved = contractRepository.save(contract);
        syncPropertyAvailability(property, saved.getStatut());
        return toContractResponse(saved, propertiesByTenant(tenantId));
    }

    public void deleteContract(String tenantId, UUID id) {
        contractRepository.delete(findContract(tenantId, id));
    }

    public List<AgencyContractResponse> listTenants(String tenantId) {
        return listContracts(tenantId);
    }

    public List<AgencyPaymentResponse> listPayments(String tenantId) {
        requireTenant(tenantId);
        Map<UUID, Contract> contractsById = contractsByTenant(tenantId);
        Map<UUID, Property> propertiesById = propertiesByTenant(tenantId);
        return paymentRepository.findByTenantIdOrderByDateEcheanceDesc(tenantId).stream()
                .map(payment -> toPaymentResponse(payment, contractsById, propertiesById))
                .toList();
    }

    public AgencyPaymentResponse getPayment(String tenantId, UUID id) {
        Payment payment = findPayment(tenantId, id);
        return toPaymentResponse(payment, contractsByTenant(tenantId), propertiesByTenant(tenantId));
    }

    public AgencyPaymentResponse createPayment(String tenantId, AgencyPaymentRequest request) {
        requireTenant(tenantId);
        findContract(tenantId, request.getContractId());
        Payment payment = Payment.builder()
                .tenantId(tenantId)
                .contractId(request.getContractId())
                .montant(request.getMontant())
                .dateEcheance(request.getDateEcheance())
                .datePaiement(request.getDatePaiement())
                .statut(request.getStatut())
                .reference(request.getReference())
                .build();
        Payment saved = paymentRepository.save(payment);
        return toPaymentResponse(saved, contractsByTenant(tenantId), propertiesByTenant(tenantId));
    }

    public AgencyPaymentResponse updatePayment(String tenantId, UUID id, AgencyPaymentRequest request) {
        Payment payment = findPayment(tenantId, id);
        findContract(tenantId, request.getContractId());
        payment.setContractId(request.getContractId());
        payment.setMontant(request.getMontant());
        payment.setDateEcheance(request.getDateEcheance());
        payment.setDatePaiement(request.getDatePaiement());
        payment.setStatut(request.getStatut());
        payment.setReference(request.getReference());
        Payment saved = paymentRepository.save(payment);
        return toPaymentResponse(saved, contractsByTenant(tenantId), propertiesByTenant(tenantId));
    }

    public AgencyPaymentResponse markPaymentPaid(String tenantId, UUID id) {
        Payment payment = findPayment(tenantId, id);
        payment.setStatut(PaymentStatus.PAYE);
        payment.setDatePaiement(payment.getDatePaiement() == null ? LocalDate.now() : payment.getDatePaiement());
        Payment saved = paymentRepository.save(payment);
        return toPaymentResponse(saved, contractsByTenant(tenantId), propertiesByTenant(tenantId));
    }

    public void deletePayment(String tenantId, UUID id) {
        paymentRepository.delete(findPayment(tenantId, id));
    }

    public List<AgencyReceiptResponse> listReceipts(String tenantId) {
        requireTenant(tenantId);
        Map<UUID, Contract> contractsById = contractsByTenant(tenantId);
        Map<UUID, Property> propertiesById = propertiesByTenant(tenantId);
        return paymentRepository.findByTenantIdOrderByDateEcheanceDesc(tenantId).stream()
                .filter(payment -> payment.getStatut() == PaymentStatus.PAYE)
                .map(payment -> toReceiptResponse(payment, contractsById, propertiesById))
                .toList();
    }

    public List<AgencyDisputeResponse> listDisputes(String tenantId) {
        requireTenant(tenantId);
        Map<UUID, Contract> contractsById = contractsByTenant(tenantId);
        return disputeRepository.findByTenantIdOrderByIdDesc(tenantId).stream()
                .map(dispute -> toDisputeResponse(dispute, contractsById))
                .toList();
    }

    public AgencyDisputeResponse getDispute(String tenantId, UUID id) {
        Dispute dispute = findDispute(tenantId, id);
        return toDisputeResponse(dispute, contractsByTenant(tenantId));
    }

    public AgencyDisputeResponse createDispute(String tenantId, AgencyDisputeRequest request) {
        requireTenant(tenantId);
        if (request.getContractId() != null) {
            findContract(tenantId, request.getContractId());
        }
        Dispute dispute = Dispute.builder()
                .tenantId(tenantId)
                .contractId(request.getContractId())
                .titre(request.getTitre())
                .description(request.getDescription())
                .statut(request.getStatut())
                .priorite(request.getPriorite())
                .resolution(request.getResolution())
                .build();
        return toDisputeResponse(disputeRepository.save(dispute), contractsByTenant(tenantId));
    }

    public AgencyDisputeResponse updateDispute(String tenantId, UUID id, AgencyDisputeRequest request) {
        Dispute dispute = findDispute(tenantId, id);
        if (request.getContractId() != null) {
            findContract(tenantId, request.getContractId());
        }
        dispute.setContractId(request.getContractId());
        dispute.setTitre(request.getTitre());
        dispute.setDescription(request.getDescription());
        dispute.setStatut(request.getStatut());
        dispute.setPriorite(request.getPriorite());
        dispute.setResolution(request.getResolution());
        return toDisputeResponse(disputeRepository.save(dispute), contractsByTenant(tenantId));
    }

    public AgencyDisputeResponse resolveDispute(String tenantId, UUID id, String resolution) {
        Dispute dispute = findDispute(tenantId, id);
        dispute.setStatut(DisputeStatus.RESOLU);
        dispute.setResolution(resolution);
        return toDisputeResponse(disputeRepository.save(dispute), contractsByTenant(tenantId));
    }

    public void deleteDispute(String tenantId, UUID id) {
        disputeRepository.delete(findDispute(tenantId, id));
    }

    public AgencyDashboardData dashboard(String tenantId) {
        requireTenant(tenantId);
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

    private void requireTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Le header X-Tenant-ID est obligatoire");
        }
    }

    private Property findProperty(String tenantId, UUID id) {
        requireTenant(tenantId);
        return propertyRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Bien introuvable"));
    }

    private Contract findContract(String tenantId, UUID id) {
        requireTenant(tenantId);
        return contractRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat introuvable"));
    }

    private Payment findPayment(String tenantId, UUID id) {
        requireTenant(tenantId);
        return paymentRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable"));
    }

    private Dispute findDispute(String tenantId, UUID id) {
        requireTenant(tenantId);
        return disputeRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Litige introuvable"));
    }

    private Map<UUID, Property> propertiesByTenant(String tenantId) {
        return propertyRepository.findByTenantIdOrderByTitreAsc(tenantId).stream()
                .collect(Collectors.toMap(Property::getId, Function.identity()));
    }

    private Map<UUID, Contract> contractsByTenant(String tenantId) {
        return contractRepository.findByTenantIdOrderByDateDebutDesc(tenantId).stream()
                .collect(Collectors.toMap(Contract::getId, Function.identity()));
    }

    private void syncPropertyAvailability(Property property, ContractStatus status) {
        property.setDisponible(status != ContractStatus.ACTIF);
        propertyRepository.save(property);
    }

    private AgencyPropertyResponse toPropertyResponse(Property property) {
        return AgencyPropertyResponse.builder()
                .id(property.getId())
                .tenantId(property.getTenantId())
                .titre(property.getTitre())
                .adresse(property.getAdresse())
                .ville(property.getVille())
                .type(property.getType())
                .loyerMensuel(property.getLoyerMensuel())
                .surface(property.getSurface())
                .nombrePieces(property.getNombrePieces())
                .disponible(property.isDisponible())
                .build();
    }

    private AgencyContractResponse toContractResponse(Contract contract, Map<UUID, Property> propertiesById) {
        return AgencyContractResponse.builder()
                .id(contract.getId())
                .tenantId(contract.getTenantId())
                .propertyId(contract.getPropertyId())
                .propertyTitle(propertyName(contract.getPropertyId(), propertiesById))
                .locataireNom(contract.getLocataireNom())
                .locataireEmail(contract.getLocataireEmail())
                .dateDebut(contract.getDateDebut())
                .dateFin(contract.getDateFin())
                .loyerMensuel(contract.getLoyerMensuel())
                .depot(contract.getDepot())
                .statut(contract.getStatut())
                .build();
    }

    private AgencyPaymentResponse toPaymentResponse(
            Payment payment,
            Map<UUID, Contract> contractsById,
            Map<UUID, Property> propertiesById) {
        Contract contract = contractsById.get(payment.getContractId());
        return AgencyPaymentResponse.builder()
                .id(payment.getId())
                .tenantId(payment.getTenantId())
                .contractId(payment.getContractId())
                .tenantName(contract == null ? "-" : contract.getLocataireNom())
                .propertyTitle(contract == null ? "-" : propertyName(contract.getPropertyId(), propertiesById))
                .montant(payment.getMontant())
                .dateEcheance(payment.getDateEcheance())
                .datePaiement(payment.getDatePaiement())
                .statut(payment.getStatut())
                .reference(payment.getReference())
                .build();
    }

    private AgencyDisputeResponse toDisputeResponse(Dispute dispute, Map<UUID, Contract> contractsById) {
        Contract contract = contractsById.get(dispute.getContractId());
        return AgencyDisputeResponse.builder()
                .id(dispute.getId())
                .tenantId(dispute.getTenantId())
                .contractId(dispute.getContractId())
                .tenantName(contract == null ? "-" : contract.getLocataireNom())
                .titre(dispute.getTitre())
                .description(dispute.getDescription())
                .statut(dispute.getStatut())
                .priorite(dispute.getPriorite())
                .resolution(dispute.getResolution())
                .build();
    }

    private AgencyReceiptResponse toReceiptResponse(
            Payment payment,
            Map<UUID, Contract> contractsById,
            Map<UUID, Property> propertiesById) {
        Contract contract = contractsById.get(payment.getContractId());
        LocalDate issuedAt = payment.getDatePaiement() == null ? payment.getDateEcheance() : payment.getDatePaiement();
        return AgencyReceiptResponse.builder()
                .paymentId(payment.getId())
                .contractId(payment.getContractId())
                .tenantName(contract == null ? "-" : contract.getLocataireNom())
                .propertyTitle(contract == null ? "-" : propertyName(contract.getPropertyId(), propertiesById))
                .period(issuedAt.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + issuedAt.getYear())
                .amount(payment.getMontant())
                .issuedAt(issuedAt)
                .reference(payment.getReference())
                .build();
    }
}
