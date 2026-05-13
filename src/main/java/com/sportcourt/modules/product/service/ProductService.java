package com.sportcourt.modules.product.service;

import com.sportcourt.modules.product.dto.ProductCreateRequest;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.dto.ProductUpdateRequest;

import java.util.List;

public interface ProductService {
    List<ProductResponse> searchProducts(ProductSearchCriteria criteria);

    ProductResponse getProductDetail(String maSp);

    void createProduct(ProductCreateRequest request);

    void updateProduct(ProductUpdateRequest request);

    void deleteProduct(String maSp);

    void restoreProduct(String maSp);
    String generateNextMaSp();
}
