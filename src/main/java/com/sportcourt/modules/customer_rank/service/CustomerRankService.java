package com.sportcourt.modules.customer_rank.service;

import com.sportcourt.modules.customer_rank.dto.CustomerRankCreateRequest;
import com.sportcourt.modules.customer_rank.dto.CustomerRankUpdateRequest;
import com.sportcourt.modules.customer_rank.entity.CustomerRank;

import java.sql.SQLException;
import java.util.List;

public interface CustomerRankService {
    List<CustomerRank> searchRanks(String keyword) throws SQLException;
    CustomerRank getRankById(String maHang) throws SQLException;
    void createRank(CustomerRankCreateRequest request) throws SQLException;
    boolean updateRank(CustomerRankUpdateRequest request) throws SQLException;
    boolean deleteRank(String maHang) throws SQLException;
    String generateNextMaHang() throws SQLException;
}
