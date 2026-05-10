package com.sportcourt.modules.sport_type.service;

import com.sportcourt.modules.sport_type.dao.SportTypeDAO;
import com.sportcourt.modules.sport_type.dao.SportTypeDAOImpl;
import com.sportcourt.modules.sport_type.dto.SportTypeForm;
import com.sportcourt.modules.sport_type.dto.SportTypeSearchCriteria;
import com.sportcourt.modules.sport_type.dto.SportTypeTableRow;
import com.sportcourt.modules.sport_type.entity.SportType;

import java.sql.SQLException;
import java.util.List;

public interface SportTypeService {
    public List<SportTypeTableRow> getAll() throws SQLException;
    public void create(SportTypeForm form) throws SQLException;
    public void update(SportTypeForm form) throws SQLException;
    public void delete(SportTypeForm form) throws SQLException;
    public void search(SportTypeSearchCriteria criteria) throws SQLException;
}
