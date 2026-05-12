package com.sportcourt.modules.equipment.dao;

import com.sportcourt.modules.equipment.dto.EquipmentCreateRequest;
import com.sportcourt.modules.equipment.dto.EquipmentUpdateRequest;
import com.sportcourt.modules.equipment.entity.Equipment;

import java.sql.SQLException;
import java.util.List;

public interface EquipmentDAO {
    List<Equipment> findEquipments(String keyword) throws SQLException;
    Equipment getEquipmentById(String maDc) throws SQLException;
    void createEquipment(EquipmentCreateRequest request) throws SQLException;
    boolean updateEquipment(EquipmentUpdateRequest request) throws SQLException;
    boolean softDeleteEquipment(String maDc) throws SQLException;
    boolean restoreEquipment(String maDc) throws SQLException;
}
