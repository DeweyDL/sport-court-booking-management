package com.sportcourt.modules.cost.controller;

import com.sportcourt.modules.cost.dto.CostCreateRequest;
import com.sportcourt.modules.cost.dto.CostUpdateRequest;
import com.sportcourt.modules.cost.entity.Cost;
import com.sportcourt.modules.cost.service.CostService;
import com.sportcourt.modules.cost.service.CostServiceImpl;

import java.sql.SQLException;
import java.util.List;

public class CostController {

    private final CostService costService;

    public CostController() {
        this.costService = new CostServiceImpl();
    }

    public List<Cost> searchCosts(String keyword) throws SQLException {
        return costService.searchCosts(keyword);
    }

    public Cost getCostById(String maBg) throws SQLException {
        return costService.getCostById(maBg);
    }

    public void createCost(CostCreateRequest request) throws SQLException {
        if (request.getGia() == null || request.getGia().signum() <= 0) {
            throw new IllegalArgumentException("Giá phải lớn hơn 0");
        }
        if (request.getGioBatDau() < 0 || request.getGioKetThuc() > 24 || request.getGioBatDau() >= request.getGioKetThuc()) {
            throw new IllegalArgumentException("Khung giờ không hợp lệ");
        }
        costService.createCost(request);
    }

    public boolean updateCost(CostUpdateRequest request) throws SQLException {
        if (request.getGia() == null || request.getGia().signum() <= 0) {
            throw new IllegalArgumentException("Giá phải lớn hơn 0");
        }
        if (request.getGioBatDau() < 0 || request.getGioKetThuc() > 24 || request.getGioBatDau() >= request.getGioKetThuc()) {
            throw new IllegalArgumentException("Khung giờ không hợp lệ");
        }
        return costService.updateCost(request);
    }

    public boolean deleteCost(String maBg) throws SQLException {
        return costService.deleteCost(maBg);
    }

    public List<String> getKhuVucOptions() {
        try {
            return costService.getAllKhuVucIds();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.List.of();
        }
    }
}
