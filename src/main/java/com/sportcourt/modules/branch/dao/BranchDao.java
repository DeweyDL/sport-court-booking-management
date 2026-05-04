package com.sportcourt.modules.branch.dao;

import com.sportcourt.modules.branch.dto.BranchCreateRequest;
import com.sportcourt.modules.branch.dto.BranchUpdateRequest;
import com.sportcourt.modules.branch.entity.Branch;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BranchDao {
    List<Branch> findByKeyword(String keyword) throws SQLException;

    Optional<Branch> findById(String maCn) throws SQLException;

    String generateNextMaCn() throws SQLException;

    void createBranch(BranchCreateRequest request) throws SQLException;

    void saveBranchChanges(BranchUpdateRequest request) throws SQLException;

    boolean softDeleteById(String maCn) throws SQLException;
}

