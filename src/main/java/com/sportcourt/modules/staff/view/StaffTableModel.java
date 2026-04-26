package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class StaffTableModel extends AbstractTableModel {
    private final String[] columns = {
            "Mã NV", "Họ tên", "SĐT", "Email", "Vị trí", "Chi nhánh", "Ngày vào làm", "CCCD", "Quản lý"
    };

    private List<StaffResponse> data = new ArrayList<>();

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
                return staff.getMaNv();
            case 1:
                return staff.getHoTen();
            case 2:
                return staff.getSdt();
            case 3:
                return staff.getEmail();
            case 4:
                return staff.getViTri();
            case 5:
                return staff.getDiaChiChiNhanh();
            case 6:
                return staff.getNgayVaoLam();
            case 7:
                return staff.getCccd();
            case 8:
                return staff.isQuanLy() ? "Có" : "Không";
            default:
                return "";
        }
    }
}
