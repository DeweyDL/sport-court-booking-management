package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffResponse;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StaffTableModel extends AbstractTableModel {
    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_PHONE = 2;
    public static final int COL_EMAIL = 3;
    public static final int COL_ROLE = 4;
    public static final int COL_START_DATE = 5;
    public static final int COL_STATUS = 6;
    public static final int COL_ACTION = 7;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] COLUMNS = {
            "MÃ NV", "HỌ TÊN", "SĐT", "EMAIL", "CHỨC VỤ", "NGÀY VÀO LÀM", "TRẠNG THÁI", "THAO TÁC"
    };

    private final List<StaffResponse> rows = new ArrayList<>();

    public void setRows(List<StaffResponse> staffRows) {
        rows.clear();
        if (staffRows != null) {
            rows.addAll(staffRows);
        }
        fireTableDataChanged();
    }

    public List<StaffResponse> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public StaffResponse getRow(int modelRow) {
        if (modelRow < 0 || modelRow >= rows.size()) {
            return null;
        }
        return rows.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == COL_ACTION;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        StaffResponse staff = getRow(rowIndex);
        if (staff == null) {
            return "";
        }

        return switch (columnIndex) {
            case COL_ID -> staff.getMaNv();
            case COL_NAME -> value(staff.getHoTen());
            case COL_PHONE -> value(staff.getSdt());
            case COL_EMAIL -> shorten(staff.getEmail(), 26);
            case COL_ROLE -> staff.isQuanLy() ? "QUẢN LÝ" : "NHÂN VIÊN";
            case COL_START_DATE -> formatDate(staff.getNgayVaoLam());
            case COL_STATUS -> value(staff.getTrangThai(), "HOẠT ĐỘNG");
            case COL_ACTION -> "Xóa    Chỉnh sửa";
            default -> "";
        };
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMATTER.format(date);
    }

    private String value(String value) {
        return value(value, "");
    }

    private String value(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
    }

    private String shorten(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
