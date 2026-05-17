package com.sportcourt.modules.payment.dao;

import com.sportcourt.modules.payment.entity.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;

public interface PaymentDao {

    BigDecimal findDeposit(String maHd) throws SQLException;
    String findInvoiceStatus(String maHd) throws SQLException;

    void insert(Payment payment) throws SQLException;

    void updateStatus(long orderCode, String status) throws SQLException;
}
