package com.sportcourt.modules.cost.dao;

import com.sportcourt.modules.cost.dto.CostCreateRequest;
import com.sportcourt.modules.cost.dto.CostUpdateRequest;
import com.sportcourt.modules.cost.entity.Cost;

import java.sql.SQLException;
import java.util.List;

public interface CostDAO {
    List<Cost> findCosts(String keyword) throws SQLException;
    Cost getCostById(String maBg) throws SQLException;
    void createCost(CostCreateRequest request) throws SQLException;
    boolean updateCost(CostUpdateRequest request) throws SQLException;
    boolean deleteCost(String maBg) throws SQLException;
    List<String> getAllKhuVucIds() throws SQLException;
}
