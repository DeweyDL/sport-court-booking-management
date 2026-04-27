package com.sportcourt.modules.staff.service;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import java.time.LocalDate;

public class StaffValidator {

    public void validateCreate(StaffCreateRequest request) {
        if (request == null) {
            throw new RuntimeException("Dữ liệu nhân viên không được để trống.");
        }

        validateCommon(
                request.getHoTen(),
                request.getNgaySinh(),
                request.getSdt(),
                request.getEmail(),
                request.getNgayVaoLam(),
                request.getCccd()
        );
    }

    public void validateUpdate(StaffUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("Dữ liệu cập nhật nhân viên không được để trống.");
        }

        if (isBlank(request.getMaNv())) {
            throw new RuntimeException("Vui lòng chọn nhân viên cần cập nhật.");
        }

        if (isBlank(request.getUserId())) {
            throw new RuntimeException("Không tìm thấy mã người dùng của nhân viên.");
        }

        validateCommon(
                request.getHoTen(),
                request.getNgaySinh(),
                request.getSdt(),
                request.getEmail(),
                request.getNgayVaoLam(),
                request.getCccd()
        );
    }

    private void validateCommon(String hoTen,
                                LocalDate ngaySinh,
                                String sdt,
                                String email,
                                LocalDate ngayVaoLam,
                                String cccd) {
        if (isBlank(hoTen)) {
            throw new RuntimeException("Họ và tên nhân viên không được để trống.");
        }

        if (isBlank(sdt)) {
            throw new RuntimeException("Số điện thoại không được để trống.");
        }

        if (!sdt.matches("^0\\d{9}$")) {
            throw new RuntimeException("Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.");
        }

        if (isBlank(cccd)) {
            throw new RuntimeException("Căn cước công dân không được để trống.");
        }

        if (!cccd.matches("\\d{12}")) {
            throw new RuntimeException("Căn cước công dân phải gồm đúng 12 chữ số.");
        }

        if (isBlank(email)) {
            throw new RuntimeException("Email không được để trống.");
        }

        if (!email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            throw new RuntimeException("Email phải đúng định dạng Gmail.");
        }

        if (ngaySinh == null) {
            throw new RuntimeException("Ngày sinh không được để trống.");
        }

        if (ngaySinh.isAfter(LocalDate.now())) {
            throw new RuntimeException("Ngày sinh không hợp lệ.");
        }

        if (ngayVaoLam == null) {
            throw new RuntimeException("Ngày vào làm không được để trống.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}