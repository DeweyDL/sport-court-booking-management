package com.sportcourt.modules.revenue.dto;

import java.time.LocalDate;

public class RevenueSearchCriteria {
    private String keyword;
    private String maCn;      // null = tất cả chi nhánh (MACN IS NULL)
    private boolean filterNullBranch; // true = chỉ lấy record có MACN IS NULL
    private String loai;      // null = tất cả, NGAY/TUAN/THANG/NAM
    private LocalDate fromDate;
    private LocalDate toDate;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getMaCn() { return maCn; }
    public void setMaCn(String maCn) { this.maCn = maCn; }

    public boolean isFilterNullBranch() { return filterNullBranch; }
    public void setFilterNullBranch(boolean filterNullBranch) { this.filterNullBranch = filterNullBranch; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
}
