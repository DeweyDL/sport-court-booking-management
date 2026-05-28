package com.sportcourt.modules.customer.dao;

import com.sportcourt.modules.customer.dto.CreateCustomerRequest;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerSummary;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;
import com.sportcourt.modules.customer.entity.KhachHang;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ManageCustomerDao {
    List<CustomerSummary> findByName(String keyword) throws SQLException;

    Optional<CustomerProfile> findProfileById(String maKhachHang) throws SQLException;

    Optional<KhachHang> findKhachHangById(String maKhachHang) throws SQLException;

    int countCustomers() throws SQLException;

    String nextNumericId(String tableName, String idColumn, String prefix) throws SQLException;

    void createCustomer(String userId, String accountId, String maKhachHang, String accountRoleGroupId,
                        CreateCustomerRequest request, String passwordHash, String username) throws SQLException;

    boolean updateCustomer(String maKhachHang, UpdateCustomerRequest request) throws SQLException;

    boolean softDeleteCustomer(String maKhachHang) throws SQLException;

    boolean restoreCustomer(String maKhachHang) throws SQLException;
}

