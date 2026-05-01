package com.sportcourt.common.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.modules.area.view.AreaManagement;
import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.auth.view.LoginScreen;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class Sidebar extends JFrame {

    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JPanel menuPanel;
    private JPanel bottomPanel;

    private final Color SIDEBAR_BG = Color.decode("#2f3c33");
    private final Color SIDEBAR_HOVER_BG = Color.decode("#43464A");
    private final Color TEXT_NORMAL = Color.decode("#B0B3B8");
    private final Color NEON_GREEN = Color.decode("#99E828");
    private final Color TEXT_ACTIVE = Color.decode("#111111");
    private final Color LOGO_COLOR = Color.decode("#39ff14");
    private final Color LOGOUT_RED = Color.decode("#FF4D4D");

    private final UserSession session;

    public Sidebar() {
        this.session = SessionManager.requireSession();
        setTitle("RentSta - Facility Management");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Color.decode("#F5F7FA"));

        // Thêm các trang tương ứng
        mainContentPanel.add(createPage("TRANG CHỦ"), "TRANG CHỦ");
        mainContentPanel.add(createPage("BÁO CÁO DOANH THU"), "BÁO CÁO DOANH THU");
        mainContentPanel.add(new AreaManagement(), "QUẢN LÝ CHI NHÁNH");
        mainContentPanel.add(createPage("QUẢN LÝ KHÁCH HÀNG"), "QUẢN LÝ KHÁCH HÀNG");
        mainContentPanel.add(createPage("QUẢN LÝ DỤNG CỤ"), "QUẢN LÝ DỤNG CỤ");
        mainContentPanel.add(createPage("QUẢN LÝ SẢN PHẨM DỊCH VỤ"), "QUẢN LÝ SẢN PHẨM DỊCH VỤ");
        mainContentPanel.add(createPage("QUẢN LÝ NHÂN VIÊN"), "QUẢN LÝ NHÂN VIÊN");


        add(mainContentPanel, BorderLayout.CENTER);

        // Mặc định chọn Trang Chủ
        SwingUtilities.invokeLater(() -> {
            if (menuPanel.getComponentCount() > 0) {
                JPanel firstWrapper = (JPanel) menuPanel.getComponent(0);
                JButton firstButton = (JButton) firstWrapper.getComponent(0);
                setActiveButton(firstWrapper, firstButton);
                cardLayout.show(mainContentPanel, "TRANG CHỦ");
            }
        });
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(320, 0));

        // --- Logo Area ---
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel logoLabel = new JLabel();
        // Text HTML: Lexend, Extrabold, Italic
        logoLabel.setText("<html><div style='font-family: Lexend; color: " +
                String.format("#%02x%02x%02x", LOGO_COLOR.getRed(), LOGO_COLOR.getGreen(), LOGO_COLOR.getBlue()) +
                "; font-size: 20px; font-weight: 800; font-style: italic;'>" +
                "RENTSTA</div></html>");

        // Load Logo Icon
        try {
            URL imgURL = getClass().getResource("/icon/logo.png");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage()
                        .getScaledInstance(45, 45, Image.SCALE_SMOOTH));
                logoLabel.setIcon(icon);
                logoLabel.setIconTextGap(12);
            }
        } catch (Exception e) {
            System.err.println("Logo not found");
        }

        logoPanel.add(logoLabel);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // --- Main Menu ---
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);

        menuPanel.add(createMenuButton("TRANG CHỦ", "/icon/home.1.png"));

        if (canView("REPORT_MANAGEMENT")) {
            menuPanel.add(createMenuButton("BÁO CÁO DOANH THU", "/icon/report.1.png"));
        }

        if (canView("BRANCH_MANAGEMENT")) {
            menuPanel.add(createMenuButton("QUẢN LÝ CHI NHÁNH", "/icon/branch.1.png"));
        }

        if (canView("CUSTOMER_MANAGEMENT")) {
            menuPanel.add(createMenuButton("QUẢN LÝ KHÁCH HÀNG", "/icon/user.1.png"));
        }

        if (canView("EQUIPMENT_MANAGEMENT")) {
            menuPanel.add(createMenuButton("QUẢN LÝ DỤNG CỤ", "/icon/tools.1.png"));
        }

        if (canView("PRODUCT_MANAGEMENT")) {
            menuPanel.add(createMenuButton("QUẢN LÝ SẢN PHẨM DỊCH VỤ", "/icon/products.1.png"));
        }

        if (canView("EMPLOYEE_MANAGEMENT")) {
            menuPanel.add(createMenuButton("QUẢN LÝ NHÂN VIÊN", "/icon/staff.1.png"));
        }
        sidebar.add(menuPanel, BorderLayout.CENTER);

        // --- Bottom Menu ---
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        bottomPanel.add(createMenuButton("TRANG CÁ NHÂN", "/icon/user.1.png"));
        bottomPanel.add(createMenuButton("ĐĂNG XUẤT", "/icon/logout.png"));

        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createMenuButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Plus Jakarta Sans", Font.BOLD, 15));
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(12, 30, 12, 10));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Màu sắc mặc định
        if (text.equals("ĐĂNG XUẤT")) {
            button.setForeground(LOGOUT_RED);
        } else {
            button.setForeground(TEXT_NORMAL);
        }

        // Icon
        try {
            URL url = getClass().getResource(iconPath);
            if (url != null) {
                button.setIcon(new ImageIcon(new ImageIcon(url).getImage()
                        .getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
                button.setIconTextGap(10);
            }
        } catch (Exception e) {
        }

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                // Nếu là ĐĂNG XUẤT thì không vẽ nền bao quanh (Wrap)
                if (text.equals("ĐĂNG XUẤT")) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = Boolean.TRUE.equals(getClientProperty("isActive"));
                boolean isHover = Boolean.TRUE.equals(getClientProperty("isHover"));

                if (isActive) {
                    g2.setColor(NEON_GREEN);
                    g2.fillRoundRect(15, 5, getWidth() - 30, getHeight() - 5, getHeight() - 10, getHeight() - 10);
                } else if (isHover) {
                    g2.setColor(SIDEBAR_HOVER_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };

        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        wrapper.add(button, BorderLayout.CENTER);

        button.addActionListener(e -> {
            if (text.equals("ĐĂNG XUẤT")) {
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    SessionManager.clear();
                    dispose();
                    new LoginScreen().setVisible(true);
                }
            } else {
                setActiveButton(wrapper, button);
                cardLayout.show(mainContentPanel, text);
            }
        });

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!text.equals("ĐĂNG XUẤT")) {
                    wrapper.putClientProperty("isHover", true);
                    wrapper.repaint();
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                wrapper.putClientProperty("isHover", false);
                wrapper.repaint();
            }
        });

        return wrapper;
    }

    private void setActiveButton(JPanel activeWrapper, JButton activeButton) {
        Component[][] sections = {menuPanel.getComponents(), bottomPanel.getComponents()};
        for (Component[] section : sections) {
            for (Component comp : section) {
                if (comp instanceof JPanel) {
                    JPanel w = (JPanel) comp;
                    JButton b = (JButton) w.getComponent(0);
                    w.putClientProperty("isActive", false);
                    w.repaint();
                    // Reset màu chữ (trừ nút Đăng xuất)
                    if (!b.getText().equals("ĐĂNG XUẤT")) {
                        b.setForeground(TEXT_NORMAL);
                    }
                }
            }
        }
        activeWrapper.putClientProperty("isActive", true);
        activeWrapper.repaint();
        activeButton.setForeground(TEXT_ACTIVE);
    }


    private JPanel createPage(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.add(new JLabel("<html><h1 style='color:#333; font-family: Lexend;'>" + title + "</h1></html>"));
        return panel;
    }

    private boolean canView(String functionId) {
        return session.hasPermission(functionId, PermissionAction.VIEW);
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new Sidebar().setVisible(true));
    }
}