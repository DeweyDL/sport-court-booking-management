package com.sportcourt.modules.auth.service;

import com.sportcourt.modules.auth.dao.PermissionDAO;
import com.sportcourt.modules.auth.dao.PermissionDAOImpl;
import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.dto.UserSession;

import java.sql.SQLException;

public class PermissionServiceImpl implements PermissionService {
    private final PermissionDAO permissionDAO;

    public PermissionServiceImpl() {
        this(new PermissionDAOImpl());
    }

    public PermissionServiceImpl(PermissionDAO permissionDAO) {
        this.permissionDAO = permissionDAO;
    }

    @Override
    public UserSession loadUserSession(String accountId) throws SQLException {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID không được để trống.");
        }
        return permissionDAO.loadUserSession(accountId);
    }

    @Override
    public boolean hasPermission(String functionId, PermissionAction action) {
        return SessionManager.getCurrentSession()
                             .map(session->session.hasPermission(functionId, action))
                             .orElse(false);
    }

    @Override
    public void requirePermission(String functionId, PermissionAction action) {
        if (!hasPermission(functionId, action)) {
            throw new SecurityException("Bạn không có quyền thực hiện chức năng này");
        }
    }
}
