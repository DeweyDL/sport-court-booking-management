package com.sportcourt.modules.cost.view;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Dữ liệu tạm (mock) cho module Bảng giá.
 * Khi BE hoàn thiện, thay thế bằng controller/service thật.
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
        private final List<CostItem> items = new ArrayList<>(createSampleData());
        private final List<AreaOption> areaOptions = createAreaOptions();
        private final List<KhungGioOption> khungGioOptions = createKhungGioOptions();

        List<AreaOption> getAreaOptions() {
            return new ArrayList<>(areaOptions);
        }

        List<KhungGioOption> getKhungGioOptions() {
            return new ArrayList<>(khungGioOptions);
        }

        List<CostItem> list(String keyword) {
            List<CostItem> all = new ArrayList<>(items);
            all.sort(Comparator.comparing(CostItem::maBg, Comparator.nullsLast(String::compareToIgnoreCase)));

            if (keyword == null || keyword.isBlank()) {
                return all;
            }

            String lower = keyword.trim().toLowerCase();
            return all.stream()
                    .filter(item -> (item.maBg() != null && item.maBg().toLowerCase().contains(lower))
                            || (item.maKv() != null && item.maKv().toLowerCase().contains(lower)))
                    .toList();
        }

        CostItem getDetail(String maBg) {
            if (maBg == null) {
                return null;
            }
            for (CostItem item : items) {
                if (maBg.equalsIgnoreCase(item.maBg())) {
                    return item;
                }
            }
            return null;
        }

        String generateNextMaBg() {
            int max = 0;
            for (CostItem item : items) {
                String id = item.maBg();
                if (id == null) {
                    continue;
                }
                String digits = id.replaceAll("\\D+", "");
                if (!digits.isBlank()) {
                    try {
                        max = Math.max(max, Integer.parseInt(digits));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return "BG%03d".formatted(max + 1);
        }

        void create(CostItem item) {
            if (item == null || item.maBg() == null || item.maBg().isBlank()) {
                throw new IllegalStateException("Mã bảng giá không hợp lệ.");
            }
            if (getDetail(item.maBg()) != null) {
                throw new IllegalStateException("Mã bảng giá đã tồn tại.");
            }
            items.add(item);
        }

        void update(CostItem updated) {
            if (updated == null || updated.maBg() == null || updated.maBg().isBlank()) {
                throw new IllegalStateException("Mã bảng giá không hợp lệ.");
            }
            for (int i = 0; i < items.size(); i++) {
                CostItem existing = items.get(i);
                if (updated.maBg().equalsIgnoreCase(existing.maBg())) {
                    // Giữ createdAt nếu đã có
                    LocalDateTime createdAt = existing.createdAt() == null ? updated.createdAt() : existing.createdAt();
                    items.set(i, new CostItem(
                            existing.maBg(),
                            updated.maKv(),
                            updated.maKg(),
                            updated.gioBatDau(),
                            updated.gioKetThuc(),
                            updated.gia(),
                            existing.deleted(),
                            createdAt
                    ));
                    return;
                }
            }
            throw new IllegalStateException("Không tìm thấy bảng giá để cập nhật.");
        }

        void delete(String maBg) {
            if (maBg == null || maBg.isBlank()) {
                throw new IllegalStateException("Mã bảng giá không hợp lệ.");
            }
            for (int i = 0; i < items.size(); i++) {
                CostItem existing = items.get(i);
                if (maBg.equalsIgnoreCase(existing.maBg())) {
                    if (existing.deleted()) {
                        return;
                    }
                    items.set(i, new CostItem(
                            existing.maBg(),
                            existing.maKv(),
                            existing.maKg(),
                            existing.gioBatDau(),
                            existing.gioKetThuc(),
                            existing.gia(),
                            true,
                            existing.createdAt()
                    ));
                    return;
                }
            }
            throw new IllegalStateException("Không tìm thấy bảng giá để xóa.");
        }
    }

    private static List<AreaOption> createAreaOptions() {
        List<AreaOption> opts = new ArrayList<>();
        opts.add(new AreaOption("KV-A", "Khu A"));
        opts.add(new AreaOption("KV-B", "Khu B"));
        opts.add(new AreaOption("KV-C", "Khu C"));
        opts.add(new AreaOption("KV-D", "Khu D"));
        opts.add(new AreaOption("KV-E", "Khu E"));
        opts.add(new AreaOption("KV-F", "Khu F"));
        opts.add(new AreaOption("KV-G", "Khu G"));
        return opts;
    }

    private static List<KhungGioOption> createKhungGioOptions() {
        List<KhungGioOption> opts = new ArrayList<>();
        for (int h = 0; h <= 23; h++) {
            opts.add(new KhungGioOption("KG%02d".formatted(h), h, Math.min(24, h + 1)));
        }
        return opts;
    }

    private static List<CostItem> createSampleData() {
        List<CostItem> list = new ArrayList<>();
        list.add(new CostItem("BG001", "KV001", "KG06", 6, 7, new BigDecimal("120000"), false, LocalDateTime.of(2025, 3, 10, 8, 30)));
        list.add(new CostItem("BG002", "KV001", "KG18", 18, 19, new BigDecimal("200000"), false, LocalDateTime.of(2025, 3, 12, 14, 0)));
        list.add(new CostItem("BG003", "KV002", "KG07", 7, 8, new BigDecimal("150000"), false, LocalDateTime.of(2025, 4, 1, 9, 15)));
        list.add(new CostItem("BG004", "KV002", "KG20", 20, 21, new BigDecimal("220000"), false, LocalDateTime.of(2025, 4, 5, 10, 0)));
        list.add(new CostItem("BG005", "KV003", "KG05", 5, 6, new BigDecimal("110000"), true, LocalDateTime.of(2025, 4, 8, 16, 45)));
        return list;
    }

    private static final Store STORE = new Store();

    static Store store() {
        return STORE;
    }
}

