package com.sportcourt.modules.branch.service;

import com.sportcourt.modules.branch.dao.BranchDao;
import com.sportcourt.modules.branch.dao.JdbcBranchDao;
import com.sportcourt.modules.branch.dto.BranchCreateRequest;
import com.sportcourt.modules.branch.dto.BranchUpdateRequest;
import com.sportcourt.modules.branch.entity.Branch;

import java.sql.SQLException;
import java.util.List;

public class BranchServiceImpl implements BranchService {
    private final BranchDao branchDao;

    public BranchServiceImpl() {
        this(new JdbcBranchDao());
    }

    public BranchServiceImpl(BranchDao branchDao) {
        this.branchDao = branchDao;
    }

    @Override
    public List<Branch> getBranchList(String keyword) {
        try {
            return branchDao.findByKeyword(keyword);
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Khong the tai du lieu chi nhanh tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Branch getBranchDetail(String maCn) {
        try {
            return branchDao.findById(maCn)
                    .orElseThrow(() -> new IllegalStateException("Khong tim thay chi nhanh: " + maCn));
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Khong the tai chi tiet chi nhanh tu database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public String generateNextMaCn() {
        try {
            return branchDao.generateNextMaCn();
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Khong the sinh ma chi nhanh moi: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void createBranch(BranchCreateRequest request) {
        try {
            branchDao.createBranch(request);
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Khong the them chi nhanh vao database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void saveBranchChanges(BranchUpdateRequest request) {
        try {
            branchDao.saveBranchChanges(request);
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Khong the luu thay doi chi nhanh vao database: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void deleteBranch(String maCn) {
        try {
            // Note: CHI_NHANH is referenced by KHU_VUC/NHAN_VIEN/... in schema.
            // We keep behavior consistent with other modules: soft delete + let DB FK constraint guard integrity.
            Branch branch = branchDao.findById(maCn)
                    .orElseThrow(() -> new IllegalStateException("Khong tim thay chi nhanh de xoa: " + maCn));

            boolean deleted = branchDao.softDeleteById(branch.maCn());
            if (!deleted) {
                throw new IllegalStateException("Khong tim thay chi nhanh de xoa: " + maCn);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Khong the xoa chi nhanh tu database: " + exception.getMessage(), exception);
        }
    }
}

