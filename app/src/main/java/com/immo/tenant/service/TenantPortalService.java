package com.immo.tenant.service;

import com.immo.agency.dto.AgencyDashboardData;
import com.immo.auth.entity.User;
import com.immo.auth.repository.UserRepository;
import com.immo.common.exception.ResourceNotFoundException;
import com.immo.common.exception.UnauthorizedException;
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
import com.immo.tenant.dto.TenantContactRequest;
import com.immo.tenant.dto.TenantPasswordRequest;
import com.immo.tenant.dto.TenantProfileRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantPortalService {
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final DisputeRepository disputeRepository;
    private final PasswordEncoder passwordEncoder;

    public AgencyDashboardData dashboard(UUID userId) {
        User user = findUser(userId);
        String tenantId = user.getTenantId();

        List<Property> agencyProperties = propertyRepository.findByTenantIdOrderByTitreAsc(tenantId);
        Map<UUID, Property> propertiesById = agencyProperties.stream()
                .collect(Collectors.toMap(Property::getId, Function.identity()));
        List<Contract> contracts = contractRepository.findByTenantIdOrderByDateDebutDesc(tenantId).stream()
                .filter(contract -> user.getEmail().equalsIgnoreCase(nullToBlank(contract.getLocataireEmail())))
                .toList();
        Set<UUID> contractIds = contracts.stream().map(Contract::getId).collect(Collectors.toSet());
        List<Payment> payments = paymentRepository.findByTenantIdOrderByDateEcheanceDesc(tenantId).stream()
                .filter(payment -> contractIds.contains(payment.getContractId()))
                .toList();
        List<Dispute> disputes = disputeRepository.findByTenantIdOrderByIdDesc(tenantId).stream()
                .filter(dispute -> contractIds.contains(dispute.getContractId()))
                .toList();

        return new AgencyDashboardData(
                new AgencyDashboardData.AgencyInfo(user.getFullName(), user.getEmail(), "LOCATAIRE"),
                propertyRepository.findByTenantIdAndDisponibleTrueOrderByTitreAsc(tenantId).stream()
                        .map(this::toProperty)
                        .toList(),
                contracts.stream().map(contract -> toTenant(user, contract, propertiesById)).toList(),
                payments.stream().map(payment -> toPayment(payment, contracts, propertiesById)).toList(),
                disputes.stream().map(dispute -> toDispute(dispute, contracts)).toList(),
                contracts.stream().map(contract -> toContract(contract, propertiesById)).toList(),
                payments.stream()
                        .filter(payment -> payment.getStatut() == PaymentStatus.PAYE)
                        .map(payment -> toReceipt(payment, contracts, propertiesById))
                        .toList(),
                List.of(),
                monthlyRevenue(payments));
    }

    public AgencyDashboardData dashboard(String userId) {
        return dashboard(UUID.fromString(userId));
    }

    public AgencyDashboardData.AgencyTenantItem updateProfile(String userId, TenantProfileRequest request) {
        User user = findUser(UUID.fromString(userId));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUpdatedAt(OffsetDateTime.now());
        User saved = userRepository.save(user);
        Contract contract = contractRepository.findByTenantIdOrderByDateDebutDesc(saved.getTenantId()).stream()
                .filter(item -> saved.getEmail().equalsIgnoreCase(nullToBlank(item.getLocataireEmail())))
                .findFirst()
                .orElse(null);
        return new AgencyDashboardData.AgencyTenantItem(
                contract == null ? saved.getId() : contract.getId(),
                saved.getFullName(),
                saved.getEmail(),
                nullToBlank(saved.getPhone()),
                contract == null ? "-" : "-",
                saved.isActive(),
                saved.getCreatedAt() == null ? LocalDate.now() : saved.getCreatedAt().toLocalDate(),
                contract != null && contract.getStatut() == ContractStatus.ACTIF ? "active" : "ended",
                true);
    }

    public void changePassword(String userId, TenantPasswordRequest request) {
        User user = findUser(UUID.fromString(userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Mot de passe actuel incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);
    }

    public AgencyDashboardData.AgencyDisputeItem contactAgency(String userId, TenantContactRequest request) {
        User user = findUser(UUID.fromString(userId));
        Contract contract = contractRepository.findByTenantIdOrderByDateDebutDesc(user.getTenantId()).stream()
                .filter(item -> user.getEmail().equalsIgnoreCase(nullToBlank(item.getLocataireEmail())))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Contrat locataire introuvable"));
        Dispute dispute = disputeRepository.save(Dispute.builder()
                .tenantId(user.getTenantId())
                .contractId(contract.getId())
                .titre(request.getSubject())
                .description(request.getMessage())
                .priorite(mapPriority(request.getPriority()))
                .statut(DisputeStatus.OUVERT)
                .build());
        return new AgencyDashboardData.AgencyDisputeItem(dispute.getId(), user.getFullName(), dispute.getTitre(), LocalDate.now(), "ongoing");
    }

    public AgencyDashboardData.AgencyPaymentItem pay(String userId, UUID paymentId) {
        User user = findUser(UUID.fromString(userId));
        AgencyDashboardData data = dashboard(user.getId());
        Payment payment = paymentRepository.findByTenantIdAndId(user.getTenantId(), paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable"));
        boolean ownsPayment = data.payments().stream().anyMatch(item -> item.id().equals(paymentId));
        if (!ownsPayment) {
            throw new UnauthorizedException("Paiement non autorise");
        }
        payment.setStatut(PaymentStatus.PAYE);
        payment.setDatePaiement(LocalDate.now());
        Payment saved = paymentRepository.save(payment);
        List<Contract> contracts = contractRepository.findByTenantIdOrderByDateDebutDesc(user.getTenantId());
        Map<UUID, Property> properties = propertyRepository.findByTenantIdOrderByTitreAsc(user.getTenantId()).stream()
                .collect(Collectors.toMap(Property::getId, Function.identity()));
        return toPayment(saved, contracts, properties);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    private AgencyDashboardData.AgencyPropertyItem toProperty(Property property) {
        return new AgencyDashboardData.AgencyPropertyItem(
                property.getId(),
                property.getTitre(),
                joinAddress(property.getAdresse(), property.getVille()),
                property.isDisponible() ? "available" : "occupied",
                property.getLoyerMensuel());
    }

    private AgencyDashboardData.AgencyTenantItem toTenant(User user, Contract contract, Map<UUID, Property> propertiesById) {
        return new AgencyDashboardData.AgencyTenantItem(
                contract.getId(),
                user.getFullName() == null ? contract.getLocataireNom() : user.getFullName(),
                user.getEmail(),
                nullToBlank(user.getPhone()),
                propertyName(contract.getPropertyId(), propertiesById),
                contract.getStatut() == ContractStatus.ACTIF,
                contract.getDateDebut(),
                contract.getStatut() == ContractStatus.ACTIF ? "active" : "ended",
                true);
    }

    private AgencyDashboardData.AgencyPaymentItem toPayment(Payment payment, List<Contract> contracts, Map<UUID, Property> propertiesById) {
        Contract contract = contracts.stream().filter(item -> item.getId().equals(payment.getContractId())).findFirst().orElse(null);
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

    private AgencyDashboardData.AgencyDisputeItem toDispute(Dispute dispute, List<Contract> contracts) {
        Contract contract = contracts.stream().filter(item -> item.getId().equals(dispute.getContractId())).findFirst().orElse(null);
        return new AgencyDashboardData.AgencyDisputeItem(
                dispute.getId(),
                contract == null ? "-" : contract.getLocataireNom(),
                dispute.getTitre(),
                LocalDate.now(),
                dispute.getStatut() == DisputeStatus.RESOLU || dispute.getStatut() == DisputeStatus.FERME ? "resolved" : "ongoing");
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

    private AgencyDashboardData.AgencyReceiptItem toReceipt(Payment payment, List<Contract> contracts, Map<UUID, Property> propertiesById) {
        Contract contract = contracts.stream().filter(item -> item.getId().equals(payment.getContractId())).findFirst().orElse(null);
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
        if (address == null || address.isBlank()) return city == null ? "" : city;
        if (city == null || city.isBlank()) return address;
        return address + ", " + city;
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private String mapPriority(String priority) {
        return switch (nullToBlank(priority).toLowerCase()) {
            case "urgent" -> "HAUTE";
            case "low" -> "BASSE";
            default -> "NORMALE";
        };
    }
}
