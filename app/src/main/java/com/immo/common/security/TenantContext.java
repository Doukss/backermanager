package com.immo.common.security;
public class TenantContext {
    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();
    public static void setTenantId(String id) { TENANT.set(id); }
    public static String getTenantId()         { return TENANT.get(); }
    public static void clear()                 { TENANT.remove(); }
}
