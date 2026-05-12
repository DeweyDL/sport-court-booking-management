package com.sportcourt.modules.cost.service;

import com.sportcourt.modules.cost.dao.CostDAO;
import com.sportcourt.modules.cost.dao.CostJdbcDAO;
import com.sportcourt.modules.cost.entity.Cost;
import java.util.List;
import java.util.Map;

public class CostServiceImpl implements CostService {
    private final CostDAO costDAO = new CostJdbcDAO();

    @Override
    public List<Cost> searchCosts(String keyword) {
        return costDAO.search(keyword);
    }

    @Override
    public Cost getCostDetail(String maBg) {
        return costDAO.findByMaBg(maBg);
    }

    @Override
    public void createCost(Cost cost) {
        costDAO.insert(cost);
    }

    @Override
    public void updateCost(Cost cost) {
        costDAO.update(cost);
    }

    @Override
    public void deleteCost(String maBg) {
        costDAO.delete(maBg);
    }

    @Override
    public Map<String, String> getKhuVucOptions() {
        return costDAO.getAllKhuVuc();
    }

    @Override
    public String generateNextMaBg() {
        return costDAO.generateNextMaBg();
    }
}
