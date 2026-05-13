package com.sportcourt.modules.staff.dto;

public class StaffUpdateRequest {
    private String hoten;
    private String sdt;
    private String diaChi;
    private String cccd;
    private int isQl;
    private String trangThai;

    public StaffUpdateRequest() {}

    public String getHoten() { return hoten; }
    public void setHoten(String hoten) { this.hoten = hoten; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public int getIsQl() { return isQl; }
    public void setIsQl(int isQl) { this.isQl = isQl; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}