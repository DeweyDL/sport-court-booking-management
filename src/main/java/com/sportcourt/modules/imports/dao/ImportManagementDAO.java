package com.sportcourt.modules.imports.dao;

import com.sportcourt.modules.imports.dto.*;

import java.sql.SQLException;
import java.util.List;

public interface ImportManagementDAO {

    List<ImportRow> findImports(String keyword) throws SQLException;

    List<ImportProductDetailDTO> findProductDetails(String manh) throws SQLException;

    List<ImportEquipmentDetailDTO> findEquipmentDetails(String manh) throws SQLException;

    String generateNextImportId() throws SQLException;

    String generateNextProductDetailId() throws SQLException;

    String generateNextEquipmentDetailId() throws SQLException;

    void createImport(ImportCreateRequest request) throws SQLException;

    void updateImport(ImportCreateRequest request) throws SQLException;

    boolean deleteImport(String manh) throws SQLException;

    List<SupplierOption> findSupplierOptions() throws SQLException;

    List<EmployeeOption> findEmployeeOptions() throws SQLException;

    List<ProductOption> findProductOptions() throws SQLException;

    List<EquipmentOption> findEquipmentOptions() throws SQLException;
}
