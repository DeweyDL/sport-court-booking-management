package com.sportcourt.modules.bill.service;

import com.sportcourt.modules.bill.dto.BillDetail;
import com.sportcourt.modules.bill.dto.BillResult;
import com.sportcourt.modules.bill.dto.BillSummary;
import com.sportcourt.modules.bill.dto.ServiceItem;

import java.util.List;

public interface ManageBillService {
    BillResult<List<BillSummary>> searchBills(String keyword);
    BillResult<BillDetail> getDetail(String maHD);
    BillResult<Void> markAsPaid(String maHD);
    BillResult<Void> cancelBill(String maHD);
    BillResult<Void> softDelete(String maHD);
    BillResult<String> createEmptyBill(String maKH, String maNV);
    BillResult<Void> addServiceItems(String maHD, List<ServiceItem> items);
}
