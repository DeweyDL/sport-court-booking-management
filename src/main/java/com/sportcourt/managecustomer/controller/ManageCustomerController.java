package com.sportcourt.managecustomer.controller;

import com.sportcourt.managecustomer.dto.CreateCustomerRequest;
import com.sportcourt.managecustomer.dto.CustomerProfile;
import com.sportcourt.managecustomer.dto.CustomerResult;
import com.sportcourt.managecustomer.dto.CustomerSummary;
import com.sportcourt.managecustomer.dto.UpdateCustomerRequest;
import com.sportcourt.managecustomer.service.ManageCustomerService;
import com.sportcourt.managecustomer.service.ManageCustomerServiceImpl;

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
}
