package com.sportcourt.modules.payment.dao;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.payment.entity.Payment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class JdbcPaymentDao implements PaymentDao {

    @Override
    public BigDecimal findDeposit(String maHd) throws SQLException {
        String sql = "SELECT TIEN_COC FROM HOA_DON WHERE MAHD = ? AND IS_DELETED = 0";
        try (Connection c = ConnectionUtils.getMyConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("TIEN_COC") : null;
            }
        }
    }

    @Override
    public String findInvoiceStatus(String maHd) throws SQLException {
        String sql = "SELECT TRANGTHAI FROM HOA_DON WHERE MAHD = ? AND IS_DELETED = 0";
        try (Connection c = ConnectionUtils.getMyConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maHd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("TRANGTHAI") : null;
            }
        }
    }

    @Override
    public void insert(Payment p) throws SQLException {
        String sql = """
                INSERT INTO PAYMENT (MA_TT, MAHD, ORDER_CODE, PAYMENT_LINK_ID, SO_TIEN, TRANGTHAI)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection c = ConnectionUtils.getMyConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.maTt());
            ps.setString(2, p.maHd());
            ps.setLong(3, p.orderCode());
            ps.setString(4, p.paymentLinkId());
            ps.setBigDecimal(5, p.soTien());
            ps.setString(6, p.trangThai());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateStatus(long orderCode, String status) throws SQLException {
        String sql = """
                UPDATE PAYMENT
                   SET TRANGTHAI = ?,
                       NGAY_THANH_TOAN = CASE WHEN ? = 'ĐÃ THANH TOÁN' THEN SYSDATE ELSE NGAY_THANH_TOAN END
                 WHERE ORDER_CODE = ?
                """;
        try (Connection c = ConnectionUtils.getMyConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setLong(3, orderCode);
            ps.executeUpdate();
        }
    }
}
