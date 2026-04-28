package com.sportcourt.modules.area.service;

import com.sportcourt.modules.area.dao.JdbcAreaDao;
import com.sportcourt.modules.area.dao.AreaDao;
import com.sportcourt.modules.area.enitity.ChiNhanh;
import com.sportcourt.modules.area.enitity.Area;
import com.sportcourt.modules.area.enitity.AreaCreateRequest;
import com.sportcourt.modules.area.enitity.AreaUpdateRequest;
import com.sportcourt.modules.area.enitity.SportType;
import com.sportcourt.modules.area.enitity.Court;
import com.sportcourt.modules.area.util.AreaImageStorage;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Service cho module khu vuc. File nay gom loi SQL thanh IllegalStateException de view xu ly gon hon.
public class AreaServiceImpl implements AreaService {
    private final AreaDao khuVucDao;

    public AreaServiceImpl() {
        this(new JdbcAreaDao());
    }

    public AreaServiceImpl(AreaDao khuVucDao) {
        this.khuVucDao = khuVucDao;
    }

    @Override
    public List<Area> getKhuVucList(String keyword) {
        try {
            return khuVucDao.findByKeyword(keyword);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the tai du lieu khu vuc tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Area getKhuVucDetail(String maKv) {
        try {
            return khuVucDao.findById(maKv)
                    .orElseThrow(() -> new IllegalStateException("Khong tim thay khu vuc: " + maKv));
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the tai chi tiet khu vuc tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<ChiNhanh> getChiNhanhList() {
        try {
            return khuVucDao.findChiNhanhList();
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the tai danh sach chi nhanh tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<SportType> getLoaiTheThaoList() {
        try {
            return khuVucDao.findLoaiTheThaoList();
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the tai danh sach loai the thao tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Court> getSanConList(String maKv) {
        try {
            return khuVucDao.findSanConByKhuVuc(maKv);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the tai danh sach san con tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<Path> getAreaImagePath(String maKv) {
        return AreaImageStorage.findImagePath(maKv);
    }

    @Override
    public String generateNextMaKv() {
        try {
            return khuVucDao.generateNextMaKv();
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the sinh ma khu vuc moi: " + exception.getMessage(), exception);
        }
    }

    @Override
    public String getDefaultChiNhanhId() {
        try {
            return khuVucDao.findDefaultChiNhanhId();
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the xac dinh chi nhanh mac dinh: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Path saveAreaImage(String maKv, Path sourceFile) {
        return AreaImageStorage.saveImage(maKv, sourceFile);
    }

    @Override
    public void createKhuVuc(AreaCreateRequest request) {
        try {
            khuVucDao.createKhuVuc(request);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the them khu vuc vao database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void saveKhuVucChanges(AreaUpdateRequest request) {
        try {
            khuVucDao.saveKhuVucChanges(request);
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the luu thay doi khu vuc vao database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void deleteKhuVuc(String maKv) {
        try {
            boolean deleted = khuVucDao.softDeleteById(maKv);
            if (!deleted) {
                throw new IllegalStateException("Khong tim thay khu vuc de xoa: " + maKv);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the xoa khu vuc tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void deleteSanCon(String maSan) {
        try {
            boolean deleted = khuVucDao.softDeleteSanConById(maSan);
            if (!deleted) {
                throw new IllegalStateException("Khong tim thay san con de xoa: " + maSan);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Khong the xoa san con tu database: " + exception.getMessage(), exception);
        }
    }
}
