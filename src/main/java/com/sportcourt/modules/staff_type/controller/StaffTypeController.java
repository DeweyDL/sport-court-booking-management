package com.sportcourt.modules.staff_type.controller;

import com.sportcourt.modules.staff_type.dto.StaffTypeForm;
import com.sportcourt.modules.staff_type.dto.StaffTypeTableRow;
import com.sportcourt.modules.staff_type.service.StaffTypeService;
import com.sportcourt.modules.staff_type.service.StaffTypeServiceImpl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class StaffTypeController {
    private final StaffTypeService service;

    public StaffTypeController() {
        this(new StaffTypeServiceImpl());
    }

    public StaffTypeController(StaffTypeService service) {
        this.service = service;
    }

    public List<StaffTypeTableRow> loadAll() {
        try {
            return service.getAll();
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public String generateNextId() {
        try {
            return service.generateNextId();
        } catch (SQLException e) {
            return "LNV-1";
        }
    }

    /** @return null on success, error message on failure */
    public String create(StaffTypeForm form) {
        try {
            service.create(form);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /** @return null on success, error message on failure */
    public String update(StaffTypeForm form) {
        try {
            service.update(form);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /** @return null on success, error message on failure */
    public String delete(StaffTypeForm form) {
        try {
            service.delete(form);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}
