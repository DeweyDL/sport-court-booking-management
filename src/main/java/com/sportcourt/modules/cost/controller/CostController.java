package com.sportcourt.modules.cost.controller;

import com.sportcourt.modules.cost.service.CostService;
import com.sportcourt.modules.cost.service.CostServiceImpl;
import com.sportcourt.modules.cost.entity.Cost;
import java.util.List;
import java.util.Map;

public class CostController {
    private final CostService costService = new CostServiceImpl();

    public List<Cost> searchCosts(String keyword) { return costService.searchCosts(keyword); }
    public Cost getCostDetail(String maBg) { return costService.getCostDetail(maBg); }
    public void createCost(Cost cost) { costService.createCost(cost); }
    public void updateCost(Cost cost) { costService.updateCost(cost); }
    public void deleteCost(String maBg) { costService.deleteCost(maBg); }
    public Map<String, String> getKhuVucOptions() { return costService.getKhuVucOptions(); }
    public String generateNextMaBg() { return costService.generateNextMaBg(); }
}
