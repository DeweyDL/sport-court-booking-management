package com.sportcourt.modules.supplier.dto;

public class SupplierUpdateRequest {
    private String mancc;
    private String tenncc;
    private String sdt;
    private String email;
    private String website;   // thêm WEBSITE (nullable)
    private String diachi;

    public String getMancc()   { return mancc; }
    public void setMancc(String mancc) { this.mancc = mancc; }

    public String getTenncc()  { return tenncc; }
    public void setTenncc(String tenncc) { this.tenncc = tenncc; }

    public String getSdt()     { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getEmail()   { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getDiachi()  { return diachi; }
    public void setDiachi(String diachi) { this.diachi = diachi; }
}