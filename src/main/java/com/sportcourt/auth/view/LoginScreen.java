package com.sportcourt.auth.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.lgooddatepicker.components.DatePicker;
import com.sportcourt.auth.controller.AuthController;
import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.LoginRequest;
import com.sportcourt.auth.dto.RegisterRequest;
import com.sportcourt.style.AppDialog;
import com.sportcourt.style.AppFonts;
import com.sportcourt.style.BackgroundPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;

public class LoginScreen extends JFrame {
    private final AuthController authController = new AuthController();

    // Khởi tạo các thành phần để tạo CardLayout
    private CardLayout cardLayout = new CardLayout();
    private JPanel rightPanel = new JPanel(cardLayout);

    public LoginScreen() {
        AppFonts.register();
        setTitle("RentSta Login");
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        rightPanel.setBackground(new Color(242, 242, 242));



        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1;

        // PANEL BÊN TRÁI
        BackgroundPanel leftPanel = new BackgroundPanel("/image/bg.png");
        leftPanel.setLayout(new GridBagLayout());


        // --- BẮT ĐẦU PHẦN NỘI DUNG LOGIN (CARD 1) ---
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(new Color(242, 242, 242));
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        // Giữ nguyên các thông số r của bạn
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(10, 180, 10, 180);
        r.gridx = 0;
        r.weightx = 1;
        r.weighty = 0;
        r.fill = GridBagConstraints.HORIZONTAL;
        r.anchor = GridBagConstraints.CENTER;

        JLabel title = new JLabel("CHÀO MỪNG TRỞ LẠI!");
        title.setFont(AppFonts.lexendBold(40f));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("Vui lòng điền thông tin tài khoản của bạn.");
        subtitle.setFont(AppFonts.lexendRegular(18f));
        subtitle.setForeground(new Color(120, 120, 120));

        JTextField phoneField = new JTextField();
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        phoneField.setBackground(new Color(242, 242, 242));
        phoneField.setBorder(null);
        phoneField.putClientProperty("JTextField.placeholderText", "Số điện thoại");

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(242, 242, 242));
        userPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        userPanel.setPreferredSize(new Dimension(200, 50));

        JLabel userIcon = new JLabel(scaleIcon("/icon/user.png", 18, 18));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(phoneField, BorderLayout.CENTER);

        JPasswordField password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        password.setBackground(new Color(242, 242, 242));
        password.setBorder(null);
        password.putClientProperty("JTextField.placeholderText", "Mật khẩu");

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(new Color(242, 242, 242));
        passPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        passPanel.setPreferredSize(new Dimension(200, 50));

        JLabel passIcon = new JLabel(scaleIcon("/icon/pass.png", 18, 18));
        passIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
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
                g2.setColor(new Color(57, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        loginBtn.setForeground(Color.BLACK);
        loginBtn.setFont(AppFonts.lexendBold(22f));
        loginBtn.setFocusPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setPreferredSize(new Dimension(200, 50));
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

        // Layout JFrame chính (Giữ nguyên weightx của bạn)
        gbc.gridx = 0;
        gbc.weightx = 8;
        add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
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
}