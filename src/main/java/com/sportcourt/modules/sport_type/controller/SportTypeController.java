package com.sportcourt.modules.sport_type.controller;

import com.sportcourt.modules.sport_type.dto.SportTypeForm;
import com.sportcourt.modules.sport_type.dto.SportTypeTableRow;
import com.sportcourt.modules.sport_type.service.SportTypeService;
import com.sportcourt.modules.sport_type.service.SportTypeServiceImpl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class SportTypeController {
    private final SportTypeService service;

    public SportTypeController() {
        this(new SportTypeServiceImpl());
    }

    public SportTypeController(SportTypeService service) {
        this.service = service;
    }

    public List<SportTypeTableRow> loadAll() {
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
            return "LTT-1";
        }
    }

    /** @return null on success, error message on failure */
    public String create(SportTypeForm form) {
        try {
            service.create(form);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /** @return null on success, error message on failure */
    public String update(SportTypeForm form) {
        try {
            service.update(form);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /** @return null on success, error message on failure */
    public String delete(SportTypeForm form) {
        try {
            service.delete(form);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}
