package com.sportcourt.modules.customer.service;

import com.sportcourt.modules.customer.dto.CreateCustomerRequest;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerResult;
import com.sportcourt.modules.customer.dto.CustomerSummary;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;

import java.util.List;

public interface ManageCustomerService {
    CustomerResult<List<CustomerSummary>> searchByName(String keyword);

    CustomerResult<CustomerProfile> getProfile(String maKhachHang);

    CustomerResult<String> generateNextMaKhachHang();

    CustomerResult<CustomerProfile> createCustomer(CreateCustomerRequest request);

    CustomerResult<CustomerProfile> updateCustomer(String maKhachHang, UpdateCustomerRequest request);

    CustomerResult<Void> softDeleteCustomer(String maKhachHang);

    CustomerResult<Void> restoreCustomer(String maKhachHang);
}

