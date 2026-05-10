package com.sportcourt.modules.sport_type.service;

import com.sportcourt.modules.sport_type.dao.SportTypeDAO;
import com.sportcourt.modules.sport_type.dao.SportTypeDAOImpl;
import com.sportcourt.modules.sport_type.dto.SportTypeForm;
import com.sportcourt.modules.sport_type.dto.SportTypeSearchCriteria;
import com.sportcourt.modules.sport_type.dto.SportTypeTableRow;
import com.sportcourt.modules.sport_type.entity.SportType;

import java.sql.SQLException;
import java.util.List;

public class SportTypeServiceImpl implements SportTypeService {
    private final SportTypeDAO sportTypeDAO;

    public SportTypeServiceImpl() {
        this(new SportTypeDAOImpl());
    }

    public SportTypeServiceImpl(SportTypeDAOImpl sportTypeDAOImpl) {
        this.sportTypeDAO = sportTypeDAOImpl;
    }

    @Override
    public List<SportTypeTableRow> getAll() throws SQLException {
        return sportTypeDAO.findAll();
    }

    @Override
    public void create(SportTypeForm form) throws SQLException {
        SportTypeForm info = new SportTypeForm(sportTypeDAO.generateNextId(),
                                               form.getName(),
                                               form.getDescription());
        sportTypeDAO.insert(info);
    }

    @Override
    public void update(SportTypeForm form) throws SQLException {
        sportTypeDAO.update(form);
    }

    @Override
    public void delete(SportTypeForm form) throws SQLException {
        sportTypeDAO.softDelete(form);
    }

    @Override
    public void search(SportTypeSearchCriteria criteria) throws SQLException {
        sportTypeDAO.search(criteria);
    }
}
