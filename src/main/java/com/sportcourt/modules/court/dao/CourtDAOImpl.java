package com.sportcourt.modules.court.dao;

import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CourtDAOImpl implements CourtDAO {
    @Override
    public List<CourtTableRow> findByCriteria(CourtSearchCriteria criteria) throws SQLException {

        return List.of();
    }

    @Override
    public Optional<Court> findIdByInBranch(String courtId, String branchId) throws SQLException {
        return Optional.empty();
    }

    @Override
    public boolean existsById(String courtId) throws SQLException {
        return false;
    }

    @Override
    public boolean areaBelongToBranch(String AreaId, String branchId) throws SQLException {
        return false;
    }

    @Override
    public boolean hasActiveRental(String courtId, String branchId) throws SQLException {
        return false;
    }

    @Override
    public void insert(Court court) throws SQLException {

    }

    @Override
    public boolean update(Court court, String branchId) throws SQLException {
        return false;
    }

    @Override
    public boolean softDelete(String courtId, String branchId) throws SQLException {
        return false;
    }
}
