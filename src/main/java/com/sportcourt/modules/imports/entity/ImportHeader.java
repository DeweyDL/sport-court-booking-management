package com.sportcourt.modules.imports.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ImportHeader {
    private String manh;
    private String mancc;
    private String manv;
    private String maChungTu;
    private BigDecimal triGia;
    private LocalDateTime createdAt;
    private boolean deleted;

    public String getManh() { return manh; }
    public void setManh(String manh) { this.manh = manh; }

    public String getMancc() { return mancc; }
    public void setMancc(String mancc) { this.mancc = mancc; }

    public String getManv() { return manv; }
    public void setManv(String manv) { this.manv = manv; }

    public String getMaChungTu() { return maChungTu; }
    public void setMaChungTu(String maChungTu) { this.maChungTu = maChungTu; }

    public BigDecimal getTriGia() { return triGia; }
    public void setTriGia(BigDecimal triGia) { this.triGia = triGia; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
