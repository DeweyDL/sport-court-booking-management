package com.sportcourt.modules.dashboard.dao;

import com.sportcourt.modules.dashboard.dto.DashboardCourtCard;
import com.sportcourt.modules.dashboard.dto.DashboardSportTypeOption;

import java.sql.SQLException;
import java.util.List;

public interface DashboardDAO {
    List<DashboardCourtCard> searchCourts(String keyword) throws SQLException;

    /**
     * Scan whole system (all branches) and list courts belonging to a sport type.
     *
     * @param sportTypeId LOAI_THE_THAO.MATT
     */
    List<DashboardCourtCard> findCourtsBySportType(String sportTypeId) throws SQLException;

    /**
     * Same as {@link #findCourtsBySportType(String)} but filter by sport type name.
     *
     * @param sportTypeName LOAI_THE_THAO.TEN
     */
    List<DashboardCourtCard> findCourtsBySportTypeName(String sportTypeName) throws SQLException;

    /**
     * Return sport types that currently have at least one non-deleted court in the whole system.
     */
    List<DashboardSportTypeOption> findAvailableSportTypes() throws SQLException;
}
