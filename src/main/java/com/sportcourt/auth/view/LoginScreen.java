package com.sportcourt.auth.view;

import com.sportcourt.auth.controller.AuthController;
import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.LoginRequest;
import com.sportcourt.style.AppDialog;
import com.sportcourt.style.BackgroundPanel;
import com.sportcourt.style.AppFonts;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class    LoginScreen extends JFrame {
    private final AuthController authController = new AuthController();

    public LoginScreen() {
        AppFonts.register();
        setTitle("RentSta Login");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1;

        // ================= LEFT PANEL =================
        BackgroundPanel leftPanel = new BackgroundPanel("/image/bg.png");
        leftPanel.setLayout(new GridBagLayout());


        // ================= RIGHT PANEL =================
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(250, 249, 250));
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(10, 30, 10, 30);
        r.fill = GridBagConstraints.HORIZONTAL;
        r.gridx = 0;
        r.weightx = 1;
        r.anchor = GridBagConstraints.NORTH;

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 7;
        filler.weightx = 1;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;

        // ===== TITLE =====
        JLabel title = new JLabel("CHÀO MỪNG TRỞ LẠI!");
        title.setFont(AppFonts.lexendBold(25f));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("Vui lòng điền thông tin tài khoản của bạn.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(new Color(120, 120, 120));

        // ===== USERNAME =====
        JTextField username = new JTextField();
        username.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        username.setBackground(new Color(250, 249, 250));
        username.setBorder(null);
        username.putClientProperty("JTextField.placeholderText", "Tên đăng nhập");

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(250, 249, 250));
        userPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        userPanel.setPreferredSize(new Dimension(200, 45));

        JLabel userIcon = new JLabel(scaleIcon("/icon/user.png", 13, 13));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(username, BorderLayout.CENTER);

        // ===== PASSWORD =====
        JPasswordField password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        password.setBackground(new Color(250, 249, 250));
        password.setBorder(null);
        password.putClientProperty("JTextField.placeholderText", "Mật khẩu");

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(new Color(250, 249, 250));
        passPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        passPanel.setPreferredSize(new Dimension(200, 45));

        JLabel passIcon = new JLabel(scaleIcon("/icon/pass.png", 13, 13));
        passIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        passPanel.add(passIcon, BorderLayout.WEST);
        passPanel.add(password, BorderLayout.CENTER);

        // ===== FORGOT PASSWORD =====
        JLabel forgot = new JLabel("Quên mật khẩu?");
        forgot.setFont(AppFonts.lexendBold(10f));
        forgot.setForeground(new Color(58, 134, 45));
        forgot.setHorizontalAlignment(SwingConstants.RIGHT);
        forgot.setCursor(new Cursor(Cursor.HAND_CURSOR));


        // ===== LOGIN BUTTON =====
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
        loginBtn.setFont(AppFonts.lexendBold(18f));
        loginBtn.setFocusPainted(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setPreferredSize(new Dimension(200, 45));

        // ===== CREATE ACCOUNT =====
        JLabel label = new JLabel(
                "<html>Bạn chưa có tài khoản? <font color='#6C757D'> </font>" +
                        "<font color='#3a862d'><b>Đăng kí ngay</b></font></html>"
        );
        label.setHorizontalAlignment(SwingConstants.CENTER);

        // ===== EVENT =====
        // REGISTER
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new Register().setVisible(true);
                dispose();
            }
        });

        forgot.setCursor(new Cursor((Cursor.HAND_CURSOR)));
        forgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ForgotPassword().setVisible(true);
                dispose();
            }
        });

        loginBtn.addActionListener(e -> {
            String usernameValue = username.getText().trim();
            String passwordValue = new String(password.getPassword());

            AuthResult result = authController.login(new LoginRequest(usernameValue, passwordValue));
            if (result.success()) {
                AppDialog.showInfo(this, result.message());
            } else {
                AppDialog.showError(this, result.message());
            }
            if (result.success()) {
                dispose();
            }
        });

        // ===== ADD COMPONENTS =====
        r.gridy = 0;
        rightPanel.add(title, r);

        r.gridy ++;
        rightPanel.add(subtitle, r);

        r.gridy++;
        rightPanel.add(userPanel, r);

        r.gridy++;
        rightPanel.add(passPanel, r);

        r.gridy++;
        rightPanel.add(forgot, r);

        r.gridy++;
        rightPanel.add(loginBtn, r);

        r.gridy++;
        rightPanel.add(label, r);
        rightPanel.add(Box.createVerticalGlue(), filler);

        // ===== ADD TO FRAME =====
        gbc.gridx = 0;
        gbc.weightx = 2;
        add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        add(rightPanel, gbc);
    }

    // ===== SCALE ICON =====
    private ImageIcon scaleIcon(String path, int w, int h) {
        URL resource = getClass().getResource(path);
        if (resource == null) {
            return new ImageIcon();
        }
        Image img = new ImageIcon(resource).getImage()
                .getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginScreen().setVisible(true);
        });
    }
}

