package com.sportcourt.common.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.style.UIScale;
import com.sportcourt.modules.account.view.AccountManagementPanel;
import com.sportcourt.modules.area.view.AreaManagement;
import com.sportcourt.modules.auth.dto.FunctionId;
import com.sportcourt.modules.auth.dto.PermissionAction;
import com.sportcourt.modules.auth.dto.RoleGroupId;
import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.auth.view.LoginScreen;
import com.sportcourt.modules.branch.view.BranchManagement;
import com.sportcourt.modules.cost.view.CostManagement;
import com.sportcourt.modules.court.view.CourtManagementPanel;
import com.sportcourt.modules.customer.view.ManageCustomerScreen;
import com.sportcourt.modules.customer_rank.view.CustomerRankManagement;
import com.sportcourt.modules.equipment.view.EquipmentManagement;
import com.sportcourt.modules.imports.view.ImportManagement;
import com.sportcourt.modules.product.view.ProductPanel;
import com.sportcourt.modules.sport_type.view.ManageSportTypeScreen;
import com.sportcourt.modules.staff.view.StaffPanel;
import com.sportcourt.modules.user_profile.view.UserProfilePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class Sidebar extends JFrame {
    private static final int SIDEBAR_MIN_WIDTH = UIScale.scale(220);
    private static final int SIDEBAR_MAX_WIDTH = UIScale.scale(320);
    private static final double SIDEBAR_WIDTH_RATIO = 0.22;

    private ContentPanel contentPanel;
    private JLabel currentTitleLabel;
    private JPanel menuPanel;
    private JPanel bottomPanel;
    private JPanel sidebarContainer;
    private boolean sidebarVisible = true;

    private final Color SIDEBAR_BG = Color.decode("#2f3c33");
    private final Color SIDEBAR_HOVER_BG = Color.decode("#43464A");
    private final Color TEXT_NORMAL = Color.decode("#B0B3B8");
    private final Color NEON_GREEN = Color.decode("#6af514");
    private final Color TEXT_ACTIVE = Color.decode("#111111");
    private final Color LOGO_COLOR = Color.decode("#39ff14");
    private final Color LOGOUT_RED = Color.decode("#FF4D4D");

    private final UserSession session;

    public Sidebar() {
        this.session = SessionManager.requireSession();
        setTitle("RentSta - Facility Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        sidebarContainer = createSidebar();
        add(sidebarContainer, BorderLayout.WEST);

        contentPanel = new ContentPanel();
        registerModuleViews();
        add(createContentWrapper(), BorderLayout.CENTER);

        applyResponsiveWindowSize();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                updateSidebarWidth();
            }
        });

        // Mặc định chọn Trang Chủ
        SwingUtilities.invokeLater(() -> {
            updateSidebarWidth();
            if (menuPanel.getComponentCount() > 0) {
                JPanel firstWrapper = (JPanel) menuPanel.getComponent(0);
                JButton firstButton = (JButton) firstWrapper.getComponent(0);
                setActiveButton(firstWrapper, firstButton);
                openView("TRANG CHỦ");
            }
        });
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_MAX_WIDTH, 0));

        // --- Logo Area ---
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(10), 0));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(
                UIScale.scale(30), UIScale.scale(20), UIScale.scale(30), UIScale.scale(20)));

        JLabel logoLabel = new JLabel();
        logoLabel.setText("<html><div style='font-family: Lexend; color: " +
                String.format("#%02x%02x%02x", LOGO_COLOR.getRed(), LOGO_COLOR.getGreen(), LOGO_COLOR.getBlue()) +
                "; font-size: " + UIScale.scale(20) + "px; font-weight: 800; font-style: italic;'>" +
                "RENTSTA</div></html>");

        // Load Logo Icon
        try {
            URL imgURL = getClass().getResource("/icon/logo.png");
            if (imgURL != null) {
                int logoSz = UIScale.scale(45);
                ImageIcon icon = new ImageIcon(new ImageIcon(imgURL).getImage()
                        .getScaledInstance(logoSz, logoSz, Image.SCALE_SMOOTH));
                logoLabel.setIcon(icon);
                logoLabel.setIconTextGap(UIScale.scale(12));
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

        if (canView(FunctionId.CUSTOMER_BOOKING_SELF_SERVICE)) menuPanel.add(createMenuButton("ĐẶT SÂN", "/icon/home.1.png"));
        if (canView(FunctionId.BRANCH_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ CHI NHÁNH", "/icon/branch.1.png"));
        if (canView(FunctionId.AREA_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ KHU VỰC", "/icon/branch.1.png"));
        if (canView(FunctionId.COURT_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ SÂN CON", "/icon/branch.1.png"));
        if (canView(FunctionId.PRICE_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ BẢNG GIÁ", "/icon/report.1.png"));
        if (canView(FunctionId.BOOKING_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ ĐẶT SÂN", "/icon/home.1.png"));
        if (canView(FunctionId.SERVICE_MANAGEMENT)) menuPanel.add(createMenuButton("CUNG CẤP DỊCH VỤ", "/icon/products.1.png"));
        if (canView(FunctionId.INVOICE_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ HÓA ĐƠN", "/icon/report.1.png"));
        if (canView(FunctionId.CUSTOMER_MANAGEMENT) && !isCustomerAccount()) {
            menuPanel.add(createMenuButton("QUẢN LÝ KHÁCH HÀNG", "/icon/user.1.png"));
        }
        if (canView(FunctionId.CUSTOMER_RANK_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ HẠNG KHÁCH HÀNG", "/icon/user.1.png"));
        if (canView(FunctionId.EMPLOYEE_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ NHÂN VIÊN", "/icon/staff.1.png"));
        if (canView(FunctionId.PRODUCT_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ SẢN PHẨM", "/icon/products.1.png"));
        if (canView(FunctionId.EQUIPMENT_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ DỤNG CỤ", "/icon/tools.1.png"));
        if (canView(FunctionId.IMPORT_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ NHẬP HÀNG", "/icon/report.1.png"));
        if (canView(FunctionId.SUPPLIER_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ NHÀ CUNG CẤP", "/icon/products.1.png"));
        if (canView(FunctionId.REVENUE_MANAGEMENT)) menuPanel.add(createMenuButton("BÁO CÁO DOANH THU", "/icon/report.1.png"));
        if (canView(FunctionId.SPORT_TYPE_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ LOẠI THỂ THAO", "/icon/tools.1.png"));
        if (canView(FunctionId.ACCOUNT_MANAGEMENT)) menuPanel.add(createMenuButton("QUẢN LÝ TÀI KHOẢN", "/icon/user.1.png"));
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setBorder(BorderFactory.createEmptyBorder());
        menuScrollPane.getViewport().setOpaque(false);
        menuScrollPane.setOpaque(false);
        menuScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sidebar.add(menuScrollPane, BorderLayout.CENTER);

        // --- Bottom Menu ---
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 0, UIScale.scale(20), 0));

        if (canView(FunctionId.PERSONAL_PROFILE_MANAGEMENT)) bottomPanel.add(createMenuButton("TRANG CÁ NHÂN", "/icon/user.1.png"));
        bottomPanel.add(createMenuButton("ĐĂNG XUẤT", "/icon/logout.png"));

        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createContentWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.decode("#F5F7FA"));
        wrapper.add(createTopBar(), BorderLayout.NORTH);
        wrapper.add(contentPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(new EmptyBorder(
                UIScale.scale(10), UIScale.scale(14), UIScale.scale(10), UIScale.scale(14)));

        JButton toggleSidebarButton = new JButton("\u2630");
        toggleSidebarButton.setToolTipText("Ẩn/hiện sidebar");
        toggleSidebarButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, UIScale.scale(16)));
        toggleSidebarButton.setFocusPainted(false);
        toggleSidebarButton.setContentAreaFilled(false);
        toggleSidebarButton.setBorderPainted(false);
        toggleSidebarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleSidebarButton.addActionListener(event -> toggleSidebar());

        currentTitleLabel = new JLabel("TRANG CHỦ");
        currentTitleLabel.setFont(new Font("Lexend", Font.BOLD, UIScale.scale(15)));
        currentTitleLabel.setForeground(new Color(39, 44, 52));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(toggleSidebarButton);
        left.add(currentTitleLabel);

        topBar.add(left, BorderLayout.WEST);
        return topBar;
    }

    private JPanel createMenuButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setFont(new Font("Plus Jakarta Sans", Font.BOLD, UIScale.scale(15)));
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(0, UIScale.scale(30), 0, UIScale.scale(10)));
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
                int iconSz = UIScale.scale(20);
                button.setIcon(new ImageIcon(new ImageIcon(url).getImage()
                        .getScaledInstance(iconSz, iconSz, Image.SCALE_SMOOTH)));
                button.setIconTextGap(UIScale.scale(10));
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
                    g2.fillRoundRect(15, 5, getWidth() - 30, getHeight() - 10, getHeight() - 10, getHeight() - 10);
                } else if (isHover) {
                    g2.setColor(SIDEBAR_HOVER_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };

        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(60)));
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
                openView(text);
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

    private void registerModuleViews() {
        contentPanel.registerView("TRANG CHỦ", () -> createPage("TRANG CHỦ"));
        if (canView(FunctionId.CUSTOMER_BOOKING_SELF_SERVICE)) contentPanel.registerView("ĐẶT SÂN", () -> createPage("ĐẶT SÂN KHÁCH HÀNG"));
        if (canView(FunctionId.BRANCH_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ CHI NHÁNH", BranchManagement::new);
        if (canView(FunctionId.AREA_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ KHU VỰC", AreaManagement::new);
        if (canView(FunctionId.COURT_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ SÂN CON", CourtManagementPanel::new);
        if (canView(FunctionId.PRICE_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ BẢNG GIÁ", CostManagement::new);
        if (canView(FunctionId.BOOKING_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ ĐẶT SÂN", () -> createPage("QUẢN LÝ ĐẶT SÂN"));
        if (canView(FunctionId.SERVICE_MANAGEMENT)) contentPanel.registerView("CUNG CẤP DỊCH VỤ", () -> createPage("CUNG CẤP DỊCH VỤ"));
        if (canView(FunctionId.INVOICE_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ HÓA ĐƠN", () -> createPage("QUẢN LÝ HÓA ĐƠN"));
        if (canView(FunctionId.CUSTOMER_MANAGEMENT) && !isCustomerAccount()) {
            contentPanel.registerView("QUẢN LÝ KHÁCH HÀNG", ManageCustomerScreen::new);
        }
        if (canView(FunctionId.CUSTOMER_RANK_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ HẠNG KHÁCH HÀNG", CustomerRankManagement::new);
        if (canView(FunctionId.EMPLOYEE_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ NHÂN VIÊN", StaffPanel::new);
        if (canView(FunctionId.PRODUCT_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ SẢN PHẨM", ProductPanel::new);
        if (canView(FunctionId.EQUIPMENT_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ DỤNG CỤ", EquipmentManagement::new);
        if (canView(FunctionId.IMPORT_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ NHẬP HÀNG", ImportManagement::new);
        if (canView(FunctionId.SUPPLIER_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ NHÀ CUNG CẤP", () -> createPage("QUẢN LÝ NHÀ CUNG CẤP"));
        if (canView(FunctionId.REVENUE_MANAGEMENT)) contentPanel.registerView("BÁO CÁO DOANH THU", () -> createPage("BÁO CÁO DOANH THU"));
        if (canView(FunctionId.SPORT_TYPE_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ LOẠI THỂ THAO", ManageSportTypeScreen::new);
        if (canView(FunctionId.ACCOUNT_MANAGEMENT)) contentPanel.registerView("QUẢN LÝ TÀI KHOẢN", AccountManagementPanel::new);
        if (canView(FunctionId.PERSONAL_PROFILE_MANAGEMENT)) contentPanel.registerView("TRANG CÁ NHÂN", UserProfilePanel::new);
    }

    private void openView(String key) {
        contentPanel.showView(key);
        if (currentTitleLabel != null) {
            currentTitleLabel.setText(key);
        }
    }

    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        sidebarContainer.setVisible(sidebarVisible);
        if (sidebarVisible) {
            updateSidebarWidth();
        }
        revalidate();
        repaint();
    }

    private void applyResponsiveWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.max(1200, (int) (screenSize.width * 0.92));
        int height = Math.max(700, (int) (screenSize.height * 0.9));
        setSize(Math.min(width, screenSize.width), Math.min(height, screenSize.height));
        setLocationRelativeTo(null);
    }

    private void updateSidebarWidth() {
        if (sidebarContainer == null) {
            return;
        }
        int frameWidth = getWidth();
        int targetWidth = (int) (frameWidth * SIDEBAR_WIDTH_RATIO);
        int resolvedWidth = Math.max(SIDEBAR_MIN_WIDTH, Math.min(SIDEBAR_MAX_WIDTH, targetWidth));
        sidebarContainer.setPreferredSize(new Dimension(resolvedWidth, 0));
        sidebarContainer.revalidate();
    }

    private boolean canView(String functionId) {
        return session.hasPermission(functionId, PermissionAction.VIEW);
    }

    private boolean isCustomerAccount() {
        return session.getRoleGroups().contains(RoleGroupId.CUSTOMER) && !session.isOwner();
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new Sidebar().setVisible(true));

    }
}
