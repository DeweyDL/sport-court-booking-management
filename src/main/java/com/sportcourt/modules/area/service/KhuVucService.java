package com.sportcourt.modules.area.service;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.KhuVuc;
import com.sportcourt.modules.area.enitity.KhuVucCreateRequest;
import com.sportcourt.modules.area.enitity.KhuVucUpdateRequest;
import com.sportcourt.modules.area.enitity.LoaiTheThao;
import com.sportcourt.modules.area.enitity.SanCon;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

// Service cho man quan ly va chi tiet khu vuc.
public interface KhuVucService {
    List<KhuVuc> getKhuVucList(String keyword);

    KhuVuc getKhuVucDetail(String maKv);

    List<ChiNhanh> getChiNhanhList();

    List<LoaiTheThao> getLoaiTheThaoList();

    List<SanCon> getSanConList(String maKv);

    Optional<Path> getAreaImagePath(String maKv);

    String generateNextMaKv();

    String getDefaultChiNhanhId();

    Path saveAreaImage(String maKv, Path sourceFile);

    void createKhuVuc(KhuVucCreateRequest request);

    void saveKhuVucChanges(KhuVucUpdateRequest request);

    void deleteKhuVuc(String maKv);

    void deleteSanCon(String maSan);
}
