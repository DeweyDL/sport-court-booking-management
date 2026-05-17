package com.sportcourt.modules.payment.service;

import com.sportcourt.modules.payment.dto.PaymentQrInfo;

public interface PaymentService {

    PaymentQrInfo createDepositPayment(String maHd);

    String getStatus(long orderCode);

    void cancel(long orderCode);

    PaymentQrInfo createPaymentLink(int amount, String description);

    String checkStatus(long orderCode);
}
