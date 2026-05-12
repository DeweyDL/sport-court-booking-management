package com.sportcourt.modules.cost.service;

import com.sportcourt.modules.cost.entity.Cost;
import java.util.List;
import java.util.Map;

public interface CostService {
    List<Cost> searchCosts(String keyword);
    Cost getCostDetail(String maBg);
    void createCost(Cost cost);
    void updateCost(Cost cost);
    void deleteCost(String maBg);
    Map<String, String> getKhuVucOptions();
    String generateNextMaBg();
}
