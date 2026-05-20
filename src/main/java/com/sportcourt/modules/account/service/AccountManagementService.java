package com.sportcourt.modules.account.service;

import com.sportcourt.modules.account.dto.AccountRow;
import com.sportcourt.modules.account.dto.AccountUpsertRequest;
import com.sportcourt.modules.account.dto.RoleGroupOption;

import java.sql.SQLException;
import java.util.List;

public interface AccountManagementService {
    List<AccountRow> searchAccounts(String keyword) throws SQLException;

    List<RoleGroupOption> getRoleGroups() throws SQLException;

    String generateNextAccountId() throws SQLException;

    void assignRoleGroup(String accountId, String groupId) throws SQLException;

    void createAccount(AccountUpsertRequest request) throws SQLException;

    void updateAccount(AccountUpsertRequest request) throws SQLException;

    void deleteAccount(String accountId) throws SQLException;

    void restoreAccount(String accountId) throws SQLException;
}
