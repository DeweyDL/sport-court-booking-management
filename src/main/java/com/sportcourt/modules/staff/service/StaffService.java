package com.sportcourt.modules.staff.service;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import java.util.List;

public interface StaffService {
    List<StaffResponse> searchStaff(StaffSearchCriteria criteria) throws Exception;
    void createStaff(StaffCreateRequest req) throws Exception;
    void updateStaff(String manv, StaffUpdateRequest req) throws Exception;
    void deleteStaff(String manv) throws Exception;
    void restoreStaff(String manv) throws Exception;
    String generateNextManv() throws Exception;
}