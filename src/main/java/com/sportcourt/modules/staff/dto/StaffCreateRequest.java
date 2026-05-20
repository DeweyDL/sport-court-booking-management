package com.sportcourt.modules.staff.dto;

public class StaffCreateRequest {
    private String manv;
    private String hoten;
    private String sdt;
    private String diaChi;
    private String cccd;
    private int    isQl;
    private String trangThai;
    private String maCn;
    private String maLoaiNv;

    public StaffCreateRequest() {}

    public String getManv()      { return manv; }
    public void   setManv(String manv) { this.manv = manv; }

    public String getHoten()     { return hoten; }
    public void   setHoten(String hoten) { this.hoten = hoten; }

    public String getSdt()       { return sdt; }
    public void   setSdt(String sdt) { this.sdt = sdt; }

    public String getDiaChi()    { return diaChi; }
    public void   setDiaChi(String diaChi) { this.diaChi = diaChi; }

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