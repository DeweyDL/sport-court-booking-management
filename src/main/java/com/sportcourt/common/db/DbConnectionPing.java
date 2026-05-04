package com.sportcourt.common.db;

import java.sql.Connection;
import java.sql.SQLException;

public class DbConnectionPing {
    public static void main(String[] args) {
        System.out.println("--- ĐANG KIỂM TRA KẾT NỐI QUA CONNECTIONUTILS ---");

        Connection conn = null;
        try {
            // Sử dụng hàm getMyConnection từ file ConnectionUtils ông vừa đưa
            conn = ConnectionUtils.getMyConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ KẾT NỐI THÀNH CÔNG!");
                System.out.println("Cổng kết nối: " + conn.toString());
                System.out.println("Database Product: " + conn.getMetaData().getDatabaseProductVersion());
            }

        } catch (SQLException e) {
            System.err.println("❌ LỖI KẾT NỐI!");
            System.err.println("Chi tiết: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Sử dụng hàm close an toàn từ ConnectionUtils để giải phóng tài nguyên
            ConnectionUtils.close(conn, null, null);
            System.out.println("--- ĐÃ ĐÓNG KẾT NỐI AN TOÀN ---");
        }
    }
}