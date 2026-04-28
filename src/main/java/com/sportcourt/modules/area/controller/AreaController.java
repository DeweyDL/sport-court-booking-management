package com.sportcourt.modules.area.controller;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.enitity.AreaCreateRequest;
import com.sportcourt.modules.area.enitity.AreaUpdateRequest;
import com.sportcourt.modules.area.enitity.SportType;
import com.sportcourt.modules.area.enitity.Court;
import com.sportcourt.modules.area.service.AreaService;
import com.sportcourt.modules.area.service.AreaServiceImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// Controller la diem giao tiep giua view va service cho module khu vuc.
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

    public List<Court> getSanConList(String maKv) {
        return areaService.getSanConList(maKv);
    }

    public Optional<Path> getAreaImagePath(String maKv) {
        return areaService.getAreaImagePath(maKv);
    }

    public String generateNextMaKv() {
        return areaService.generateNextMaKv();
    }

    public String getDefaultChiNhanhId() {
        return areaService.getDefaultChiNhanhId();
    }

    public Path saveAreaImage(String maKv, Path sourceFile) {
        return areaService.saveAreaImage(maKv, sourceFile);
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

    public void deleteSanCon(String maSan) {
        areaService.deleteSanCon(maSan);
    }
}
