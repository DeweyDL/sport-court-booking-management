package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.dto.ProductResponse;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProductTableModel extends AbstractTableModel {
    public static final int COL_ID     = 0;
    public static final int COL_NAME   = 1;
    public static final int COL_DVT    = 2;
    public static final int COL_PRICE  = 3;
    public static final int COL_STOCK  = 4;
    public static final int COL_ACTION = 5;

    private static final String[] COLUMNS = {
            "MÃ SP", "TÊN SẢN PHẨM", "ĐƠN VỊ TÍNH", "ĐƠN GIÁ", "SỐ LƯỢNG TỒN", "THAO TÁC"
    };

    private final DecimalFormat moneyFormat;
    private final List<ProductResponse> rows = new ArrayList<ProductResponse>();

    public ProductTableModel() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        moneyFormat = new DecimalFormat("#,###", symbols);
    }

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
            case COL_ID:     return safe(product.getMaSp());
            case COL_NAME:   return safe(product.getTenSp());
            case COL_DVT:    return safe(product.getDvt());
            case COL_PRICE:  return formatMoney(product.getGia());
            case COL_STOCK:  return product.getSlTon() == null ? 0 : product.getSlTon();
            case COL_ACTION: return product.isDeleted() ? "Khôi phục    Chỉnh sửa" : "Xóa    Chỉnh sửa";
            default:         return "";
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