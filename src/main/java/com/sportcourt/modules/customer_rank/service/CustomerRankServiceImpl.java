package com.sportcourt.modules.customer_rank.service;

import com.sportcourt.modules.customer_rank.dao.CustomerRankDAO;
import com.sportcourt.modules.customer_rank.dao.CustomerRankJdbcDAO;
import com.sportcourt.modules.customer_rank.dto.CustomerRankCreateRequest;
import com.sportcourt.modules.customer_rank.dto.CustomerRankUpdateRequest;
import com.sportcourt.modules.customer_rank.entity.CustomerRank;

import java.sql.SQLException;
import java.util.List;

public class CustomerRankServiceImpl implements CustomerRankService {

    private final CustomerRankDAO customerRankDAO;

    public CustomerRankServiceImpl() {
        this.customerRankDAO = new CustomerRankJdbcDAO();
    }

    public CustomerRankServiceImpl(CustomerRankDAO customerRankDAO) {
        this.customerRankDAO = customerRankDAO;
    }

    @Override
    public List<CustomerRank> searchRanks(String keyword) throws SQLException {
        return customerRankDAO.findCustomerRanks(keyword);
    }

    @Override
    public CustomerRank getRankById(String maHang) throws SQLException {
        return customerRankDAO.getCustomerRankById(maHang);
    }

    @Override
    public void createRank(CustomerRankCreateRequest request) throws SQLException {
        customerRankDAO.createCustomerRank(request);
    }

    @Override
    public boolean updateRank(CustomerRankUpdateRequest request) throws SQLException {
        return customerRankDAO.updateCustomerRank(request);
    }

    @Override
    public boolean deleteRank(String maHang) throws SQLException {
        return customerRankDAO.softDeleteCustomerRank(maHang);
    }
}
