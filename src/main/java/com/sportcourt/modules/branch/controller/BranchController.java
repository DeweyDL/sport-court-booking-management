package com.sportcourt.modules.branch.controller;

import com.sportcourt.modules.branch.dto.BranchCreateRequest;
import com.sportcourt.modules.branch.dto.BranchUpdateRequest;
import com.sportcourt.modules.branch.entity.Branch;
import com.sportcourt.modules.branch.service.BranchService;
import com.sportcourt.modules.branch.service.BranchServiceImpl;

import java.util.List;

public class BranchController {
    private final BranchService branchService;

    public BranchController() {
        this(new BranchServiceImpl());
    }

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    public List<Branch> getBranchList(String keyword) {
        return branchService.getBranchList(keyword);
    }

    public Branch getBranchDetail(String maCn) {
        return branchService.getBranchDetail(maCn);
    }

    public String generateNextMaCn() {
        return branchService.generateNextMaCn();
    }

    public void createBranch(BranchCreateRequest request) {
        branchService.createBranch(request);
    }

    public void saveBranchChanges(BranchUpdateRequest request) {
        branchService.saveBranchChanges(request);
    }

    public void deleteBranch(String maCn) {
        branchService.deleteBranch(maCn);
    }
}

