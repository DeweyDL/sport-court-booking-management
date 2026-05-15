package com.sportcourt.modules.imports.service;

import com.sportcourt.modules.imports.dto.*;

import java.sql.SQLException;
import java.util.List;

public interface ImportManagementService {

    List<ImportRow> searchImports(String keyword) throws SQLException;

    List<ImportProductDetailDTO> getProductDetails(String manh) throws SQLException;

    List<ImportEquipmentDetailDTO> getEquipmentDetails(String manh) throws SQLException;

    String generateNextImportId() throws SQLException;

    void createImport(ImportCreateRequest request) throws SQLException;

    void updateImport(ImportCreateRequest request) throws SQLException;

    void deleteImport(String manh) throws SQLException;

    List<SupplierOption> getSupplierOptions() throws SQLException;

    List<EmployeeOption> getEmployeeOptions() throws SQLException;

    List<ProductOption> getProductOptions() throws SQLException;

    List<EquipmentOption> getEquipmentOptions() throws SQLException;
}
