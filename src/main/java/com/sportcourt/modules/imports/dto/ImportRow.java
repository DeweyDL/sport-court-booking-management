package com.sportcourt.modules.imports.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ImportRow {
    private String manh;
    private String mancc;
    private String tenNcc;
    private String manv;
    private String tenNv;
    private String maChungTu;
    private BigDecimal triGia;
    private LocalDateTime createdAt;
    private boolean deleted;

    public String getManh() { return manh; }
    public void setManh(String manh) { this.manh = manh; }

    public String getMancc() { return mancc; }
    public void setMancc(String mancc) { this.mancc = mancc; }

    public String getTenNcc() { return tenNcc; }
    public void setTenNcc(String tenNcc) { this.tenNcc = tenNcc; }

    public String getManv() { return manv; }
    public void setManv(String manv) { this.manv = manv; }

    public String getTenNv() { return tenNv; }
    public void setTenNv(String tenNv) { this.tenNv = tenNv; }

    public String getMaChungTu() { return maChungTu; }
    public void setMaChungTu(String maChungTu) { this.maChungTu = maChungTu; }

    public BigDecimal getTriGia() { return triGia; }
    public void setTriGia(BigDecimal triGia) { this.triGia = triGia; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
