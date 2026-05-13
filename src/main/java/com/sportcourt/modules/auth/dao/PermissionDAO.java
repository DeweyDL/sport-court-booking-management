package com.sportcourt.modules.auth.dao;

import com.sportcourt.modules.auth.dto.UserSession;

import java.sql.*;

public interface PermissionDAO {
    public UserSession loadUserSession(String accountId) throws SQLException;
}
