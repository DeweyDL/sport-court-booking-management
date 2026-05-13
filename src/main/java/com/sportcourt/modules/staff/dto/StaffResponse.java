package com.sportcourt.modules.staff.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StaffResponse {
    private String    manv;
    private String    hoten;
    private String    sdt;
    private String    diaChi;
    private String    cccd;
    private int       isQl;
    private String    chucVu;
    private String    trangThai;
    private LocalDate ngayVaoLam;
    private boolean   isDeleted;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public StaffResponse() {}

    public String    getManv()       { return manv; }
    public void      setManv(String manv) { this.manv = manv; }

    public String    getHoten()      { return hoten; }
    public void      setHoten(String hoten) { this.hoten = hoten; }

    public String    getSdt()        { return sdt; }
    public void      setSdt(String sdt) { this.sdt = sdt; }

    public String    getDiaChi()     { return diaChi; }
    public void      setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String    getCccd()       { return cccd; }
    public void      setCccd(String cccd) { this.cccd = cccd; }

    public int       getIsQl()       { return isQl; }
    public void      setIsQl(int isQl) { this.isQl = isQl; }

    public String    getChucVu()     { return chucVu; }
    public void      setChucVu(String chucVu) { this.chucVu = chucVu; }

    public String    getTrangThai()  { return trangThai; }
    public void      setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDate getNgayVaoLam() { return ngayVaoLam; }
    public void      setNgayVaoLam(LocalDate ngayVaoLam) { this.ngayVaoLam = ngayVaoLam; }

    public boolean   isDeleted()     { return isDeleted; }
    public void      setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    public String    getNgayVaoLamFormatted() {
        return ngayVaoLam == null ? "--" : ngayVaoLam.format(FMT);
    }
}
