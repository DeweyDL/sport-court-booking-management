package com.sportcourt.modules.auth.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.common.ui.Sidebar;
import com.sportcourt.modules.auth.controller.AuthController;
import com.sportcourt.modules.auth.dto.AuthResult;
import com.sportcourt.modules.auth.dto.LoginRequest;
import com.sportcourt.common.style.AppDialog;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.BackgroundPanel;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class LoginScreen extends JFrame {
    private static final double WINDOW_WIDTH_RATIO = 0.9;
    private static final double WINDOW_HEIGHT_RATIO = 0.9;
    private static final int MIN_WINDOW_WIDTH = UIScale.scale(1100);
    private static final int MIN_WINDOW_HEIGHT = UIScale.scale(680);
    private static final double LEFT_PANEL_WEIGHT = 1.0;
    private static final double RIGHT_PANEL_WEIGHT = 1.0;
    private static final Dimension LOGIN_FIELD_SIZE = new Dimension(UIScale.scale(300), UIScale.scale(44));

    private final AuthController authController = new AuthController();

    // Khởi tạo các thành phần để tạo CardLayout
    private CardLayout cardLayout = new CardLayout();
    private JPanel rightPanel = new JPanel(cardLayout);

    public LoginScreen() {
        AppFonts.register();
        setTitle("RentSta Login");
        applyResponsiveWindowSize();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        rightPanel.setBackground(new Color(242, 242, 242));

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1;

        // PANEL BÊN TRÁI
        // Use COVER so left background fully fills its side without distortion.
        BackgroundPanel leftPanel = new BackgroundPanel("/image/bg.png", BackgroundPanel.ScaleMode.COVER);
        leftPanel.setLayout(new GridBagLayout());


        // --- BẮT ĐẦU PHẦN NỘI DUNG LOGIN (CARD 1) ---
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(new Color(242, 242, 242));
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(
                UIScale.scale(32), UIScale.scale(24), UIScale.scale(32), UIScale.scale(24)));

        // Giữ nguyên các thông số r của bạn
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(UIScale.scale(8), UIScale.scale(24), UIScale.scale(8), UIScale.scale(24));
        r.gridx = 0;
        r.weightx = 1;
        r.weighty = 0;
        r.fill = GridBagConstraints.HORIZONTAL;
        r.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("CHÀO MỪNG TRỞ LẠI!");
        title.setFont(AppFonts.lexendBold(34f));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("Vui lòng điền thông tin tài khoản của bạn.");
        subtitle.setFont(AppFonts.lexendRegular(16f));
        subtitle.setForeground(new Color(120, 120, 120));

        JTextField phoneField = new JTextField();
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(20)));
        phoneField.setBackground(new Color(242, 242, 242));
        phoneField.setBorder(null);
        phoneField.putClientProperty("JTextField.placeholderText", "Số điện thoại");

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(242, 242, 242));
        userPanel.setBorder(AuthViewStyle.createInputUnderlineBorder());
        userPanel.setPreferredSize(LOGIN_FIELD_SIZE);

        JLabel userIcon = new JLabel(scaleIcon("/icon/user.png", UIScale.scale(18), UIScale.scale(18)));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(5), 0, UIScale.scale(5)));
        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(phoneField, BorderLayout.CENTER);

        JPasswordField password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(20)));
        password.setBackground(new Color(242, 242, 242));
        password.setBorder(null);
        password.putClientProperty("JTextField.placeholderText", "Mật khẩu");

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(new Color(242, 242, 242));
        passPanel.setBorder(AuthViewStyle.createInputUnderlineBorder());
        passPanel.setPreferredSize(LOGIN_FIELD_SIZE);

        JLabel passIcon = new JLabel(scaleIcon("/icon/pass.png", UIScale.scale(18), UIScale.scale(18)));
        passIcon.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(5), 0, UIScale.scale(5)));
        passPanel.add(passIcon, BorderLayout.WEST);
        passPanel.add(password, BorderLayout.CENTER);

        JLabel forgot = new JLabel("Quên mật khẩu?");
        forgot.setFont(AppFonts.lexendBold(20f));
        forgot.setForeground(new Color(58, 134, 45));
        forgot.setHorizontalAlignment(SwingConstants.RIGHT);
        forgot.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton loginBtn = new JButton("Đăng nhập") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(111, 240, 36));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFont(AppFonts.lexendBold(20f));
        loginBtn.setFocusPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setPreferredSize(new Dimension(200, UIScale.scale(50)));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(
                "<html><div style='font-size:12px;'>" +
                        "Bạn chưa có tài khoản? <font color='#6C757D'> </font>" +
                        "<font color='#3a862d'><b>Đăng kí ngay</b></font>" +
                        "</div></html>"
        );
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // SỬA: Chuyển sang Card Register thay vì mở Frame mới
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(rightPanel, "REGISTER");
            }
        });

        // SỬA: Chuyển sang Card Forgot thay vì mở Frame mới
        forgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(rightPanel, "FORGOT");
            }
        });

        Runnable doLogin = () -> {
            String phoneValue = phoneField.getText().trim();
            String passwordValue = new String(password.getPassword());
            AuthResult result = authController.login(new LoginRequest(phoneValue, passwordValue));
            if (result.success()) {
                AppDialog.showInfo(this, result.message());
                new Sidebar().setVisible(true);
                dispose();
            } else {
                AppDialog.showError(this, result.message());
            }
        };

        loginBtn.addActionListener(e -> doLogin.run());
        password.addActionListener(e -> doLogin.run());

        r.gridy = 1;
        loginPanel.add(title, r);
        r.gridy++;
        loginPanel.add(subtitle, r);
        r.gridy++;
        loginPanel.add(userPanel, r);
        r.gridy++;
        loginPanel.add(passPanel, r);
        r.gridy++;
        loginPanel.add(forgot, r);
        r.gridy++;
        loginPanel.add(loginBtn, r);
        r.gridy++;
        loginPanel.add(label, r);
        // --- KẾT THÚC PHẦN NỘI DUNG LOGIN ---

        // Thêm các "Thẻ" vào rightPanel
        rightPanel.add(loginPanel, "LOGIN");

        rightPanel.add(new Register(cardLayout, rightPanel), "REGISTER");

        rightPanel.add(new ForgotPassword(cardLayout, rightPanel), "FORGOT");

        // Single scaling path for all auth-screen fonts
        CrudViewStyle.installResponsiveTypography(rightPanel);

        // Layout JFrame chính (Giữ nguyên weightx của bạn)
        gbc.gridx = 0;
        gbc.weightx = LEFT_PANEL_WEIGHT;
        add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = RIGHT_PANEL_WEIGHT;
        add(rightPanel, gbc);
    }

    // Hàm phụ tạo panel placeholder để bạn không bị lỗi khi bấm chuyển trang
    private JPanel createPlaceholderPanel(String text) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(242, 242, 242));
        JButton backBtn = new JButton("Quay lại Đăng nhập");
        backBtn.addActionListener(e -> cardLayout.show(rightPanel, "LOGIN"));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        p.add(new JLabel(text), c);
        c.gridy = 1;
        c.insets = new Insets(20, 0, 0, 0);
        p.add(backBtn, c);
        return p;
    }

    private ImageIcon scaleIcon(String path, int w, int h) {
        URL resource = getClass().getResource(path);
        if (resource == null) return new ImageIcon();
        Image img = new ImageIcon(resource).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }

    private void applyResponsiveWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.max(MIN_WINDOW_WIDTH, (int) (screenSize.width * WINDOW_WIDTH_RATIO));
        int height = Math.max(MIN_WINDOW_HEIGHT, (int) (screenSize.height * WINDOW_HEIGHT_RATIO));
        setSize(Math.min(width, screenSize.width), Math.min(height, screenSize.height));
        setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        setLocationRelativeTo(null);
    }
}
