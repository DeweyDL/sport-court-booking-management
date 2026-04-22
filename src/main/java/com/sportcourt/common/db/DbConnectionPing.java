package com.sportcourt.common.db;

import java.sql.Connection;

public class DbConnectionPing {
    public static void main(String[] args) {
        System.out.println("=== DB Connection Ping ===");
        try (Connection connection = ConnectionUtils.getMyConnection()) {
            boolean ok = connection != null && !connection.isClosed();
            System.out.println("KET_QUA: " + (ok ? "THANH_CONG" : "THAT_BAI"));
            if (ok) {
                System.out.println("Driver: " + connection.getMetaData().getDriverName());
                System.out.println("URL: " + connection.getMetaData().getURL());
                System.out.println("User: " + connection.getMetaData().getUserName());
            }
        } catch (Exception e) {
            System.out.println("KET_QUA: THAT_BAI");
            System.out.println("LOI: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
