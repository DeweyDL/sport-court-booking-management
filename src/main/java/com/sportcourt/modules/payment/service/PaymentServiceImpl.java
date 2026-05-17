package com.sportcourt.modules.payment.service;

import com.sportcourt.modules.payment.config.PayOSConfig;
import com.sportcourt.modules.payment.dao.JdbcPaymentDao;
import com.sportcourt.modules.payment.dao.PaymentDao;
import com.sportcourt.modules.payment.dto.PaymentQrInfo;
import com.sportcourt.modules.payment.entity.Payment;

import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

import java.math.BigDecimal;
import java.sql.SQLException;

public class PaymentServiceImpl implements PaymentService {

    private final PaymentDao dao;
    private final PayOS payOS;

    public PaymentServiceImpl() {
        this(new JdbcPaymentDao());
    }

    public PaymentServiceImpl(PaymentDao dao) {
        this.dao = dao;
        this.payOS = PayOSConfig.get();
    }

    @Override
    public PaymentQrInfo createDepositPayment(String maHd) {
        try {
            String invoiceStatus = dao.findInvoiceStatus(maHd);
            if (invoiceStatus == null) {
                throw new IllegalStateException("Khong tim thay hoa don " + maHd);
            }
            if (!"CHƯA THANH TOÁN".equals(invoiceStatus)) {
                throw new IllegalStateException("Hoa don khong o trang thai CHUA THANH TOAN");
            }

            BigDecimal coc = dao.findDeposit(maHd);
            if (coc == null || coc.signum() <= 0) {
                throw new IllegalStateException("Hoa don nay khong co tien coc de thu");
            }
            int amount = coc.intValue();

            long orderCode = System.currentTimeMillis() / 1000;

            ItemData item = ItemData.builder()
                    .name("Coc " + maHd)
                    .quantity(1)
                    .price(amount)
                    .build();

            PaymentData data = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description("Coc " + maHd)
                    .returnUrl("https://localhost/return")
                    .cancelUrl("https://localhost/cancel")
                    .item(item)
                    .expiredAt(System.currentTimeMillis() / 1000 + 15 * 60)
                    .build();

            CheckoutResponseData res = payOS.createPaymentLink(data);

            dao.insert(new Payment(
                    "TT" + orderCode,
                    maHd,
                    res.getOrderCode(),
                    res.getPaymentLinkId(),
                    BigDecimal.valueOf(res.getAmount().longValue()),
                    "CHỜ THANH TOÁN"));

            return new PaymentQrInfo(
                    res.getOrderCode(),
                    res.getPaymentLinkId(),
                    res.getQrCode(),
                    res.getAccountName(),
                    res.getAccountNumber(),
                    res.getBin(),
                    res.getAmount(),
                    res.getDescription());

        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Loi DB khi tao thanh toan: " + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong tao duoc thanh toan PayOS: " + e.getMessage(), e);
        }
    }

    @Override
    public String getStatus(long orderCode) {
        try {
            PaymentLinkData info = payOS.getPaymentLinkInformation(orderCode);
            String status = mapStatus(info.getStatus());
            dao.updateStatus(orderCode, status);
            return status;
        } catch (Exception e) {
            e.printStackTrace();
            return "CHỜ THANH TOÁN";
        }
    }

    @Override
    public void cancel(long orderCode) {
        try {
            payOS.cancelPaymentLink(orderCode, "Nguoi dung huy");
            dao.updateStatus(orderCode, "ĐÃ HUỶ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public PaymentQrInfo createPaymentLink(int amount, String description) {
        try {
            long orderCode = System.currentTimeMillis() / 1000;

            ItemData item = ItemData.builder()
                    .name(description)
                    .quantity(1)
                    .price(amount)
                    .build();

            PaymentData data = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .returnUrl("https://localhost/return")
                    .cancelUrl("https://localhost/cancel")
                    .item(item)
                    .expiredAt(System.currentTimeMillis() / 1000 + 15 * 60)
                    .build();

            CheckoutResponseData res = payOS.createPaymentLink(data);

            return new PaymentQrInfo(
                    res.getOrderCode(),
                    res.getPaymentLinkId(),
                    res.getQrCode(),
                    res.getAccountName(),
                    res.getAccountNumber(),
                    res.getBin(),
                    res.getAmount(),
                    res.getDescription());

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Khong tao duoc thanh toan PayOS: " + e.getMessage(), e);
        }
    }

    @Override
    public String checkStatus(long orderCode) {
        try {
            return mapStatus(payOS.getPaymentLinkInformation(orderCode).getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            return "CHỜ THANH TOÁN";
        }
    }

    private static String mapStatus(String payosStatus) {
        if (payosStatus == null) {
            return "CHỜ THANH TOÁN";
        }
        return switch (payosStatus) {
            case "PAID" -> "ĐÃ THANH TOÁN";
            case "CANCELLED" -> "ĐÃ HUỶ";
            case "EXPIRED" -> "HẾT HẠN";
            default -> "CHỜ THANH TOÁN";
        };
    }
}
