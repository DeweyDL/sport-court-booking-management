package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffResponse;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TableModel dự phòng (không dùng trực tiếp trong StaffPanel custom-row layout).
 * Giữ lại để tương thích nếu có nơi khác tham chiếu.
 */
public class StaffTableModel extends AbstractTableModel {

    public static final int COL_ID         = 0;
    public static final int COL_NAME       = 1;
    public static final int COL_CCCD       = 2;
    public static final int COL_START_DATE = 3;
    public static final int COL_ROLE       = 4;
    public static final int COL_STATUS     = 5;
    public static final int COL_ACTION     = 6;

    private static final String[] COLUMNS = {
            "MÃ NV", "HỌ TÊN", "CĂN CƯỚC CD", "NGÀY VÀO LÀM", "CHỨC VỤ", "TRẠNG THÁI", "THAO TÁC"
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
        if (modelRow < 0 || modelRow >= rows.size()) return null;
        return rows.get(modelRow);
    }

    @Override public int getRowCount()    { return rows.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int column) { return COLUMNS[column]; }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        StaffResponse staff = getRow(rowIndex);
        if (staff == null) return "";

        return switch (columnIndex) {
            case COL_ID         -> nvl(staff.getManv());
            case COL_NAME       -> nvl(staff.getHoten());
            case COL_CCCD       -> nvl(staff.getCccd());
            case COL_START_DATE -> staff.getNgayVaoLamFormatted();
            case COL_ROLE       -> staff.getIsQl() == 1 ? "Quản lý" : "Nhân viên";
            case COL_STATUS     -> nvl(staff.getTrangThai());
            case COL_ACTION     -> "Xóa | Chỉnh sửa";
            default             -> "";
        };
    }

    private String nvl(String value) {
        return (value == null || value.isBlank()) ? "--" : value.trim();
    }
}