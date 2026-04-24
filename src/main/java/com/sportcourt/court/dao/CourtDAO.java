package com.sportcourt.court.dao;

import com.sportcourt.court.dto.CourtSearchCriteria;
import com.sportcourt.court.dto.CourtTableRow;
import com.sportcourt.court.entity.Court;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CourtDAO {
    List<CourtTableRow> findByCriteria(CourtSearchCriteria criteria) throws SQLException;

    Optional<Court> findIdByInBranch(String courtId, String branchId) throws SQLException;

    boolean existsById(String courtId) throws SQLException;

    boolean areaBelongToBranch(String AreaId, String branchId) throws SQLException;

    boolean hasActiveRental(String courtId, String branchId) throws SQLException;

    void insert(Court court) throws SQLException;

    boolean update(Court court, String branchId) throws SQLException;

    boolean softDelete(String courtId, String branchId) throws SQLException;
}
