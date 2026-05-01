package com.sportcourt.modules.imports.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu tạm (mock) cho module nhập hàng.
 * Khi BE hoàn thiện, thay thế bằng controller/service thật.
 */
final class ImportMockData {

    private ImportMockData() {
    }

    // --- NHAP_HANG ---
    record ImportItem(
            String manh,
            String tenNcc,
            String tenNv,
            String maChungTu,
            BigDecimal triGia,
            LocalDateTime createdAt
    ) {
    }

    // --- CHI_TIET_NHAP_HANG (sản phẩm) ---
    record ImportProductDetail(
            String maCt,
            String manh,
            String maSp,
            String tenSp,
            int slChungTu,
            int slThucNhap,
            BigDecimal donGia,
            BigDecimal vat
    ) {
    }

    // --- CHI_TIET_NHAP_DUNG_CU (dụng cụ) ---
    record ImportEquipmentDetail(
            String maCt,
            String manh,
            String maDc,
            String tenDc,
            int slChungTu,
            int slThucNhap,
            BigDecimal donGia,
            BigDecimal cktm
    ) {
    }

    // --------- SAMPLE DATA ---------

    static List<ImportItem> createSampleImports() {
        List<ImportItem> items = new ArrayList<>();
        items.add(new ImportItem("NH001", "Công ty TNHH Yonex Việt Nam", "Nguyễn Văn An", "CT-2025-001",
                new BigDecimal("15200000"), LocalDateTime.of(2025, 3, 10, 8, 30)));
        items.add(new ImportItem("NH002", "Công ty CP Mikasa", "Trần Thị Bình", "CT-2025-002",
                new BigDecimal("8500000"), LocalDateTime.of(2025, 3, 15, 10, 0)));
        items.add(new ImportItem("NH003", "NCC Adidas Việt Nam", "Lê Minh Châu", "CT-2025-003",
                new BigDecimal("22000000"), LocalDateTime.of(2025, 4, 1, 14, 15)));
        items.add(new ImportItem("NH004", "Công ty Spalding Sports", "Phạm Đức Duy", "CT-2025-004",
                new BigDecimal("6750000"), LocalDateTime.of(2025, 4, 12, 9, 0)));
        items.add(new ImportItem("NH005", "NCC Wilson Sporting Goods", "Hoàng Thị Eo", "CT-2025-005",
                new BigDecimal("31500000"), LocalDateTime.of(2025, 5, 5, 11, 30)));
        items.add(new ImportItem("NH006", "Công ty TNHH Butterfly VN", "Vũ Quang Phúc", "CT-2025-006",
                new BigDecimal("42000000"), LocalDateTime.of(2025, 5, 20, 13, 45)));
        items.add(new ImportItem("NH007", "NCC Speedo International", "Đỗ Thanh Giang", "CT-2025-007",
                new BigDecimal("9800000"), LocalDateTime.of(2025, 6, 8, 8, 0)));
        items.add(new ImportItem("NH008", "Công ty CP Nike Việt Nam", "Bùi Thị Hạnh", "CT-2025-008",
                new BigDecimal("18600000"), LocalDateTime.of(2025, 6, 25, 15, 30)));
        return items;
    }

    static List<ImportProductDetail> createSampleProductDetails() {
        List<ImportProductDetail> items = new ArrayList<>();
        items.add(new ImportProductDetail("CTSP001", "NH001", "SP001", "Nước uống ion Pocari", 100, 98,
                new BigDecimal("12000"), new BigDecimal("10")));
        items.add(new ImportProductDetail("CTSP002", "NH001", "SP002", "Khăn lạnh thể thao", 50, 50,
                new BigDecimal("25000"), new BigDecimal("10")));
        items.add(new ImportProductDetail("CTSP003", "NH002", "SP003", "Băng dán thể thao", 200, 195,
                new BigDecimal("8000"), new BigDecimal("8")));
        items.add(new ImportProductDetail("CTSP004", "NH003", "SP004", "Bình nước thể thao 750ml", 80, 80,
                new BigDecimal("45000"), new BigDecimal("10")));
        items.add(new ImportProductDetail("CTSP005", "NH005", "SP005", "Grip vợt tennis", 150, 148,
                new BigDecimal("15000"), new BigDecimal("5")));
        return items;
    }

    static List<ImportEquipmentDetail> createSampleEquipmentDetails() {
        List<ImportEquipmentDetail> items = new ArrayList<>();
        items.add(new ImportEquipmentDetail("CTDC001", "NH001", "DC001", "Vợt cầu lông Yonex", 30, 28,
                new BigDecimal("850000"), new BigDecimal("5")));
        items.add(new ImportEquipmentDetail("CTDC002", "NH002", "DC002", "Quả bóng đá Mikasa", 20, 20,
                new BigDecimal("420000"), new BigDecimal("3")));
        items.add(new ImportEquipmentDetail("CTDC003", "NH003", "DC004", "Giày thể thao Adidas", 40, 38,
                new BigDecimal("1500000"), new BigDecimal("8")));
        items.add(new ImportEquipmentDetail("CTDC004", "NH004", "DC005", "Bóng rổ Spalding", 15, 15,
                new BigDecimal("750000"), new BigDecimal("4")));
        items.add(new ImportEquipmentDetail("CTDC005", "NH005", "DC007", "Vợt tennis Wilson", 10, 10,
                new BigDecimal("2200000"), new BigDecimal("6")));
        items.add(new ImportEquipmentDetail("CTDC006", "NH006", "DC009", "Bàn bóng bàn Butterfly", 5, 5,
                new BigDecimal("8500000"), new BigDecimal("10")));
        return items;
    }
}
