package com.sportcourt.modules.staff.service;
import com.sportcourt.modules.staff.dao.*;
import com.sportcourt.modules.staff.dto.*;
import com.sportcourt.modules.staff.entity.*;
import java.util.List;

public class StaffValidator {

    public void validateCreate(StaffCreateRequest request) {
        validateCommon(
                request.getHoTen(),
                request.getSdt(),
                request.getEmail(),
                request.getMaCn(),
                request.getMaLoaiNv(),
                request.getCccd()
        );

        if (request.isCreateAccount()) {
            if (isBlank(request.getUsername())) {
                throw new RuntimeException("Tên đăng nhập không được để trống.");
            }

            if (isBlank(request.getPassword())) {
                throw new RuntimeException("Mật khẩu không được để trống.");
            }

            if (isBlank(request.getRoleGroupId())) {
                throw new RuntimeException("Nhóm quyền không được để trống.");
            }
        }
    }

    public void validateUpdate(StaffUpdateRequest request) {
        if (isBlank(request.getMaNv())) {
            throw new RuntimeException("Vui lòng chọn nhân viên cần cập nhật.");
        }

        validateCommon(
                request.getHoTen(),
                request.getSdt(),
                request.getEmail(),
                request.getMaCn(),
                request.getMaLoaiNv(),
                request.getCccd()
        );
    }

    private void validateCommon(String hoTen, String sdt, String email,
                                String maCn, String maLoaiNv, String cccd) {
        if (isBlank(hoTen)) {
            throw new RuntimeException("Họ tên không được để trống.");
        }

        if (isBlank(sdt) || !sdt.matches("^0\\d{9}$")) {
            throw new RuntimeException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
        }

        if (isBlank(email) || !email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            throw new RuntimeException("Email phải đúng định dạng Gmail.");
        }

        if (isBlank(maCn)) {
            throw new RuntimeException("Chi nhánh không được để trống.");
        }

        if (isBlank(maLoaiNv)) {
            throw new RuntimeException("Vị trí nhân viên không được để trống.");
        }

        if (isBlank(cccd)) {
            throw new RuntimeException("CCCD không được để trống.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}