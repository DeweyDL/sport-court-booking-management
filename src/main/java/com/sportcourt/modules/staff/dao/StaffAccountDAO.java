package com.sportcourt.modules.staff.dao;
import com.sportcourt.modules.staff.entity.StaffType; // (hoặc Branch)
import java.util.List;

public class StaffAccountDAO {

    public void insertAccount(Connection conn, String accountId, String userId,
                              String username, String passwordHash) {
        // INSERT INTO ACCOUNT
    }

    public void assignRoleGroup(Connection conn, String accountId, String groupId) {
        // INSERT INTO ACCOUNT_ROLE_GROUP
    }

    public void lockAccountByUserId(Connection conn, String userId) {
        // UPDATE ACCOUNT SET STATUS = 'LOCKED' WHERE USER_ID = ?
    }
}