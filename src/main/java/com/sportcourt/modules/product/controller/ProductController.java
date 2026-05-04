package com.sportcourt.modules.product.controller;

import com.sportcourt.modules.product.dto.ProductCreateRequest;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.dto.ProductUpdateRequest;
import com.sportcourt.modules.product.service.ProductService;
import com.sportcourt.modules.product.service.ProductServiceImpl;
import com.sportcourt.modules.product.view.ProductPanel;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ProductController {
    private final ProductPanel view;
    private final ProductService productService;

    public ProductController(ProductPanel view) {
        this(view, new ProductServiceImpl());
    }

    public ProductController(ProductPanel view, ProductService productService) {
        this.view = view;
        this.productService = productService;
        initEvents();
        searchProducts();
    }

    private void initEvents() {
        view.setSearchAction(e -> searchProducts());
        view.setAddAction(e -> addProduct());
        view.setUpdateAction(e -> updateProduct());
        view.setDeleteAction(e -> deleteProduct());
        view.setRestoreAction(e -> restoreProduct());
        view.setRefreshAction(e -> searchProducts());
    }

    private void searchProducts() {
        ProductSearchCriteria criteria = view.getSearchCriteria();
        view.setLoading(true);

        SwingWorker<List<ProductResponse>, Void> worker = new SwingWorker<List<ProductResponse>, Void>() {
            @Override
            protected List<ProductResponse> doInBackground() {
                return productService.searchProducts(criteria);
            }

            @Override
            protected void done() {
                view.setLoading(false);

                try {
                    view.showProductTable(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    view.showProductTable(new ArrayList<ProductResponse>());
                    view.showError("Quá trình tải dữ liệu đã bị gián đoạn.");
                } catch (ExecutionException e) {
                    view.showProductTable(new ArrayList<ProductResponse>());
                    Throwable cause = e.getCause();
                    view.showError(cause == null ? e.getMessage() : cause.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void addProduct() {
        try {
            ProductCreateRequest request = view.showCreateDialog();

            if (request == null) {
                return;
            }

            productService.createProduct(request);
            view.showMessage("Thêm sản phẩm thành công.");
            searchProducts();
        } catch (Exception e) {
            view.showError(getErrorMessage(e, "Không thể thêm sản phẩm."));
        }
    }

    private void updateProduct() {
        try {
            String maSp = view.getSelectedProductId();

            if (maSp == null || maSp.trim().isEmpty()) {
                view.showError("Vui lòng chọn sản phẩm cần chỉnh sửa.");
                return;
            }

            ProductResponse product = productService.getProductDetail(maSp);
            ProductUpdateRequest request = view.showUpdateDialog(product);

            if (request == null) {
                return;
            }

            productService.updateProduct(request);
            view.showMessage("Cập nhật sản phẩm thành công.");
            searchProducts();
        } catch (Exception e) {
            view.showError(getErrorMessage(e, "Không thể cập nhật sản phẩm."));
        }
    }

    private void deleteProduct() {
        try {
            String maSp = view.getSelectedProductId();

            if (maSp == null || maSp.trim().isEmpty()) {
                view.showError("Vui lòng chọn sản phẩm cần xoá.");
                return;
            }

            if (!view.confirm("Bạn có chắc muốn xoá sản phẩm này không?")) {
                return;
            }

            productService.deleteProduct(maSp);
            view.showMessage("Đã xóa sản phẩm.");
            searchProducts();
        } catch (Exception e) {
            view.showError(getErrorMessage(e, "Không thể xoá sản phẩm."));
        }
    }

    private void restoreProduct() {
        try {
            String maSp = view.getSelectedProductId();

            if (maSp == null || maSp.trim().isEmpty()) {
                view.showError("Vui lòng chọn sản phẩm cần khôi phục.");
                return;
            }

            if (!view.confirm("Bạn có chắc muốn khôi phục sản phẩm này không?")) {
                return;
            }

            productService.restoreProduct(maSp);
            view.showMessage("Khôi phục sản phẩm thành công.");
            searchProducts();
        } catch (Exception e) {
            view.showError(getErrorMessage(e, "Không thể khôi phục sản phẩm."));
        }
    }

    private String getErrorMessage(Exception e, String defaultMessage) {
        if (e == null) {
            return defaultMessage;
        }

        if (e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
            return e.getMessage();
        }

        Throwable cause = e.getCause();

        if (cause != null && cause.getMessage() != null && !cause.getMessage().trim().isEmpty()) {
            return cause.getMessage();
        }

        return defaultMessage;
    }
}
