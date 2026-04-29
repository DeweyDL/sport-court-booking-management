package com.sportcourt.modules.staff.service;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import java.util.List;

public interface StaffService {
    List<StaffResponse> searchStaff(StaffSearchCriteria criteria);

    StaffDetailResponse getStaffDetail(String maNv);

    void createStaff(StaffCreateRequest request);

    void updateStaff(StaffUpdateRequest request);

    void deleteStaff(String maNv);
}
