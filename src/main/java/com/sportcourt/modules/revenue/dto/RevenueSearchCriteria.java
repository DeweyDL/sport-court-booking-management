package com.sportcourt.modules.revenue.dto;

import java.time.LocalDate;

public class RevenueSearchCriteria {
    private String keyword;
    private String maCn;      // null = tất cả chi nhánh
    private LocalDate fromDate;
    private LocalDate toDate;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getMaCn() { return maCn; }
    public void setMaCn(String maCn) { this.maCn = maCn; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
}
