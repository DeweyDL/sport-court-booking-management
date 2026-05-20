package com.sportcourt.modules.supplier.dao;

import com.sportcourt.modules.supplier.entity.Supplier;
import com.sportcourt.modules.supplier.dto.SupplierCreateRequest;
import com.sportcourt.modules.supplier.dto.SupplierUpdateRequest;

import java.sql.SQLException;
import java.util.List;

public interface SupplierManagementDAO {
    List<Supplier> findSuppliers(String keyword) throws SQLException;
    void createSupplier(String mancc, SupplierCreateRequest request) throws SQLException;
    boolean updateSupplier(SupplierUpdateRequest request) throws SQLException;
    boolean softDeleteSupplier(String mancc) throws SQLException;
    boolean restoreSupplier(String mancc) throws SQLException;
    String generateNextId() throws SQLException;
}