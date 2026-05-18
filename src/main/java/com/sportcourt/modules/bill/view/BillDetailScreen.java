package com.sportcourt.modules.bill.view;

import com.sportcourt.modules.branch.entity.Branch;

/**
 * Màn xem chi tiết hóa đơn — giống BillEditScreen nhưng CHỈ XEM:
 * không có nút "Thêm sân/dụng cụ/sản phẩm" và không có nút thanh toán.
 * Dùng cho hóa đơn đã thanh toán / đã huỷ (không cho tác động lên hóa đơn).
 */
public class BillDetailScreen extends BillEditScreen {

    public BillDetailScreen(String maHD, Branch branch, Runnable onBack) {
        super(maHD, branch, onBack, null, null, null, null, true);
    }
}
