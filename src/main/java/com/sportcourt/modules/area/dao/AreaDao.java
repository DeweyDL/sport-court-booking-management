package com.sportcourt.modules.area.dao;

import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.dto.AreaCreateRequest;
import com.sportcourt.modules.area.dto.AreaUpdateRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AreaDao {
    List<Area> findByKeyword(String keyword) throws SQLException;

    Optional<Area> findById(String maKv) throws SQLException;

    List<Area.ChiNhanhOption> findChiNhanhList() throws SQLException;

    List<Area.SportTypeOption> findLoaiTheThaoList() throws SQLException;

    // TODO san con: them API doc/kiem tra san con theo khu vuc khi module san con hoan thien.

    String generateNextMaKv() throws SQLException;

    String findDefaultChiNhanhId() throws SQLException;

    void createKhuVuc(AreaCreateRequest request) throws SQLException;

    void saveKhuVucChanges(AreaUpdateRequest request) throws SQLException;

    boolean softDeleteById(String maKv) throws SQLException;
}
