package com.sportcourt.modules.supplier.entity;

import java.time.LocalDate;

public class Supplier {
    private String mancc;
    private String tenncc;
    private String sdt;
    private String email;
    private String website;
    private String diachi;
    private LocalDate createdAt;
    private boolean deleted;

    // Getters and Setters
    public String getMancc() { return mancc; }
    public void setMancc(String mancc) { this.mancc = mancc; }
    public String getTenncc() { return tenncc; }
    public void setTenncc(String tenncc) { this.tenncc = tenncc; }
    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getDiachi() { return diachi; }
    public void setDiachi(String diachi) { this.diachi = diachi; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}