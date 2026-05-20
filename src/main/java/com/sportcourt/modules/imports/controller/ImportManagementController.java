package com.sportcourt.modules.imports.controller;

import com.sportcourt.modules.imports.dto.*;
import com.sportcourt.modules.imports.service.ImportManagementService;
import com.sportcourt.modules.imports.service.ImportManagementServiceImpl;

import java.sql.SQLException;
import java.util.List;

public class ImportManagementController {
    private final ImportManagementService importManagementService;

    public ImportManagementController() {
        this(new ImportManagementServiceImpl());
    }

    public ImportManagementController(ImportManagementService importManagementService) {
        this.importManagementService = importManagementService;
    }

    public List<ImportRow> searchImports(String keyword) throws SQLException {
        return importManagementService.searchImports(keyword);
    }

    public List<ImportProductDetailDTO> getProductDetails(String manh) throws SQLException {
        return importManagementService.getProductDetails(manh);
    }

    public List<ImportEquipmentDetailDTO> getEquipmentDetails(String manh) throws SQLException {
        return importManagementService.getEquipmentDetails(manh);
    }

    public String generateNextImportId() throws SQLException {
        return importManagementService.generateNextImportId();
    }

    public void createImport(ImportCreateRequest request) throws SQLException {
        importManagementService.createImport(request);
    }

    public void updateImport(ImportCreateRequest request) throws SQLException {
        importManagementService.updateImport(request);
    }

    public void deleteImport(String manh) throws SQLException {
        importManagementService.deleteImport(manh);
    }

    public List<SupplierOption> getSupplierOptions() throws SQLException {
        return importManagementService.getSupplierOptions();
    }

    public List<EmployeeOption> getEmployeeOptions() throws SQLException {
        return importManagementService.getEmployeeOptions();
    }

    public List<ProductOption> getProductOptions() throws SQLException {
        return importManagementService.getProductOptions();
    }

    public List<EquipmentOption> getEquipmentOptions() throws SQLException {
        return importManagementService.getEquipmentOptions();
    }
}
