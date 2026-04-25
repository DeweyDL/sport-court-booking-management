package com.sportcourt.modules.area.controller;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.KhuVuc;
import com.sportcourt.modules.area.enitity.KhuVucCreateRequest;
import com.sportcourt.modules.area.enitity.KhuVucUpdateRequest;
import com.sportcourt.modules.area.enitity.LoaiTheThao;
import com.sportcourt.modules.area.enitity.SanCon;
import com.sportcourt.modules.area.service.KhuVucService;
import com.sportcourt.modules.area.service.KhuVucServiceImpl;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// Controller la diem giao tiep giua view va service cho module khu vuc.
public class KhuVucController {
    private final KhuVucService khuVucService;

    public KhuVucController() {
        this(new KhuVucServiceImpl());
    }

    public KhuVucController(KhuVucService khuVucService) {
        this.khuVucService = khuVucService;
    }

    public List<KhuVuc> getKhuVucList(String keyword) {
        return khuVucService.getKhuVucList(keyword);
    }

    public KhuVuc getKhuVucDetail(String maKv) {
        return khuVucService.getKhuVucDetail(maKv);
    }

    public List<ChiNhanh> getChiNhanhList() {
        return khuVucService.getChiNhanhList();
    }

    public List<LoaiTheThao> getLoaiTheThaoList() {
        return khuVucService.getLoaiTheThaoList();
    }

    public List<SanCon> getSanConList(String maKv) {
        return khuVucService.getSanConList(maKv);
    }

    public Optional<Path> getAreaImagePath(String maKv) {
        return khuVucService.getAreaImagePath(maKv);
    }

    public Path saveAreaImage(String maKv, Path sourceFile) {
        return khuVucService.saveAreaImage(maKv, sourceFile);
    }

    public void createKhuVuc(KhuVucCreateRequest request) {
        khuVucService.createKhuVuc(request);
    }

    public void saveKhuVucChanges(KhuVucUpdateRequest request) {
        khuVucService.saveKhuVucChanges(request);
    }

    public void deleteKhuVuc(String maKv) {
        khuVucService.deleteKhuVuc(maKv);
    }

    public void deleteSanCon(String maSan) {
        khuVucService.deleteSanCon(maSan);
    }
}
