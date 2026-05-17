package com.sportcourt.modules.payment.dto;


public record PaymentQrInfo(
        long   orderCode,
        String paymentLinkId,
        String qrCodeData,
        String accountName,
        String accountNumber,
        String bin,
        int    amount,
        String description
) {}
