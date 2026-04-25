package com.sportcourt.modules.managecustomer.service;

import com.sportcourt.modules.managecustomer.dto.CreateCustomerRequest;
import com.sportcourt.modules.managecustomer.dto.CustomerProfile;
import com.sportcourt.modules.managecustomer.dto.CustomerResult;
import com.sportcourt.modules.managecustomer.dto.CustomerSummary;
import com.sportcourt.modules.managecustomer.dto.UpdateCustomerRequest;

import java.util.List;

public interface ManageCustomerService {
    CustomerResult<List<CustomerSummary>> searchByName(String keyword);

    CustomerResult<CustomerProfile> getProfile(String maKhachHang);

    CustomerResult<CustomerProfile> createCustomer(CreateCustomerRequest request);

    CustomerResult<CustomerProfile> updateCustomer(String maKhachHang, UpdateCustomerRequest request);

    CustomerResult<Void> softDeleteCustomer(String maKhachHang);

    CustomerResult<Void> restoreCustomer(String maKhachHang);
}
