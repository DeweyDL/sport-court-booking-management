package com.sportcourt.modules.customer_rank.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class CustomerRankMockData {

    private CustomerRankMockData() {
    }
    record CustomerRankItem(
            String maHang,
            String tenHang,
            BigDecimal chietKhau,
            BigDecimal mucTien
    ) {
    }

    static List<CustomerRankItem> createSampleData() {
        List<CustomerRankItem> items = new ArrayList<>();
        items.add(new CustomerRankItem("HANG001", "Đồng", new BigDecimal("0"), new BigDecimal("0")));
        items.add(new CustomerRankItem("HANG002", "Bạc", new BigDecimal("5"), new BigDecimal("5000000")));
        items.add(new CustomerRankItem("HANG003", "Vàng", new BigDecimal("10"), new BigDecimal("15000000")));
        items.add(new CustomerRankItem("HANG004", "Kim cương", new BigDecimal("15"), new BigDecimal("50000000")));
        items.add(new CustomerRankItem("HANG005", "VIP", new BigDecimal("20"), new BigDecimal("100000000")));
        return items;
    }
}