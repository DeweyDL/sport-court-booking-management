package com.sportcourt.modules.court.dao;

import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CourtDAO {
    List<CourtTableRow> findByCriteria(CourtSearchCriteria criteria) throws SQLException;

    Optional<Court> findByIdInBranch(String courtId, String branchId) throws SQLException;

    Optional<CourtTableRow> findDetail(String courtId, String branchId) throws SQLException;

    boolean existsById(String courtId) throws SQLException;

    boolean areaBelongsToBranch(String areaId, String branchId) throws SQLException;

    boolean hasActiveRental(String courtId, String branchId) throws SQLException;

    List<String> findAreaIdsByBranch(String branchId) throws SQLException;

    void insert(Court court) throws SQLException;

    boolean update(Court court, String branchId) throws SQLException;

    boolean softDelete(String courtId, String branchId) throws SQLException;
}
