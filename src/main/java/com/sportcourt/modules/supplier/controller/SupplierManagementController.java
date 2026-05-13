package com.sportcourt.modules.supplier.controller;

import com.sportcourt.modules.supplier.entity.Supplier;
import com.sportcourt.modules.supplier.dto.SupplierCreateRequest;
import com.sportcourt.modules.supplier.dto.SupplierUpdateRequest;
import com.sportcourt.modules.supplier.service.SupplierManagementService;
import com.sportcourt.modules.supplier.service.SupplierManagementServiceImpl;

import java.sql.SQLException;
import java.util.List;

public class SupplierManagementController {
    private final SupplierManagementService supplierManagementService;

    public SupplierManagementController() {
        this(new SupplierManagementServiceImpl());
    }

    public SupplierManagementController(SupplierManagementService supplierManagementService) {
        this.supplierManagementService = supplierManagementService;
    }

    public List<Supplier> searchSuppliers(String keyword) throws SQLException {
        return supplierManagementService.searchSuppliers(keyword);
    }

    public void createSupplier(SupplierCreateRequest request) throws SQLException {
        supplierManagementService.createSupplier(request);
    }

    public void updateSupplier(SupplierUpdateRequest request) throws SQLException {
        supplierManagementService.updateSupplier(request);
    }

    public void deleteSupplier(String mancc) throws SQLException {
        supplierManagementService.deleteSupplier(mancc);
    }

    public void restoreSupplier(String mancc) throws SQLException {
        supplierManagementService.restoreSupplier(mancc);
    }
}