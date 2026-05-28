package com.sportcourt.modules.area.service;

import com.sportcourt.modules.area.dao.AreaDao;
import com.sportcourt.modules.area.dao.JdbcAreaDao;
import com.sportcourt.modules.area.dto.AreaCreateRequest;
import com.sportcourt.modules.area.dto.AreaUpdateRequest;
import com.sportcourt.modules.area.entity.Area;

import java.sql.SQLException;
import java.util.List;

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
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the tai du lieu khu vuc tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Area getKhuVucDetail(String maKv) {
        try {
            return khuVucDao.findById(maKv)
                    .orElseThrow(() -> new IllegalStateException("Khong tim thay khu vuc: " + maKv));
        } catch (SQLException exception) {
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the tai chi tiet khu vuc tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Area.ChiNhanhOption> getChiNhanhList() {
        try {
            return khuVucDao.findChiNhanhList();
        } catch (SQLException exception) {
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the tai danh sach chi nhanh tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public List<Area.SportTypeOption> getLoaiTheThaoList() {
        try {
            return khuVucDao.findLoaiTheThaoList();
        } catch (SQLException exception) {
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the tai danh sach loai the thao tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public String generateNextMaKv() {
        try {
            return khuVucDao.generateNextMaKv();
        } catch (SQLException exception) {
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the sinh ma khu vuc moi: " + exception.getMessage(), exception);
        }
    }

    @Override
    public String getDefaultChiNhanhId() {
        try {
            return khuVucDao.findDefaultChiNhanhId();
        } catch (SQLException exception) {
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the xac dinh chi nhanh mac dinh: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void createKhuVuc(AreaCreateRequest request) {
        if (request.soLuongSan() > 0) {
            throw new IllegalStateException("Chua lam chuc nang lien quan den san con, chua the tao khu vuc kem san con.");
        }

        try {
            khuVucDao.createKhuVuc(request);
        } catch (SQLException exception) {
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the them khu vuc vao database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void saveKhuVucChanges(AreaUpdateRequest request) {
        try {
            khuVucDao.saveKhuVucChanges(request);
        } catch (SQLException exception) {
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the luu thay doi khu vuc vao database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void deleteKhuVuc(String maKv) {
        try {
            Area area = khuVucDao.findById(maKv)
                    .orElseThrow(() -> new IllegalStateException("Khong tim thay khu vuc de xoa: " + maKv));

            // TODO san con: khi module san con hoan thien, kiem tra/xu ly san con truoc khi xoa khu vuc.
            // boolean hasActiveSanCon = khuVucDao.existsActiveSanConByKhuVuc(maKv);
            if (area.soLuongSan() > 0) {
                throw new IllegalStateException("Chua lam chuc nang lien quan den san con, chua the xoa khu vuc dang co san con.");
            }

            boolean deleted = khuVucDao.softDeleteById(maKv);
            if (!deleted) {
                throw new IllegalStateException("Khong tim thay khu vuc de xoa: " + maKv);
            }
        } catch (SQLException exception) {
            exception.printStackTrace(); // Re-added for debugging
            throw new IllegalStateException("Khong the xoa khu vuc tu database: " + exception.getMessage(), exception);
        }
    }
}
