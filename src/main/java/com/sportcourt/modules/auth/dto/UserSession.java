package com.sportcourt.modules.auth.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class UserSession {
    private final String accountId;
    private final String userId;
    private final String username;
    private final String displayName;
    private final Set<String> roleGroups;
    private final Map<String, Permission> permissions;
    private final String employeeId;
    private final String customerId;
    private final String branchId;
    private final boolean owner;
    private final LocalDateTime loginTime;

    public UserSession(String accountId, String userId, String username, String displayName,
                       Set<String> roleGroups, Map<String, Permission> permissions,
                       String employeeId, String customerId, String branchId,
                       boolean owner, LocalDateTime loginTime) {
        this.accountId = accountId;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.roleGroups = roleGroups == null ? Set.of() : Set.copyOf(roleGroups);
        this.permissions = permissions == null ? Map.of() : Map.copyOf(permissions);
        this.employeeId = employeeId;
        this.customerId = customerId;
        this.branchId = branchId;
        this.owner = owner;
        this.loginTime = loginTime;
    }

    public boolean hasPermission(String functionId, PermissionAction action) {
        if (owner) {
            return true;
        }
        Permission permission = permissions.get(functionId);
        return permission != null && permission.allows(action);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getRoleGroups() {
        return Collections.unmodifiableSet(roleGroups);
    }

    public Map<String, Permission> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getBranchId() {
        return branchId;
    }

    public boolean isOwner() {
        return owner;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
