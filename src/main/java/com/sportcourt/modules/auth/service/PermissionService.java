package com.sportcourt.modules.auth.service;

import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.dto.UserSession;

import java.sql.*;

public interface PermissionService {
    UserSession loadUserSession(String accountId) throws SQLException;

    boolean hasPermission(String functionId, PermissionAction action);

    void requirePermission(String functionId, PermissionAction action);
}
