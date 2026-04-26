package com.sportcourt.modules.staff.dao;
import com.sportcourt.modules.staff.entity.Staff;
import com.sportcourt.modules.staff.entity.User;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import java.sql.Connection;
import java.util.List;
import java.util.List;

public class BranchDAO {

    public List<Branch> findAllActive() {
        // SELECT * FROM CHI_NHANH WHERE IS_DELETED = 0
        return null;
    }

    public boolean existsById(String maCn) {
        return false;
    }
}