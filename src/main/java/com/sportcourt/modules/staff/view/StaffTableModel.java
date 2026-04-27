package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffResponse;

import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StaffTableModel extends AbstractTableModel {
    private final String[] columns = {
            "MÃ NV",
            "HỌ TÊN",
            "SĐT",
            "EMAIL",
            "CHỨC VỤ",
            "NGÀY VÀO LÀM",
            "TRẠNG THÁI",
            "THAO TÁC"
    };

    private List<StaffResponse> data = new ArrayList<>();
    private JLabel footerLabel;

    public void setFooterLabel(JLabel footerLabel) {
        this.footerLabel = footerLabel;
    }

    public void setData(List<StaffResponse> data) {
        this.data = data == null ? new ArrayList<>() : data;
        fireTableDataChanged();
    }

    public StaffResponse getStaffAt(int row) {
        if (row < 0 || row >= data.size()) {
            return null;
        }

        return data.get(row);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        StaffResponse staff = data.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return safe(staff.getMaNv());
            case 1:
                return safe(staff.getHoTen());
            case 2:
                return safe(staff.getSdt());
            case 3:
                return safe(staff.getEmail());
            case 4:
                return normalizeRole(staff);
            case 5:
                if (staff.getNgayVaoLam() == null) {
                    return "--";
                }
                return staff.getNgayVaoLam().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case 6:
                return safe(staff.getTrangThai());
            case 7:
                return "";
            default:
                return "";
        }
    }

    private String normalizeRole(StaffResponse staff) {
        String value = staff.getViTri();

        if (value == null || value.trim().isEmpty()) {
            return staff.isQuanLy() ? "QUẢN LÝ" : "NHÂN VIÊN";
        }

        String lower = value.toLowerCase();

        if (lower.contains("quan") || lower.contains("quản")) {
            return "QUẢN LÝ";
        }

        if (lower.contains("nhan") || lower.contains("nhân")) {
            return "NHÂN VIÊN";
        }

        return value.trim().toUpperCase();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "--" : value;
    }
}
