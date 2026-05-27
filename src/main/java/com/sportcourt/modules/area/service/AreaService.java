package com.sportcourt.modules.area.service;

import com.sportcourt.modules.area.entity.Area;
import com.sportcourt.modules.area.dto.AreaCreateRequest;
import com.sportcourt.modules.area.dto.AreaUpdateRequest;

import java.util.List;

public interface AreaService {
    List<Area> getKhuVucList(String keyword);

    Area getKhuVucDetail(String maKv);

    List<Area.ChiNhanhOption> getChiNhanhList();

    List<Area.SportTypeOption> getLoaiTheThaoList();

    String generateNextMaKv();

    String getDefaultChiNhanhId();

    void createKhuVuc(AreaCreateRequest request);

    void saveKhuVucChanges(AreaUpdateRequest request);

    void deleteKhuVuc(String maKv);
}
