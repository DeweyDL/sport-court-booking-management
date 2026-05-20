package com.sportcourt.modules.auth.service;

import com.sportcourt.modules.auth.dto.UserSession;

import java.util.Optional;

public class SessionManager {
    private static UserSession currentSession;

    private SessionManager() {
    }

    public static Optional<UserSession> getCurrentSession() {
        return Optional.ofNullable(currentSession);
    }

    public static void setCurrentSession(UserSession session) {
        currentSession = session;
    }

    public static UserSession requireSession() {
        if (currentSession == null) {
            throw new IllegalStateException("Người dùng chưa đăng nhập.");
        }
        return currentSession;
    }

    public static void clear() {
        currentSession = null;
    }
}