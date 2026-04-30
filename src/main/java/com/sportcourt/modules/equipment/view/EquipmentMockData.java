package com.sportcourt.modules.equipment.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu tạm (mock) cho module dụng cụ thể thao.
 * Khi BE hoàn thiện, thay thế bằng controller/service thật.
 */
final class EquipmentMockData {

    private EquipmentMockData() {
    }

    record EquipmentItem(
            String maDc,
            String tenDc,
            String dvt,
            BigDecimal gia,
            int slTon,
            LocalDateTime createdAt
    ) {
    }

    static List<EquipmentItem> createSampleData() {
        List<EquipmentItem> items = new ArrayList<>();
        items.add(new EquipmentItem("DC001", "Vợt cầu lông Yonex", "Cây", new BigDecimal("850000"), 24, LocalDateTime.of(2025, 3, 10, 8, 30)));
        items.add(new EquipmentItem("DC002", "Quả bóng đá Mikasa", "Quả", new BigDecimal("420000"), 15, LocalDateTime.of(2025, 3, 12, 14, 0)));
        items.add(new EquipmentItem("DC003", "Lưới cầu lông tiêu chuẩn", "Bộ", new BigDecimal("1200000"), 6, LocalDateTime.of(2025, 4, 1, 9, 15)));
        items.add(new EquipmentItem("DC004", "Giày thể thao Adidas", "Đôi", new BigDecimal("1500000"), 30, LocalDateTime.of(2025, 4, 5, 10, 0)));
        items.add(new EquipmentItem("DC005", "Bóng rổ Spalding", "Quả", new BigDecimal("750000"), 12, LocalDateTime.of(2025, 4, 8, 16, 45)));
        items.add(new EquipmentItem("DC006", "Băng quấn cổ tay", "Cái", new BigDecimal("35000"), 100, LocalDateTime.of(2025, 5, 2, 11, 30)));
        items.add(new EquipmentItem("DC007", "Vợt tennis Wilson", "Cây", new BigDecimal("2200000"), 8, LocalDateTime.of(2025, 5, 15, 13, 20)));
        items.add(new EquipmentItem("DC008", "Quả cầu lông (hộp 12)", "Hộp", new BigDecimal("180000"), 50, LocalDateTime.of(2025, 6, 1, 7, 0)));
        items.add(new EquipmentItem("DC009", "Bàn bóng bàn Butterfly", "Bộ", new BigDecimal("8500000"), 3, LocalDateTime.of(2025, 6, 20, 15, 10)));
        items.add(new EquipmentItem("DC010", "Kính bơi Speedo", "Cái", new BigDecimal("320000"), 40, LocalDateTime.of(2025, 7, 3, 9, 0)));
        return items;
    }
}
