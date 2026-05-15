package com.sportcourt.modules.staff_type.dao;

import com.sportcourt.modules.staff_type.dto.StaffTypeForm;
import com.sportcourt.modules.staff_type.dto.StaffTypeSearchCriteria;
import com.sportcourt.modules.staff_type.dto.StaffTypeTableRow;

import java.sql.SQLException;
import java.util.List;

public interface StaffTypeDAO {
    List<StaffTypeTableRow> findAll() throws SQLException;
    List<StaffTypeTableRow> findById(String staffTypeId) throws SQLException;
    List<StaffTypeTableRow> search(StaffTypeSearchCriteria criteria) throws SQLException;
    String generateNextId() throws SQLException;
    void insert(StaffTypeForm form) throws SQLException;
    void update(StaffTypeForm form) throws SQLException;
    void softDelete(StaffTypeForm form) throws SQLException;
}
