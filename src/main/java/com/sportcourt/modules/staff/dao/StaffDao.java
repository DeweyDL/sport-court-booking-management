package com.sportcourt.modules.staff.dao;

import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.entity.Staff;
import com.sportcourt.modules.staff.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface StaffDao {
    List<StaffResponse> search(StaffSearchCriteria criteria) throws SQLException;

    Optional<StaffDetailResponse> findDetailById(String maNv) throws SQLException;

    boolean existsByPhone(String sdt, String exceptUserId) throws SQLException;

    boolean existsByEmail(String email, String exceptUserId) throws SQLException;

    boolean existsByCccd(String cccd, String exceptMaNv) throws SQLException;

    boolean existsBranch(String maCn) throws SQLException;

    boolean existsStaffType(String maLoaiNv) throws SQLException;

    void insertUser(Connection conn, User user) throws SQLException;

    void insertStaff(Connection conn, Staff staff) throws SQLException;

    boolean updateUser(Connection conn, User user) throws SQLException;

    boolean updateStaff(Connection conn, Staff staff) throws SQLException;

    boolean softDeleteStaff(Connection conn, String maNv) throws SQLException;
}
