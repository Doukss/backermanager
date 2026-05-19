package com.immo.agency.service;

import com.immo.agency.dto.*;
import com.immo.agency.entity.Agency;
import com.immo.agency.entity.PlatformNotification;
import com.immo.agency.entity.Subscription;
import com.immo.agency.repository.AgencyRepository;
import com.immo.agency.repository.PlatformNotificationRepository;
import com.immo.agency.repository.SubscriptionRepository;
import com.immo.common.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminAgencyService {
    private final AgencyRepository agencyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlatformNotificationRepository notificationRepository;

    public DashboardResponse dashboard() {
        return DashboardResponse.builder()
                .agencies(agencyRepository.count())
                .activeAgencies(agencyRepository.countByActive(true))
                .suspendedAgencies(agencyRepository.countByActive(false))
                .activeSubscriptions(subscriptionRepository.findByStatus("ACTIVE").size())
                .monthlyRevenue(subscriptionRepository.currentMonthlyRevenue())
                .build();
    }

    public List<AgencyResponse> listAgencies() {
        return agencyRepository.findAll().stream().map(this::toAgencyResponse).toList();
    }

    public AgencyResponse createAgency(AgencyRequest request) {
        Agency agency = Agency.builder()
                .tenantId(request.getTenantId())
                .nom(request.getNom())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .plan(request.getPlan())
                .active(true)
                .createdAt(OffsetDateTime.now())
                .build();
        Agency saved = agencyRepository.save(agency);
        createDefaultSubscription(saved);
        notify("Nouvelle agence", saved.getNom() + " a ete creee.", "AGENCY", "MEDIUM", saved.getNom());
        return toAgencyResponse(saved);
    }

    public AgencyResponse setAgencyActive(UUID id, boolean active) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence introuvable"));
        agency.setActive(active);
        Agency saved = agencyRepository.save(agency);
        subscriptionRepository.findByTenantId(saved.getTenantId()).ifPresent(subscription -> {
            subscription.setStatus(active ? "ACTIVE" : "SUSPENDED");
            subscriptionRepository.save(subscription);
        });
        notify(active ? "Agence reactivee" : "Agence suspendue", saved.getNom(), "AGENCY", active ? "LOW" : "HIGH", saved.getNom());
        return toAgencyResponse(saved);
    }

    public List<SubscriptionResponse> listSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(this::toSubscriptionResponse)
                .toList();
    }

    public List<PlatformNotification> listNotifications() {
        return notificationRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public PlatformNotification markNotificationRead(UUID id) {
        PlatformNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    private void createDefaultSubscription(Agency agency) {
        BigDecimal price = switch (agency.getPlan() == null ? "STARTER" : agency.getPlan().toUpperCase()) {
            case "PRO", "PROFESSIONNEL" -> BigDecimal.valueOf(15000);
            case "ENTERPRISE" -> BigDecimal.valueOf(45000);
            default -> BigDecimal.ZERO;
        };
        subscriptionRepository.save(Subscription.builder()
                .tenantId(agency.getTenantId())
                .plan(agency.getPlan())
                .status("ACTIVE")
                .monthlyPrice(price)
                .startedAt(LocalDate.now())
                .nextBillingAt(LocalDate.now().plusMonths(1))
                .build());
    }

    private void notify(String title, String message, String type, String priority, String target) {
        notificationRepository.save(PlatformNotification.builder()
                .title(title)
                .message(message)
                .type(type)
                .priority(priority)
                .target(target)
                .read(false)
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private AgencyResponse toAgencyResponse(Agency agency) {
        return AgencyResponse.builder()
                .id(agency.getId())
                .tenantId(agency.getTenantId())
                .nom(agency.getNom())
                .adresse(agency.getAdresse())
                .telephone(agency.getTelephone())
                .email(agency.getEmail())
                .plan(agency.getPlan())
                .active(agency.isActive())
                .createdAt(agency.getCreatedAt())
                .build();
    }

    private SubscriptionResponse toSubscriptionResponse(Subscription subscription) {
        String agencyName = agencyRepository.findByTenantId(subscription.getTenantId())
                .map(Agency::getNom)
                .orElse(subscription.getTenantId());
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .tenantId(subscription.getTenantId())
                .agencyName(agencyName)
                .plan(subscription.getPlan())
                .status(subscription.getStatus())
                .monthlyPrice(subscription.getMonthlyPrice())
                .startedAt(subscription.getStartedAt())
                .nextBillingAt(subscription.getNextBillingAt())
                .build();
    }
}
