package com.sportcourt.modules.cost.service;

import com.sportcourt.modules.cost.dao.CostDAO;
import com.sportcourt.modules.cost.dao.CostJdbcDAO;
import com.sportcourt.modules.cost.dto.CostCreateRequest;
import com.sportcourt.modules.cost.dto.CostUpdateRequest;
import com.sportcourt.modules.cost.entity.Cost;

import java.sql.SQLException;
import java.util.List;

public class CostServiceImpl implements CostService {

    private final CostDAO costDAO;

    public CostServiceImpl() {
        this.costDAO = new CostJdbcDAO();
    }

    @Override
    public List<Cost> searchCosts(String keyword) throws SQLException {
        return costDAO.findCosts(keyword);
    }

    @Override
    public Cost getCostById(String maBg) throws SQLException {
        return costDAO.getCostById(maBg);
    }

    @Override
    public void createCost(CostCreateRequest request) throws SQLException {
        costDAO.createCost(request);
    }

    @Override
    public boolean updateCost(CostUpdateRequest request) throws SQLException {
        return costDAO.updateCost(request);
    }

    @Override
    public boolean deleteCost(String maBg) throws SQLException {
        return costDAO.deleteCost(maBg);
    }

    @Override
    public List<String> getAllKhuVucIds() throws SQLException {
        return costDAO.getAllKhuVucIds();
    }
}
