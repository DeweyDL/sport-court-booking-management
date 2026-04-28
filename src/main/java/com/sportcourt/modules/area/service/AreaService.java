package com.sportcourt.modules.area.service;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.dto.AreaCreateRequest;
import com.sportcourt.modules.area.dto.AreaUpdateRequest;
import com.sportcourt.modules.area.enitity.SportType;

import java.util.List;

public interface AreaService {
    List<Area> getKhuVucList(String keyword);

    Area getKhuVucDetail(String maKv);

    List<ChiNhanh> getChiNhanhList();

    List<SportType> getLoaiTheThaoList();

    // TODO san con: them ham lay danh sach san con theo khu vuc khi module san con hoan thien.

    String generateNextMaKv();

    String getDefaultChiNhanhId();

    void createKhuVuc(AreaCreateRequest request);

    void saveKhuVucChanges(AreaUpdateRequest request);

    void deleteKhuVuc(String maKv);
}
