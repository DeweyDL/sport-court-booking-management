package com.sportcourt.modules.cost.view;

import com.sportcourt.modules.cost.controller.CostController;
import com.sportcourt.modules.cost.entity.Cost;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Adapter kết nối giữa View cũ và DB Backend thật.
 * View gọi class này, class này gọi Database thông qua Controller.
 */
final class CostMockData {

    private CostMockData() {
    }

    record AreaOption(String maKv, String tenKv) {
        @Override
        public String toString() {
            if (tenKv == null || tenKv.isBlank()) {
                return Objects.toString(maKv, "");
            }
            return maKv + " - " + tenKv;
        }
    }

    record KhungGioOption(String maKg, int gioBatDau, int gioKetThuc) {
        @Override
        public String toString() {
            return "%02d:00 - %02d:00".formatted(gioBatDau, gioKetThuc);
        }
    }

    record CostItem(
            String maBg,
            String maKv,
            String maKg,
            int gioBatDau,
            int gioKetThuc,
            BigDecimal gia,
            boolean deleted,
            LocalDateTime createdAt
    ) {
        boolean isDeleted() {
            return deleted;
        }

        String getStatus() {
            return deleted ? "Đã xóa" : "Đang áp dụng";
        }
    }

    static final class Store {
        private final CostController controller = new CostController();

        List<AreaOption> getAreaOptions() {
            List<AreaOption> opts = new ArrayList<>();
            Map<String, String> areas = controller.getKhuVucOptions();
            for (Map.Entry<String, String> entry : areas.entrySet()) {
                opts.add(new AreaOption(entry.getKey(), entry.getValue()));
            }
            return opts;
        }

        List<KhungGioOption> getKhungGioOptions() {
            List<KhungGioOption> opts = new ArrayList<>();
            for (int h = 0; h <= 23; h++) {
                opts.add(new KhungGioOption("KG%02d".formatted(h), h, Math.min(24, h + 1)));
            }
            return opts;
        }

        List<CostItem> list(String keyword) {
            List<CostItem> items = new ArrayList<>();
            for (Cost cost : controller.searchCosts(keyword)) {
                items.add(new CostItem(
                        cost.getMaBg(), cost.getMaKv(), null,
                        cost.getGioBatDau(), cost.getGioKetThuc(),
                        cost.getGia(), cost.isDeleted(), cost.getCreatedAt()
                ));
            }
            return items;
        }

        CostItem getDetail(String maBg) {
            Cost cost = controller.getCostDetail(maBg);
            if (cost == null) return null;
            return new CostItem(
                    cost.getMaBg(), cost.getMaKv(), null,
                    cost.getGioBatDau(), cost.getGioKetThuc(),
                    cost.getGia(), cost.isDeleted(), cost.getCreatedAt()
            );
        }

        String generateNextMaBg() {
            return controller.generateNextMaBg();
        }

        void create(CostItem item) {
            controller.createCost(new Cost(
                    item.maBg(), item.maKv(), item.gioBatDau(), item.gioKetThuc(), item.gia(), false, LocalDateTime.now()
            ));
        }

        void update(CostItem updated) {
            controller.updateCost(new Cost(
                    updated.maBg(), updated.maKv(), updated.gioBatDau(), updated.gioKetThuc(), updated.gia(), false, null
            ));
        }

        void delete(String maBg) {
            controller.deleteCost(maBg);
        }
    }

    private static final Store STORE = new Store();

    static Store store() {
        return STORE;
    }
}
