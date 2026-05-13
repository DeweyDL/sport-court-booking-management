package com.sportcourt.modules.customer.controller;

import com.sportcourt.modules.customer.dto.CreateCustomerRequest;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerResult;
import com.sportcourt.modules.customer.dto.CustomerSummary;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;
import com.sportcourt.modules.customer.service.ManageCustomerService;
import com.sportcourt.modules.customer.service.ManageCustomerServiceImpl;

import java.util.List;

public class ManageCustomerController {
    private final ManageCustomerService manageCustomerService;

    public ManageCustomerController() {
        this(new ManageCustomerServiceImpl());
    }

    public ManageCustomerController(ManageCustomerService manageCustomerService) {
        this.manageCustomerService = manageCustomerService;
    }

    public CustomerResult<List<CustomerSummary>> searchByName(String keyword) {
        return manageCustomerService.searchByName(keyword);
    }

    public CustomerResult<CustomerProfile> getProfile(String maKhachHang) {
        return manageCustomerService.getProfile(maKhachHang);
    }

    public CustomerResult<CustomerProfile> create(CreateCustomerRequest request) {
        return manageCustomerService.createCustomer(request);
    }

    public CustomerResult<CustomerProfile> update(String maKhachHang, UpdateCustomerRequest request) {
        return manageCustomerService.updateCustomer(maKhachHang, request);
    }

    public CustomerResult<Void> softDelete(String maKhachHang) {
        return manageCustomerService.softDeleteCustomer(maKhachHang);
    }

    public CustomerResult<Void> restore(String maKhachHang) {
        return manageCustomerService.restoreCustomer(maKhachHang);
    }
}

