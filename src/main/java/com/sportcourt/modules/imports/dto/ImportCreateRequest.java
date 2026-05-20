package com.sportcourt.modules.imports.dto;

import java.math.BigDecimal;
import java.util.List;

public class ImportCreateRequest {
    private String manh;
    private String mancc;
    private String manv;
    private String maChungTu;
    private BigDecimal triGia;
    private List<ImportProductDetailDTO> productDetails;
    private List<ImportEquipmentDetailDTO> equipmentDetails;

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

    public List<ImportProductDetailDTO> getProductDetails() { return productDetails; }
    public void setProductDetails(List<ImportProductDetailDTO> productDetails) { this.productDetails = productDetails; }

    public List<ImportEquipmentDetailDTO> getEquipmentDetails() { return equipmentDetails; }
    public void setEquipmentDetails(List<ImportEquipmentDetailDTO> equipmentDetails) { this.equipmentDetails = equipmentDetails; }
}
