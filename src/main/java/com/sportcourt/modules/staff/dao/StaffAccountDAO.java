package com.sportcourt.modules.staff.dao;

import com.sportcourt.common.db.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StaffAccountDAO {

    public void insertAccount(Connection conn,
                              String accountId,
                              String userId,
                              String username,
                              String passwordHash) {
        String sql = "INSERT INTO ACCOUNT ("
                + "ACCOUNT_ID, USER_ID, USERNAME, PASSWORD_HASH, STATUS, CREATED_AT, IS_DELETED"
                + ") VALUES (?, ?, ?, ?, 'ACTIVE', SYSDATE, 0)";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, accountId);
            ps.setString(2, userId);
            ps.setString(3, username);
            ps.setString(4, passwordHash);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tạo tài khoản nhân viên.", e);
        } finally {
            ConnectionUtils.close(null, ps, null);
        }
    }

    public void assignRoleGroup(Connection conn,
                                String accountRoleGroupId,
                                String accountId,
                                String groupId) {
        String sql = "INSERT INTO ACCOUNT_ROLE_GROUP ("
                + "ACCOUNT_ROLE_GROUP_ID, ACCOUNT_ID, GROUP_ID, CREATED_AT, IS_DELETED"
                + ") VALUES (?, ?, ?, SYSDATE, 0)";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, accountRoleGroupId);
            ps.setString(2, accountId);
            ps.setString(3, groupId);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể gán nhóm quyền cho tài khoản nhân viên.", e);
        } finally {
            ConnectionUtils.close(null, ps, null);
        }
    }

    public void lockAccountByUserId(Connection conn, String userId) {
        String sql = "UPDATE ACCOUNT "
                + "SET STATUS = 'LOCKED' "
                + "WHERE USER_ID = ? "
                + "AND IS_DELETED = 0";

        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể khóa tài khoản nhân viên.", e);
        } finally {
            ConnectionUtils.close(null, ps, null);
        }
    }
}
