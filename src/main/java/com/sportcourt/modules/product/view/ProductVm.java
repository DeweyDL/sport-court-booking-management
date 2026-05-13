package com.sportcourt.modules.product.view;

import com.sportcourt.modules.product.dto.ProductResponse;

import java.math.BigDecimal;

final class ProductVm {
    private final String maSp;
    private String tenSp;
    private String dvt;
    private BigDecimal gia;
    private Integer slTon;
    private boolean deleted;
    private String trangThai;

    private ProductVm(
            String maSp,
            String tenSp,
            String dvt,
            BigDecimal gia,
            Integer slTon,
            boolean deleted,
            String trangThai
    ) {
        this.maSp = maSp;
        this.tenSp = tenSp;
        this.dvt = dvt;
        this.gia = gia;
        this.slTon = slTon;
        this.deleted = deleted;
        this.trangThai = trangThai;
    }

    static ProductVm fromResponse(ProductResponse response) {
        return new ProductVm(
                response.getMaSp(),
                response.getTenSp(),
                response.getDvt(),
                response.getGia(),
                response.getSlTon(),
                response.isDeleted(),
                response.getTrangThai()
        );
    }

    String getMaSp() {
        return maSp;
    }

    String getTenSp() {
        return tenSp;
    }

    String getDvt() {
        return dvt;
    }

    BigDecimal getGia() {
        return gia;
    }

    Integer getSlTon() {
        return slTon;
    }

    boolean isDeleted() {
        return deleted;
    }

    String getTrangThai() {
        return trangThai;
    }
}
