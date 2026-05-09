package com.sportcourt.modules.staff.dto;

public class StaffCreateRequest {
    private String manv;
    private String hoten;
    private String cccd;
    private int    isQl;
    private String trangThai;
    private String maCn;      // Mã chi nhánh — bắt buộc (FK_NHAN_VIEN_CHI_NHANH)
    private String maLoaiNv;  // Mã loại nhân viên — bắt buộc (FK_NHAN_VIEN_LOAI)

    public StaffCreateRequest() {}

    public String getManv()      { return manv; }
    public void   setManv(String manv) { this.manv = manv; }

    public String getHoten()     { return hoten; }
    public void   setHoten(String hoten) { this.hoten = hoten; }

    public String getCccd()      { return cccd; }
    public void   setCccd(String cccd) { this.cccd = cccd; }

    public int    getIsQl()      { return isQl; }
    public void   setIsQl(int isQl) { this.isQl = isQl; }

    public String getTrangThai() { return trangThai; }
    public void   setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getMaCn()      { return maCn; }
    public void   setMaCn(String maCn) { this.maCn = maCn; }

    public String getMaLoaiNv()  { return maLoaiNv; }
    public void   setMaLoaiNv(String maLoaiNv) { this.maLoaiNv = maLoaiNv; }
}