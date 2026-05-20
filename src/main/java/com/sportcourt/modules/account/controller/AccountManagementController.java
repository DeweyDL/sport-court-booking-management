package com.sportcourt.modules.account.controller;

import com.sportcourt.modules.account.dto.AccountRow;
import com.sportcourt.modules.account.dto.AccountUpsertRequest;
import com.sportcourt.modules.account.dto.RoleGroupOption;
import com.sportcourt.modules.account.service.AccountManagementService;
import com.sportcourt.modules.account.service.AccountManagementServiceImpl;

import java.sql.SQLException;
import java.util.List;

public class AccountManagementController {
    private final AccountManagementService accountManagementService;

    public AccountManagementController() {
        this(new AccountManagementServiceImpl());
    }

    public AccountManagementController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    public List<AccountRow> searchAccounts(String keyword) throws SQLException {
        return accountManagementService.searchAccounts(keyword);
    }

    public List<RoleGroupOption> getRoleGroups() throws SQLException {
        return accountManagementService.getRoleGroups();
    }

    public String generateNextAccountId() throws SQLException {
        return accountManagementService.generateNextAccountId();
    }

    public void assignRoleGroup(String accountId, String groupId) throws SQLException {
        accountManagementService.assignRoleGroup(accountId, groupId);
    }

    public void createAccount(AccountUpsertRequest request) throws SQLException {
        accountManagementService.createAccount(request);
    }

    public void updateAccount(AccountUpsertRequest request) throws SQLException {
        accountManagementService.updateAccount(request);
    }

    public void deleteAccount(String accountId) throws SQLException {
        accountManagementService.deleteAccount(accountId);
    }

    public void restoreAccount(String accountId) throws SQLException {
        accountManagementService.restoreAccount(accountId);
    }
}
