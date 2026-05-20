package com.sportcourt.modules.product.dto;

public class ProductSearchCriteria {
    private String keyword;
    private Boolean includeDeleted = true;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Boolean getIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }
}
