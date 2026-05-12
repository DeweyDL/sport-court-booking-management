package com.sportcourt.modules.equipment.service;

import com.sportcourt.modules.equipment.dao.EquipmentDAO;
import com.sportcourt.modules.equipment.dao.EquipmentJdbcDAO;
import com.sportcourt.modules.equipment.dto.EquipmentCreateRequest;
import com.sportcourt.modules.equipment.dto.EquipmentUpdateRequest;
import com.sportcourt.modules.equipment.entity.Equipment;

import java.sql.SQLException;
import java.util.List;

public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentDAO equipmentDAO;

    public EquipmentServiceImpl() {
        this.equipmentDAO = new EquipmentJdbcDAO();
    }

    @Override
    public List<Equipment> searchEquipments(String keyword) throws SQLException {
        return equipmentDAO.findEquipments(keyword);
    }

    @Override
    public Equipment getEquipmentById(String maDc) throws SQLException {
        return equipmentDAO.getEquipmentById(maDc);
    }

    @Override
    public void createEquipment(EquipmentCreateRequest request) throws SQLException {
        equipmentDAO.createEquipment(request);
    }

    @Override
    public boolean updateEquipment(EquipmentUpdateRequest request) throws SQLException {
        return equipmentDAO.updateEquipment(request);
    }

    @Override
    public boolean deleteEquipment(String maDc) throws SQLException {
        return equipmentDAO.softDeleteEquipment(maDc);
    }

    @Override
    public boolean restoreEquipment(String maDc) throws SQLException {
        return equipmentDAO.restoreEquipment(maDc);
    }
}
