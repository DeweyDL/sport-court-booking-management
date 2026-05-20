package com.sportcourt.modules.staff.entity;

import java.time.LocalDate;

public class Staff {
    private String manv;        // MANV
    private String userId;      // USER_ID (Khóa ngoại tới USERS)
    private String malnv;       // MALNV (Mã loại nhân viên)
    private String macn;        // MACN (Mã chi nhánh)
    private LocalDate nvl;      // NVL (Ngày vào làm)
    private String cccd;        // CCCD
    private int isQl;           // IS_QL (1: Quản lý, 0: Nhân viên)
    private String trangThai;   // TRANG_THAI
    private int isDeleted;      // IS_DELETED

    public Staff() {}

    public String getManv() { return manv; }
    public void setManv(String manv) { this.manv = manv; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMalnv() { return malnv; }
    public void setMalnv(String malnv) { this.malnv = malnv; }

    public String getMacn() { return macn; }
    public void setMacn(String macn) { this.macn = macn; }

    public LocalDate getNvl() { return nvl; }
    public void setNvl(LocalDate nvl) { this.nvl = nvl; }

    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }

    public int getIsQl() { return isQl; }
    public void setIsQl(int isQl) { this.isQl = isQl; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }
}