package com.sportcourt.modules.court.dao;

import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.entity.Court;

import java.sql.SQLException;
import java.util.Optional;

public interface CourtDAO {
    Optional<Object> findByCriteria(CourtSearchCriteria criteria) throws SQLException;

    Optional<Court> findIdByInBranch(String courtId, String branchId) throws SQLException;

    boolean existsById(String courtId) throws SQLException;

    boolean areaBelongToBranch(String AreaId, String branchId) throws SQLException;

    boolean hasActiveRental(String courtId, String branchId) throws SQLException;

    void insert(Court court) throws SQLException;

    boolean update(Court court, String branchId) throws SQLException;

    boolean softDelete(String courtId, String branchId) throws SQLException;
}
