package com.sportcourt.modules.supplier.view;

import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.service.PermissionService;
import com.sportcourt.modules.supplier.controller.SupplierManagementController;
import com.sportcourt.modules.supplier.dao.JdbcSupplierManagementDAO;
import com.sportcourt.modules.supplier.service.SupplierManagementServiceImpl;

import javax.swing.*;

public class DemoSupplier {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chạy Thử - Quản Lý Nhà Cung Cấp");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);

            // No-op PermissionService: bypass permission check khi chạy demo
            PermissionService noOpPermission = new PermissionService() {
                @Override
                public boolean hasPermission(String functionId, PermissionAction action) {
                    return true;
                }

                @Override
                public void requirePermission(String functionId, PermissionAction action) {
                    // no-op: luôn cho phép khi chạy demo
                }

                @Override
                public com.sportcourt.modules.auth.dto.UserSession loadUserSession(String accountId) {
                    return null;
                }
            };

            SupplierManagementServiceImpl service = new SupplierManagementServiceImpl(
                    new JdbcSupplierManagementDAO(), noOpPermission
            );
            SupplierManagementController controller = new SupplierManagementController(service);

            SupplierManagementPanel managementPanel = new SupplierManagementPanel(controller);
            frame.add(managementPanel);
            frame.setVisible(true);
        });
    }
}
