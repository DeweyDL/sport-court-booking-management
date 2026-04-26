package com.sportcourt.modules.staff.view;
import com.sportcourt.modules.staff.dto.StaffResponse;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class StaffTableModel extends AbstractTableModel {
    private final String[] columns = {
            "Mã NV", "Họ tên", "SĐT", "Email", "Vị trí", "Chi nhánh", "Ngày vào làm", "CCCD", "Quản lý"
    };

    private List<StaffResponse> data = new ArrayList<>();

    public void setData(List<StaffResponse> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public StaffResponse getStaffAt(int row) {
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

        return switch (columnIndex) {
            case 0 -> staff.getMaNv();
            case 1 -> staff.getHoTen();
            case 2 -> staff.getSdt();
            case 3 -> staff.getEmail();
            case 4 -> staff.getViTri();
            case 5 -> staff.getDiaChiChiNhanh();
            case 6 -> staff.getNgayVaoLam();
            case 7 -> staff.getCccd();
            case 8 -> staff.isQuanLy() ? "Có" : "Không";
            default -> "";
        };
    }
}