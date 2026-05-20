package com.sportcourt.modules.staff_type.service;

import com.sportcourt.modules.staff_type.dao.StaffTypeDAO;
import com.sportcourt.modules.staff_type.dao.StaffTypeDAOImpl;
import com.sportcourt.modules.staff_type.dto.StaffTypeForm;
import com.sportcourt.modules.staff_type.dto.StaffTypeSearchCriteria;
import com.sportcourt.modules.staff_type.dto.StaffTypeTableRow;

import java.sql.SQLException;
import java.util.List;

public class StaffTypeServiceImpl implements StaffTypeService {
    private final StaffTypeDAO staffTypeDAO;

    public StaffTypeServiceImpl() {
        this(new StaffTypeDAOImpl());
    }

    public StaffTypeServiceImpl(StaffTypeDAOImpl staffTypeDAOImpl) {
        this.staffTypeDAO = staffTypeDAOImpl;
    }

    @Override
    public List<StaffTypeTableRow> getAll() throws SQLException {
        return staffTypeDAO.findAll();
    }

    @Override
    public String generateNextId() throws SQLException {
        return staffTypeDAO.generateNextId();
    }

    @Override
    public void create(StaffTypeForm form) throws SQLException {
        String staffTypeId = form.getStaffTypeId() == null || form.getStaffTypeId().isBlank()
                ? staffTypeDAO.generateNextId()
                : form.getStaffTypeId().trim();
        StaffTypeForm info = new StaffTypeForm(staffTypeId, form.getPosition(), form.getSalary());
        staffTypeDAO.insert(info);
    }

    @Override
    public void update(StaffTypeForm form) throws SQLException {
        staffTypeDAO.update(form);
    }

    @Override
    public void delete(StaffTypeForm form) throws SQLException {
        staffTypeDAO.softDelete(form);
    }

    @Override
    public void search(StaffTypeSearchCriteria criteria) throws SQLException {
        staffTypeDAO.search(criteria);
    }
}
