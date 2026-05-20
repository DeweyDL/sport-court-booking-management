package com.sportcourt.modules.customer_rank.dao;

import com.sportcourt.modules.customer_rank.dto.CustomerRankCreateRequest;
import com.sportcourt.modules.customer_rank.dto.CustomerRankUpdateRequest;
import com.sportcourt.modules.customer_rank.entity.CustomerRank;

import java.sql.SQLException;
import java.util.List;

public interface CustomerRankDAO {
    List<CustomerRank> findCustomerRanks(String keyword) throws SQLException;
    CustomerRank getCustomerRankById(String maHang) throws SQLException;
    void createCustomerRank(CustomerRankCreateRequest request) throws SQLException;
    boolean updateCustomerRank(CustomerRankUpdateRequest request) throws SQLException;
    boolean softDeleteCustomerRank(String maHang) throws SQLException;
    String generateNextMaHang() throws SQLException;
}
