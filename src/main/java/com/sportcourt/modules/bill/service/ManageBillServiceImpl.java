package com.sportcourt.modules.bill.service;

import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.bill.dao.JdbcManageBillDao;
import com.sportcourt.modules.bill.dao.ManageBillDao;
import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.BillResult;
import com.sportcourt.modules.bill.dto.BillSummary;
import com.sportcourt.modules.bill.dto.ServiceItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ManageBillServiceImpl implements ManageBillService {
    private static final String STATUS_PAID = "ĐÃ THANH TOÁN";
    private static final String STATUS_CANCELLED = "ĐÃ HUỶ";
    private static final String STATUS_UNPAID = "CHƯA THANH TOÁN";

    private final ManageBillDao dao;

    public ManageBillServiceImpl() {
        this(new JdbcManageBillDao());
    }

    public ManageBillServiceImpl(ManageBillDao dao) {
        this.dao = dao;
    }

    @Override
    public BillResult<List<BillSummary>> searchBills(String keyword) {
        try {
            UserSession session = SessionManager.requireSession();
            // Owner (chủ sân) thấy tất cả chi nhánh; nhân viên chỉ thấy chi nhánh của mình
            String branchId = session.isOwner() ? null : session.getBranchId();
            return BillResult.ok("Lấy danh sách hóa đơn thành công.", dao.findAll(keyword, branchId));
        } catch (SQLException e) {
            return BillResult.fail("Không thể tải danh sách hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public BillResult<BillDetail> getDetail(String maHD) {
        if (isBlank(maHD)) {
            return BillResult.fail("Thiếu mã hóa đơn.");
        }
        try {
            Optional<BillDetail> detail = dao.findDetailById(maHD.trim());
            if (detail.isEmpty()) {
                return BillResult.fail("Không tìm thấy hóa đơn.");
            }
            return BillResult.ok("Lấy chi tiết hóa đơn thành công.", detail.get());
        } catch (SQLException e) {
            return BillResult.fail("Không thể lấy chi tiết hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public BillResult<Void> markAsPaid(String maHD) {
        if (isBlank(maHD)) {
            return BillResult.fail("Thiếu mã hóa đơn.");
        }
        try {
            // SQL tự kiểm tra TRANGTHAI = 'CHƯA THANH TOÁN' bằng TRIM → tránh mismatch encoding
            boolean updated = dao.updateStatus(maHD.trim(), STATUS_PAID, STATUS_UNPAID);
            if (!updated) {
                return BillResult.fail("Không thể thanh toán: hóa đơn không tồn tại hoặc không ở trạng thái chưa thanh toán.");
            }
            return BillResult.ok("Hóa đơn đã được thanh toán.", null);
        } catch (SQLException e) {
            return BillResult.fail("Lỗi khi thanh toán hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public BillResult<Void> cancelBill(String maHD) {
        if (isBlank(maHD)) {
            return BillResult.fail("Thiếu mã hóa đơn.");
        }
        try {
            // SQL tự kiểm tra TRANGTHAI = 'CHƯA THANH TOÁN' bằng TRIM → chỉ huỷ khi chưa thanh toán
            boolean updated = dao.updateStatus(maHD.trim(), STATUS_CANCELLED, STATUS_UNPAID);
            if (!updated) {
                return BillResult.fail("Không thể huỷ: hóa đơn không tồn tại hoặc đã thanh toán/đã huỷ.");
            }
            return BillResult.ok("Đã huỷ hóa đơn.", null);
        } catch (SQLException e) {
            return BillResult.fail("Lỗi khi huỷ hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public BillResult<Void> softDelete(String maHD) {
        if (isBlank(maHD)) {
            return BillResult.fail("Thiếu mã hóa đơn.");
        }
        try {
            boolean deleted = dao.softDelete(maHD.trim());
            if (!deleted) {
                return BillResult.fail("Không tìm thấy hóa đơn để xóa.");
            }
            return BillResult.ok("Đã xóa hóa đơn.", null);
        } catch (SQLException e) {
            return BillResult.fail("Lỗi khi xóa hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public BillResult<String> createEmptyBill(String maKH, String maNV) {
        try {
            String maHD = dao.createEmptyBill(maKH, maNV);
            return BillResult.ok(null, maHD);
        } catch (SQLException e) {
            return BillResult.fail("Không thể tạo hóa đơn: " + e.getMessage());
        }
    }

    @Override
    public BillResult<Void> addServiceItems(String maHD, List<ServiceItem> items) {
        if (isBlank(maHD)) {
            return BillResult.fail("Thiếu mã hóa đơn.");
        }
        if (items == null || items.isEmpty()) {
            return BillResult.ok("Không có mục nào để thêm.", null);
        }
        try {
            dao.addServiceItems(maHD.trim(), items);
            return BillResult.ok("Đã thêm vào hóa đơn.", null);
        } catch (SQLException e) {
            return BillResult.fail("Không thể thêm dịch vụ: " + e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
