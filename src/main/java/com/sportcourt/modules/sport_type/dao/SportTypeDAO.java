package com.sportcourt.modules.sport_type.dao;

import com.sportcourt.modules.sport_type.dto.SportTypeForm;
import com.sportcourt.modules.sport_type.dto.SportTypeSearchCriteria;
import com.sportcourt.modules.sport_type.dto.SportTypeTableRow;
import com.sportcourt.modules.sport_type.entity.SportType;

import java.sql.SQLException;
import java.util.List;

public interface SportTypeDAO {
    List<SportTypeTableRow> findAll() throws SQLException;
    List<SportTypeTableRow> findById(String sportId) throws SQLException;
    List<SportTypeTableRow> search(SportTypeSearchCriteria keyword) throws SQLException;
    String generateNextId() throws SQLException;
    void insert(SportTypeForm sportTypeForm) throws SQLException;
    void update(SportTypeForm sportTypeForm) throws SQLException;
    void softDelete(SportTypeForm sportTypeForm) throws SQLException;
}
