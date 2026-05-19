package com.immo.agency.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AgencyDashboardData(
        AgencyInfo agency,
        List<AgencyPropertyItem> properties,
        List<AgencyTenantItem> tenants,
        List<AgencyPaymentItem> payments,
        List<AgencyDisputeItem> disputes,
        List<AgencyContractItem> contracts,
        List<AgencyReceiptItem> receipts,
        List<AgencyActivityItem> activities,
        List<AgencyRevenuePoint> monthlyRevenue) {

    public record AgencyInfo(String name, String email, String plan) {
    }

    public record AgencyPropertyItem(UUID id, String name, String address, String status, BigDecimal monthlyRent) {
    }

    public record AgencyTenantItem(
            UUID id,
            String fullName,
            String email,
            String phone,
            String propertyName,
            boolean active,
            LocalDate joinedAt,
            String contractStatus,
            boolean isRegistered) {
    }

    public record AgencyPaymentItem(
            UUID id,
            String tenantName,
            String propertyName,
            BigDecimal amount,
            LocalDate date,
            String status) {
    }

    public record AgencyDisputeItem(UUID id, String tenantName, String subject, LocalDate date, String status) {
    }

    public record AgencyContractItem(
            UUID id,
            String tenantName,
            String propertyName,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal monthlyRent,
            String status,
            boolean signed) {
    }

    public record AgencyReceiptItem(
            UUID id,
            String tenantName,
            String propertyName,
            String period,
            BigDecimal amount,
            LocalDate issuedAt,
            String status) {
    }

    public record AgencyActivityItem(long id, String title, String description, String time, String type) {
    }

    public record AgencyRevenuePoint(String month, BigDecimal revenue) {
    }
}
