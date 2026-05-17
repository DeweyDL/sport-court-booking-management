package com.sportcourt.modules.payment.entity;

import java.math.BigDecimal;


public record Payment(
        String     maTt,
        String     maHd,
        long       orderCode,
        String     paymentLinkId,
        BigDecimal soTien,
        String     trangThai
) {}
