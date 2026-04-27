package com.sportcourt.modules.court.service;

import com.sportcourt.modules.court.dto.CourtSearchCriteria;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CourtService {
    List<CourtTableRow> search(CourtSearchCriteria criteria) throws SQLException;

    Optional<CourtTableRow> findDetail(String courtId, String branchId) throws SQLException;

    void create(Court court, String branchId) throws SQLException;

    void update(Court court, String branchId) throws SQLException;

    void delete(String courtId, String branchId) throws SQLException;
}
