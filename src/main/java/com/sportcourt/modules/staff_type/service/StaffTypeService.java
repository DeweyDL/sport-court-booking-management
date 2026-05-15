package com.sportcourt.modules.staff_type.service;

import com.sportcourt.modules.staff_type.dto.StaffTypeForm;
import com.sportcourt.modules.staff_type.dto.StaffTypeSearchCriteria;
import com.sportcourt.modules.staff_type.dto.StaffTypeTableRow;

import java.sql.SQLException;
import java.util.List;

public interface StaffTypeService {
    List<StaffTypeTableRow> getAll() throws SQLException;
    String generateNextId() throws SQLException;
    void create(StaffTypeForm form) throws SQLException;
    void update(StaffTypeForm form) throws SQLException;
    void delete(StaffTypeForm form) throws SQLException;
    void search(StaffTypeSearchCriteria criteria) throws SQLException;
}
