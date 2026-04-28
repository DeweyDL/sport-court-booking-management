package com.sportcourt.modules.area.controller;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.dto.AreaCreateRequest;
import com.sportcourt.modules.area.dto.AreaUpdateRequest;
import com.sportcourt.modules.area.enitity.SportType;
import com.sportcourt.modules.area.service.AreaService;
import com.sportcourt.modules.area.service.AreaServiceImpl;

import java.util.List;

public class AreaController {
    private final AreaService areaService;

    public AreaController() {
        this(new AreaServiceImpl());
    }

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    public List<Area> getKhuVucList(String keyword) {
        return areaService.getKhuVucList(keyword);
    }

    public Area getKhuVucDetail(String maKv) {
        return areaService.getKhuVucDetail(maKv);
    }

    public List<ChiNhanh> getChiNhanhList() {
        return areaService.getChiNhanhList();
    }

    public List<SportType> getLoaiTheThaoList() {
        return areaService.getLoaiTheThaoList();
    }

    public String generateNextMaKv() {
        return areaService.generateNextMaKv();
    }

    public String getDefaultChiNhanhId() {
        return areaService.getDefaultChiNhanhId();
    }

    public void createKhuVuc(AreaCreateRequest request) {
        areaService.createKhuVuc(request);
    }

    public void saveKhuVucChanges(AreaUpdateRequest request) {
        areaService.saveKhuVucChanges(request);
    }

    public void deleteKhuVuc(String maKv) {
        areaService.deleteKhuVuc(maKv);
    }
}
