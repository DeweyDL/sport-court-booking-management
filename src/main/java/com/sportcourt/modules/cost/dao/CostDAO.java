package com.sportcourt.modules.cost.dao;

import com.sportcourt.modules.cost.entity.Cost;
import java.util.List;
import java.util.Map;

public interface CostDAO {
    List<Cost> search(String keyword);
    Cost findByMaBg(String maBg);
    void insert(Cost cost);
    void update(Cost cost);
    void delete(String maBg);
    Map<String, String> getAllKhuVuc();
    String generateNextMaBg();
}
