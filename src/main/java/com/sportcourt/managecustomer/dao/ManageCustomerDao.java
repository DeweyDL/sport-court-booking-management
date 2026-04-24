package com.sportcourt.managecustomer.dao;

import com.sportcourt.managecustomer.dto.CreateCustomerRequest;
import com.sportcourt.managecustomer.dto.CustomerProfile;
import com.sportcourt.managecustomer.dto.CustomerSummary;
import com.sportcourt.managecustomer.dto.UpdateCustomerRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ManageCustomerDao {
    List<CustomerSummary> findByName(String keyword) throws SQLException;

    Optional<CustomerProfile> findProfileById(String maKhachHang) throws SQLException;

    void createCustomer(String userId, String accountId, String maKhachHang, CreateCustomerRequest request,
                        String generatedEmail, String passwordHash, String username) throws SQLException;

    boolean updateCustomer(String maKhachHang, UpdateCustomerRequest request) throws SQLException;

    boolean softDeleteCustomer(String maKhachHang) throws SQLException;
}
