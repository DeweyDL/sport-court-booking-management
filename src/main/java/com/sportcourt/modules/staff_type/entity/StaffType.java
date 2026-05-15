package com.sportcourt.modules.staff_type.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StaffType {
    private String staffTypeId;
    private String position;
    private BigDecimal salary;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    public StaffType() {
    }

    public StaffType(String staffTypeId, String position, BigDecimal salary, LocalDateTime createdAt, boolean isDeleted) {
        this.staffTypeId = staffTypeId;
        this.position = position;
        this.salary = salary;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
    }

    public String getStaffTypeId() {
        return staffTypeId;
    }

    public void setStaffTypeId(String staffTypeId) {
        this.staffTypeId = staffTypeId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
