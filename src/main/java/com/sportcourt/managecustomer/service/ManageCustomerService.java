package com.sportcourt.managecustomer.service;

import com.sportcourt.managecustomer.dto.CreateCustomerRequest;
import com.sportcourt.managecustomer.dto.CustomerProfile;
import com.sportcourt.managecustomer.dto.CustomerResult;
import com.sportcourt.managecustomer.dto.CustomerSummary;
import com.sportcourt.managecustomer.dto.UpdateCustomerRequest;

import java.util.List;

public interface ManageCustomerService {
    CustomerResult<List<CustomerSummary>> searchByName(String keyword);

    CustomerResult<CustomerProfile> getProfile(String maKhachHang);

    CustomerResult<CustomerProfile> createCustomer(CreateCustomerRequest request);

    CustomerResult<CustomerProfile> updateCustomer(String maKhachHang, UpdateCustomerRequest request);

    CustomerResult<Void> softDeleteCustomer(String maKhachHang);
}
