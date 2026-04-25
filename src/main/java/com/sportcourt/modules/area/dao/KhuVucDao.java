package com.sportcourt.modules.area.dao;

import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.KhuVuc;
import com.sportcourt.modules.area.enitity.KhuVucCreateRequest;
import com.sportcourt.modules.area.enitity.KhuVucUpdateRequest;
import com.sportcourt.modules.area.enitity.LoaiTheThao;
import com.sportcourt.modules.area.enitity.SanCon;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// DAO chiu trach nhiem doc va cap nhat du lieu khu vuc / san con tu database.
public interface KhuVucDao {
    List<KhuVuc> findByKeyword(String keyword) throws SQLException;

    Optional<KhuVuc> findById(String maKv) throws SQLException;

    List<ChiNhanh> findChiNhanhList() throws SQLException;

    List<LoaiTheThao> findLoaiTheThaoList() throws SQLException;

    List<SanCon> findSanConByKhuVuc(String maKv) throws SQLException;

    void createKhuVuc(KhuVucCreateRequest request) throws SQLException;

    void saveKhuVucChanges(KhuVucUpdateRequest request) throws SQLException;

    boolean softDeleteById(String maKv) throws SQLException;

    boolean softDeleteSanConById(String maSan) throws SQLException;
}
