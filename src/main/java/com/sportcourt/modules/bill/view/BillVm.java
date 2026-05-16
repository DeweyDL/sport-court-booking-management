package com.sportcourt.modules.bill.view;

import com.sportcourt.modules.bill.dto.BillSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillVm {
    private String maHD;
    private String maKH;
    private String tenKhachHang;
    private String maNV;
    private String tenNhanVien;
    private BigDecimal tienCoc;
    private BigDecimal giamGia;
    private BigDecimal tongGiaTri;
    private String trangThai;
    private BigDecimal tongTien;
    private LocalDateTime createdAt;

    private BillVm() {}

    public static BillVm fromSummary(BillSummary summary) {
        BillVm vm = new BillVm();
        vm.maHD = summary.maHD();
        vm.maKH = summary.maKH();
        vm.tenKhachHang = summary.tenKhachHang();
        vm.maNV = summary.maNV();
        vm.tenNhanVien = summary.tenNhanVien();
        vm.tienCoc = summary.tienCoc();
        vm.giamGia = summary.giamGia();
        vm.tongGiaTri = summary.tongGiaTri();
        vm.trangThai = summary.trangThai();
        vm.tongTien = summary.tongTien();
        vm.createdAt = summary.createdAt();
        return vm;
    }

    public String getMaHD() { return maHD; }
    public String getMaKH() { return maKH; }
    public String getTenKhachHang() { return tenKhachHang; }
    public String getMaNV() { return maNV; }
    public String getTenNhanVien() { return tenNhanVien; }
    public BigDecimal getTienCoc() { return tienCoc; }
    public BigDecimal getGiamGia() { return giamGia; }
    public BigDecimal getTongGiaTri() { return tongGiaTri; }
    public String getTrangThai() { return trangThai; }
    public BigDecimal getTongTien() { return tongTien; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
