package com.sportcourt.modules.area.service;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.enitity.AreaCreateRequest;
import com.sportcourt.modules.area.enitity.AreaUpdateRequest;
import com.sportcourt.modules.area.enitity.SportType;
import com.sportcourt.modules.area.enitity.Court;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// Service cho man quan ly va chi tiet khu vuc.
public interface AreaService {
    List<Area> getKhuVucList(String keyword);

    Area getKhuVucDetail(String maKv);

    List<ChiNhanh> getChiNhanhList();

    List<SportType> getLoaiTheThaoList();

    List<Court> getSanConList(String maKv);

    Optional<Path> getAreaImagePath(String maKv);

    String generateNextMaKv();

    String getDefaultChiNhanhId();

    Path saveAreaImage(String maKv, Path sourceFile);

    void createKhuVuc(AreaCreateRequest request);

    void saveKhuVucChanges(AreaUpdateRequest request);

    void deleteKhuVuc(String maKv);

    void deleteSanCon(String maSan);
}
