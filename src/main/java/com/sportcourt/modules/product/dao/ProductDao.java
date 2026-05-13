package com.sportcourt.modules.product.dao;

import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.entity.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    List<ProductResponse> search(ProductSearchCriteria criteria) throws SQLException;

    Optional<ProductResponse> findById(String maSp) throws SQLException;

    boolean existsByName(String tenSp, String exceptMaSp) throws SQLException;

    void insert(Connection conn, Product product) throws SQLException;

    boolean update(Connection conn, Product product) throws SQLException;

    boolean softDelete(Connection conn, String maSp) throws SQLException;

    boolean restore(Connection conn, String maSp) throws SQLException;
    String generateNextMaSp() throws SQLException;
}
