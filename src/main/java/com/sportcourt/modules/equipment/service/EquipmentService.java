package com.sportcourt.modules.equipment.service;

import com.sportcourt.modules.equipment.dto.EquipmentCreateRequest;
import com.sportcourt.modules.equipment.dto.EquipmentUpdateRequest;
import com.sportcourt.modules.equipment.entity.Equipment;

import java.sql.SQLException;
import java.util.List;

public interface EquipmentService {
    List<Equipment> searchEquipments(String keyword) throws SQLException;
    Equipment getEquipmentById(String maDc) throws SQLException;
    void createEquipment(EquipmentCreateRequest request) throws SQLException;
    boolean updateEquipment(EquipmentUpdateRequest request) throws SQLException;
    boolean deleteEquipment(String maDc) throws SQLException;
    boolean restoreEquipment(String maDc) throws SQLException;
}
