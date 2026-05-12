package com.sportcourt.modules.customer_rank.controller;

import com.sportcourt.modules.customer_rank.dto.CustomerRankCreateRequest;
import com.sportcourt.modules.customer_rank.dto.CustomerRankUpdateRequest;
import com.sportcourt.modules.customer_rank.entity.CustomerRank;
import com.sportcourt.modules.customer_rank.service.CustomerRankService;
import com.sportcourt.modules.customer_rank.service.CustomerRankServiceImpl;

import java.sql.SQLException;
import java.util.List;

public class CustomerRankController {
    private final CustomerRankService customerRankService;

    public CustomerRankController() {
        this.customerRankService = new CustomerRankServiceImpl();
    }

    public CustomerRankController(CustomerRankService customerRankService) {
        this.customerRankService = customerRankService;
    }

    public List<CustomerRank> searchRanks(String keyword) throws SQLException {
        return customerRankService.searchRanks(keyword);
    }

    public CustomerRank getRankById(String maHang) throws SQLException {
        return customerRankService.getRankById(maHang);
    }

    public void createRank(CustomerRankCreateRequest request) throws SQLException {
        customerRankService.createRank(request);
    }

    public boolean updateRank(CustomerRankUpdateRequest request) throws SQLException {
        return customerRankService.updateRank(request);
    }

    public boolean deleteRank(String maHang) throws SQLException {
        return customerRankService.deleteRank(maHang);
    }
}
