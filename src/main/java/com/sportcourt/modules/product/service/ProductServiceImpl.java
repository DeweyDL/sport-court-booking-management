package com.sportcourt.modules.product.service;

import com.sportcourt.common.db.ConnectionUtils;
import com.sportcourt.modules.product.dao.JdbcProductDao;
import com.sportcourt.modules.product.dao.ProductDao;
import com.sportcourt.modules.product.dto.ProductCreateRequest;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.dto.ProductUpdateRequest;
import com.sportcourt.modules.product.entity.Product;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao;

    public ProductServiceImpl() {
        this(new JdbcProductDao());
    }

    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public List<ProductResponse> searchProducts(ProductSearchCriteria criteria) {
        try {
            return productDao.search(criteria);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi tải danh sách sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductResponse getProductDetail(String maSp) {
        if (isBlank(maSp)) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm.");
        }

        try {
            Optional<ProductResponse> product = productDao.findById(maSp.trim());

            if (product.isPresent()) {
                return product.get();
            }

            throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi tải thông tin sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public void createProduct(ProductCreateRequest request) {
        validateCreateRequest(request);

        try {
            Optional<ProductResponse> existing = productDao.findById(request.getMaSp().trim());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Mã sản phẩm đã tồn tại.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi kiểm tra mã sản phẩm: " + e.getMessage(), e);
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            Product product = new Product();
            product.setMaSp(request.getMaSp().trim());
            product.setTenSp(request.getTenSp().trim());
            product.setDvt(request.getDvt().trim());
            product.setGia(request.getGia());
            product.setSlTon(1);
            product.setDeleted(false);

            productDao.insert(conn, product);

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi thêm sản phẩm: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void updateProduct(ProductUpdateRequest request) {
        validateUpdateRequest(request);

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            Product product = new Product();
            product.setMaSp(request.getMaSp().trim());
            product.setTenSp(request.getTenSp().trim());
            product.setDvt(request.getDvt().trim());
            product.setGia(request.getGia());
            product.setSlTon(request.getSlTon());

            boolean updated = productDao.update(conn, product);

            if (!updated) {
                throw new IllegalArgumentException("Không thể cập nhật sản phẩm.");
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi cập nhật sản phẩm: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void deleteProduct(String maSp) {
        if (isBlank(maSp)) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần xoá.");
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            boolean deleted = productDao.softDelete(conn, maSp.trim());

            if (!deleted) {
                throw new IllegalArgumentException("Không thể xoá sản phẩm.");
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi xoá sản phẩm: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    @Override
    public void restoreProduct(String maSp) {
        if (isBlank(maSp)) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần khôi phục.");
        }

        Connection conn = null;

        try {
            conn = ConnectionUtils.getMyConnection();
            conn.setAutoCommit(false);

            boolean restored = productDao.restore(conn, maSp.trim());

            if (!restored) {
                throw new IllegalArgumentException("Không thể khôi phục sản phẩm.");
            }

            conn.commit();
        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            throw new RuntimeException("Lỗi SQL khi khôi phục sản phẩm: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn);
        }
    }

    private void validateCreateRequest(ProductCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu sản phẩm không hợp lệ.");
        }

        if (isBlank(request.getMaSp())) {
            throw new IllegalArgumentException("Vui lòng nhập mã sản phẩm.");
        }

        if (request.getMaSp().trim().length() > 20) {
            throw new IllegalArgumentException("Mã sản phẩm tối đa 20 ký tự.");
        }

        validateCommon(request.getTenSp(), request.getDvt(), request.getGia());
    }

    private void validateUpdateRequest(ProductUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu sản phẩm không hợp lệ.");
        }

        if (isBlank(request.getMaSp())) {
            throw new IllegalArgumentException("Thiếu mã sản phẩm.");
        }

        validateCommon(request.getTenSp(), request.getDvt(), request.getGia());
    }

    private void validateCommon(String tenSp, String dvt, BigDecimal gia) {
        if (isBlank(tenSp)) {
            throw new IllegalArgumentException("Vui lòng nhập tên sản phẩm.");
        }

        if (tenSp.trim().length() > 100) {
            throw new IllegalArgumentException("Tên sản phẩm tối đa 100 ký tự.");
        }

        if (isBlank(dvt)) {
            throw new IllegalArgumentException("Vui lòng nhập đơn vị tính/danh mục.");
        }

        if (dvt.trim().length() > 20) {
            throw new IllegalArgumentException("Đơn vị tính/danh mục tối đa 20 ký tự.");
        }

        if (gia == null || gia.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn 0.");
        }
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }

        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
