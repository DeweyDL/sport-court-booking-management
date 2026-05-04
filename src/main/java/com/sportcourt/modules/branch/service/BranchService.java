package com.sportcourt.modules.branch.service;

import com.sportcourt.modules.branch.dto.BranchCreateRequest;
import com.sportcourt.modules.branch.dto.BranchUpdateRequest;
import com.sportcourt.modules.branch.entity.Branch;

import java.util.List;

public interface BranchService {
    List<Branch> getBranchList(String keyword);

    Branch getBranchDetail(String maCn);

    String generateNextMaCn();

    void createBranch(BranchCreateRequest request);

    void saveBranchChanges(BranchUpdateRequest request);

    void deleteBranch(String maCn);
}

