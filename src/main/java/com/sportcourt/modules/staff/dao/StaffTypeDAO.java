package com.sportcourt.modules.staff.dao;
import com.sportcourt.modules.staff.entity.StaffType; // (hoặc Branch)
import java.util.List;

public class StaffTypeDAO {

    public List<StaffType> findAllActive() {
        // SELECT * FROM LOAI_NHAN_VIEN WHERE IS_DELETED = 0
        return null;
    }

    public boolean existsById(String maLoaiNv) {
        return false;
    }
}