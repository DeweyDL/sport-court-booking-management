package com.sportcourt.modules.account.dao;

import com.sportcourt.modules.account.dto.AccountRow;
import com.sportcourt.modules.account.dto.AccountUpsertRequest;
import com.sportcourt.modules.account.dto.RoleGroupOption;

import java.sql.SQLException;
import java.util.List;

public interface AccountManagementDAO {
    List<AccountRow> findAccounts(String keyword) throws SQLException;

    List<RoleGroupOption> findRoleGroupOptions() throws SQLException;

    void assignRoleGroup(String accountId, String groupId) throws SQLException;

    void createAccount(String userId, String accountId, String accountRoleGroupId, AccountUpsertRequest request, String passwordHash) throws SQLException;

    boolean updateAccount(AccountUpsertRequest request) throws SQLException;

    boolean softDeleteAccount(String accountId) throws SQLException;

    boolean restoreAccount(String accountId) throws SQLException;
}
