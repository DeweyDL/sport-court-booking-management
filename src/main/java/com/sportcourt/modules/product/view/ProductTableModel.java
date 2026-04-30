package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.dto.ProductResponse;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductTableModel extends AbstractTableModel {
    public static final int COL_ID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_CATEGORY = 2;
    public static final int COL_PRICE = 3;
    public static final int COL_QUANTITY = 4;
    public static final int COL_STATUS = 5;
    public static final int COL_ACTION = 6;

    private static final String[] COLUMNS = {
            "MÃ SP", "TÊN SẢN PHẨM", "DANH MỤC", "ĐƠN GIÁ", "SỐ LƯỢNG", "TRẠNG THÁI", "THAO TÁC"
    };

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    private final List<ProductResponse> rows = new ArrayList<>();

    public void setRows(List<ProductResponse> products) {
        rows.clear();
        if (products != null) {
            rows.addAll(products);
        }
        fireTableDataChanged();
    }

    public ProductResponse getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return null;
        }
        return rows.get(rowIndex);
    }

    public List<ProductResponse> getRows() {
        return Collections.unmodifiableList(rows);
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
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProductResponse product = getRow(rowIndex);
        if (product == null) {
            return "";
        }

        switch (columnIndex) {
            case COL_ID:
                return safe(product.getMaSp());
            case COL_NAME:
                return safe(product.getTenSp());
            case COL_CATEGORY:
                return safe(product.getDanhMuc());
            case COL_PRICE:
                return formatMoney(product.getGia());
            case COL_QUANTITY:
                return product.getSoLuongTon() == null ? 0 : product.getSoLuongTon();
            case COL_STATUS:
                return safe(product.getTrangThai());
            case COL_ACTION:
                return product.isDeleted() ? "restore-edit" : "delete-edit";
            default:
                return "";
        }
    }

    private String formatMoney(BigDecimal value) {
        BigDecimal safeValue = value == null ? BigDecimal.ZERO : value;
        return moneyFormat.format(safeValue) + " VNĐ";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
