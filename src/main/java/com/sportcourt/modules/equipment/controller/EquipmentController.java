package com.sportcourt.modules.equipment.controller;

import com.sportcourt.modules.equipment.dto.EquipmentCreateRequest;
import com.sportcourt.modules.equipment.dto.EquipmentUpdateRequest;
import com.sportcourt.modules.equipment.entity.Equipment;
import com.sportcourt.modules.equipment.service.EquipmentService;
import com.sportcourt.modules.equipment.service.EquipmentServiceImpl;

import java.sql.SQLException;
import java.util.List;

public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController() {
        this.equipmentService = new EquipmentServiceImpl();
    }

    public List<Equipment> searchEquipments(String keyword) throws SQLException {
        return equipmentService.searchEquipments(keyword);
    }

    public Equipment getEquipmentById(String maDc) throws SQLException {
        return equipmentService.getEquipmentById(maDc);
    }

    public String generateNextMaDc() throws SQLException {
        return equipmentService.generateNextMaDc();
    }

    public void createEquipment(EquipmentCreateRequest request) throws SQLException {
        equipmentService.createEquipment(request);
    }

    public boolean updateEquipment(EquipmentUpdateRequest request) throws SQLException {
        return equipmentService.updateEquipment(request);
    }

    public boolean deleteEquipment(String maDc) throws SQLException {
        return equipmentService.deleteEquipment(maDc);
    }

    public boolean restoreEquipment(String maDc) throws SQLException {
        return equipmentService.restoreEquipment(maDc);
    }
}
