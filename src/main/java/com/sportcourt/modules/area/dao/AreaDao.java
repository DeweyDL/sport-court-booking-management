package com.sportcourt.modules.area.dao;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.enitity.AreaCreateRequest;
import com.sportcourt.modules.area.enitity.AreaUpdateRequest;
import com.sportcourt.modules.area.enitity.SportType;
import com.sportcourt.modules.area.enitity.Court;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// DAO chiu trach nhiem doc va cap nhat du lieu khu vuc / san con tu database.
public interface AreaDao {
    List<Area> findByKeyword(String keyword) throws SQLException;

    Optional<Area> findById(String maKv) throws SQLException;

    List<ChiNhanh> findChiNhanhList() throws SQLException;

    List<SportType> findLoaiTheThaoList() throws SQLException;

    List<Court> findSanConByKhuVuc(String maKv) throws SQLException;

    String generateNextMaKv() throws SQLException;

    String findDefaultChiNhanhId() throws SQLException;

    void createKhuVuc(AreaCreateRequest request) throws SQLException;

    void saveKhuVucChanges(AreaUpdateRequest request) throws SQLException;

    boolean softDeleteById(String maKv) throws SQLException;

    boolean softDeleteSanConById(String maSan) throws SQLException;
}
