package com.sportcourt.modules.auth.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.auth.dto.Permission;
import com.sportcourt.modules.auth.dto.UserSession;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionDAOImpl implements PermissionDAO {

    @Override
    public UserSession loadUserSession(String accountId) throws SQLException {
        AccountScope scope = loadAccountScope(accountId);
        Set<String> roleGroups = loadRoleGroups(accountId);
        Map<String, PermissionAccumulator> accumulators = new HashMap<>();

        loadGroupPermissions(accountId, accumulators);
        loadDirectPermissions(accountId, accumulators);

        Map<String, Permission> permissions = new HashMap<>();
        for (Map.Entry<String, PermissionAccumulator> entry : accumulators.entrySet()) {
            permissions.put(entry.getKey(), entry.getValue().toPermission(entry.getKey()));
        }

        boolean isOwner = roleGroups.contains("OWNER");

        return new UserSession(
                scope.accountId(),
                scope.userId(),
                scope.username(),
                scope.displayName(),
                roleGroups,
                permissions,
                scope.employeeId(),
                scope.customerId(),
                scope.branchId(),
                isOwner,
                LocalDateTime.now()
        );

    }

    private record AccountScope(
            String accountId,
            String userId,
            String username,
            String displayName,
            String employeeId,
            String customerId,
            String branchId
    ) {

    }

    private static class PermissionAccumulator {
        private boolean canView;
        private boolean canAdd;
        private boolean canEdit;
        private boolean canDelete;
        private boolean canDownload;

        void merge(boolean canView, boolean canAdd, boolean canEdit, boolean canDelete, boolean canDownload) {
            this.canView = this.canView || canView;
            this.canAdd = this.canAdd || canAdd;
            this.canEdit = this.canEdit || canEdit;
            this.canDelete = this.canDelete || canDelete;
            this.canDownload = this.canDownload || canDownload;
        }

        Permission toPermission(String functionId) {
            return new Permission(functionId, canView, canAdd, canEdit, canDelete, canDownload);
        }
    }

    private AccountScope loadAccountScope(String accountId) throws SQLException {
        String sql = """
                SELECT  a.ACCOUNT_ID,
                        a.USER_ID,
                        a.USERNAME,
                        u.HOTEN,
                        nv.MANV,
                        nv.MACN,
                        kh.MAKH
                FROM ACCOUNT a
                JOIN USERS u
                    ON u.USER_ID = a.USER_ID
                    AND u.IS_DELETED = 0
                LEFT JOIN NHAN_VIEN nv
                    ON nv.USER_ID = a.USER_ID
                    AND nv.IS_DELETED = 0
                LEFT JOIN KHACH_HANG kh
                    ON kh.USER_ID = a.USER_ID
                    AND kh.IS_DELETED = 0
                WHERE a.ACCOUNT_ID = ?
                AND a.STATUS = 'ACTIVE'
                AND a.IS_DELETED = 0
                """;
        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Account is not active or does not exist");
                }
                return new AccountScope(
                        rs.getString("ACCOUNT_ID"),
                        rs.getString("USER_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("HOTEN"),
                        rs.getString("MANV"),
                        rs.getString("MAKH"),
                        rs.getString("MACN")
                );
            }
        }
    }

    private void loadPermissions(String accountId, Map<String, PermissionAccumulator> permissions, String sql) throws SQLException {
        try (Connection connection = ConnectionUtils.getMyConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String functionId = rs.getString("FUNCTION_ID");
                    PermissionAccumulator accumulator = permissions.computeIfAbsent(
                            functionId,
                            ignored -> new PermissionAccumulator()
                    );

                    accumulator.merge(
                            rs.getInt("CAN_VIEW") == 1,
                            rs.getInt("CAN_ADD") == 1,
                            rs.getInt("CAN_EDIT") == 1,
                            rs.getInt("CAN_DELETE") == 1,
                            rs.getInt("CAN_DOWNLOAD") == 1
                    );
                }
            }
        }
    }

    private Set<String> loadRoleGroups(String accountId) throws SQLException {
        String sql = """
                SELECT rg.GROUP_ID
                FROM ACCOUNT_ROLE_GROUP arg
                JOIN ROLE_GROUP rg
                    ON rg.GROUP_ID = arg.GROUP_ID 
                    AND rg.IS_DELETED = 0
                WHERE arg.ACCOUNT_ID = ?
                AND arg.IS_DELETED = 0
                """;

        Set<String> roleGroups = new HashSet<>();

        try (Connection conn = ConnectionUtils.getMyConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roleGroups.add(rs.getString("GROUP_ID"));
                }
            }
        }
        return roleGroups;
    }

    private void loadGroupPermissions(String accountId, Map<String, PermissionAccumulator> permissions) throws SQLException {
        String sql = """
                SELECT  f.FUNCTION_ID,
                        r.CAN_VIEW,
                        r.CAN_ADD,
                        r.CAN_EDIT,
                        r.CAN_DELETE,
                        r.CAN_DOWNLOAD
                FROM ACCOUNT_ROLE_GROUP arg
                JOIN ACCOUNT_ROLE_GROUP_MAPPING m
                    ON m.GROUP_ID = arg.GROUP_ID
                    AND m.IS_DELETED = 0
                JOIN ROLE r
                    ON r.ROLE_ID = m.ROLE_ID
                    AND r.IS_DELETED = 0
                JOIN FUNCTIONS f
                    ON f.FUNCTION_ID = r.FUNCTION_ID
                    AND f.IS_DELETED = 0
                WHERE arg.ACCOUNT_ID = ?
                  AND arg.IS_DELETED = 0
                """;

        loadPermissions(accountId, permissions, sql);
    }

    private void loadDirectPermissions(String accountId, Map<String, PermissionAccumulator> permissions) throws SQLException {
        String sql = """
                SELECT  f.FUNCTION_ID,
                        r.CAN_VIEW,
                        r.CAN_ADD,
                        r.CAN_EDIT,
                        r.CAN_DELETE,
                        r.CAN_DOWNLOAD
                FROM ACCOUNT_ROLE ar
                JOIN ROLE r
                    ON r.ROLE_ID = ar.ROLE_ID
                    AND r.IS_DELETED = 0
                JOIN FUNCTIONS f
                    ON f.FUNCTION_ID = r.FUNCTION_ID
                    AND f.IS_DELETED = 0
                WHERE ar.ACCOUNT_ID = ?
                  AND ar.IS_DELETED = 0
                """;

        loadPermissions(accountId, permissions, sql);
    }
}
