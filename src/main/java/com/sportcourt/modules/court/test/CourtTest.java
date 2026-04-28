package com.sportcourt.modules.court.test;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.modules.court.view.CourtManagementPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

public class CourtTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            JFrame frame = new JFrame("Court Management Panel Test");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(1280, 820);
            frame.setLocationRelativeTo(null);

            CourtManagementPanel panel = new CourtManagementPanel();
            panel.setCurrentBranchId("CN_TEST_01");

            JPanel root = new JPanel(new BorderLayout(0, 0));
            root.add(createTestToolbar(panel), BorderLayout.NORTH);
            root.add(panel, BorderLayout.CENTER);

            frame.setContentPane(root);
            frame.setVisible(true);
        });
    }

    private static JPanel createTestToolbar(CourtManagementPanel panel) {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel lblBranch = new JLabel("Chi nhánh test:");
        lblBranch.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JComboBox<String> branchBox = new JComboBox<>(new String[]{
                "CN_TEST_01",
                "CN_TEST_02",
                "CN_TEST_03"
        });
        branchBox.setSelectedItem("CN_TEST_01");
        branchBox.addActionListener(event -> {
            String branchId = (String) branchBox.getSelectedItem();
            if (branchId != null) {
                panel.setCurrentBranchId(branchId);
            }
        });

        JButton btnRefresh = new JButton("Reload dữ liệu");
        btnRefresh.setMargin(new Insets(6, 10, 6, 10));
        btnRefresh.addActionListener(event -> panel.refreshData());

        JButton btnGuide = new JButton("Hướng dẫn test");
        btnGuide.setMargin(new Insets(6, 10, 6, 10));
        btnGuide.addActionListener(event -> JOptionPane.showMessageDialog(
                panel,
                """
                Checklist test nhanh Court:
                1) Tìm kiếm theo mã sân/mã khu vực.
                2) Thêm sân con mới bằng nút '+ Thêm sân con'.
                3) Mở chi tiết -> Chỉnh sửa khu vực/trạng thái.
                4) Xóa sân con và kiểm tra danh sách tự reload.
                5) Đổi chi nhánh bằng combobox trên thanh test để test dữ liệu theo chi nhánh.
                """,
                "Hướng dẫn test Court",
                JOptionPane.INFORMATION_MESSAGE
        ));

        left.add(lblBranch);
        left.add(branchBox);
        left.add(btnRefresh);
        left.add(btnGuide);

        toolbar.add(left, BorderLayout.WEST);
        return toolbar;
    }
}