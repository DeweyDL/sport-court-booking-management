package com.sportcourt.modules.staff.dto;

public class StaffUpdateRequest extends StaffCreateRequest {
    private String maNv;
    private String userId;

    public String getMaNv() {
        return maNv;
    }

    public void setMaNv(String maNv) {
        this.maNv = maNv;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
