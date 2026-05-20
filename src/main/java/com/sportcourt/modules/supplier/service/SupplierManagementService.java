package com.sportcourt.modules.supplier.service;

import com.sportcourt.modules.supplier.entity.Supplier;
import com.sportcourt.modules.supplier.dto.SupplierCreateRequest;
import com.sportcourt.modules.supplier.dto.SupplierUpdateRequest;

import java.sql.SQLException;
import java.util.List;

public interface SupplierManagementService {
    List<Supplier> searchSuppliers(String keyword) throws SQLException;

    String generateNextId() throws SQLException;

    void createSupplier(SupplierCreateRequest request) throws SQLException;

    void updateSupplier(SupplierUpdateRequest request) throws SQLException;

    void deleteSupplier(String mancc) throws SQLException;

    void restoreSupplier(String mancc) throws SQLException;
}