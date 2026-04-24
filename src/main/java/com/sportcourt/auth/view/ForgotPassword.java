package com.sportcourt.auth.view;

import com.sportcourt.auth.controller.AuthController;
import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.ResetPasswordRequest;
import com.sportcourt.style.AppDialog;
import com.sportcourt.style.AppFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class ForgotPassword extends JPanel {
    private final AuthController authController = new AuthController();
    private CardLayout cardLayout;
    private JPanel parentPanel;

    public ForgotPassword(CardLayout cardLayout, JPanel parentPanel) {
        this.cardLayout = cardLayout;
        this.parentPanel = parentPanel;
        AppFonts.register();

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1;

        // ================= RIGHT PANEL =================
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(242, 242, 242));
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(10, 180, 10, 180);
        r.gridx = 0;
        r.weightx = 1;
        r.weighty = 0;
        r.fill = GridBagConstraints.HORIZONTAL;
        r.anchor = GridBagConstraints.CENTER;

        JLabel backToLogin = new JLabel("< Quay lại đăng nhập");
        backToLogin.setFont(AppFonts.lexendRegular(20f));
        backToLogin.setForeground(new Color(58, 134, 45));
        backToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(parentPanel, "LOGIN");
            }
        });

        // ===== TITLE =====
        JLabel title = new JLabel("ĐẶT LẠI MẬT KHẨU");
        title.setFont(AppFonts.lexendBold(40f));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("Vui lòng điền thông tin để nhận mã xác thực.");
        subtitle.setFont(AppFonts.lexendRegular(18f));
        subtitle.setForeground(new Color(120, 120, 120));

        // ===== NAME =====
        JTextField username = new JTextField();
        username.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        username.setBackground(new Color(242, 242, 242));
        username.setBorder(null);
        username.putClientProperty("JTextField.placeholderText", "Số điện thoại");

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(242, 242, 242));
        userPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        userPanel.setPreferredSize(new Dimension(200, 45));

        JLabel userIcon = new JLabel(scaleIcon("/icon/user.png", 18, 18));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(username, BorderLayout.CENTER);

        // ---- EMAIL ----
        JTextField emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        emailField.setBackground(new Color(242, 242, 242));
        emailField.setBorder(null);
        emailField.putClientProperty("JTextField.placeholderText", "Email");

        JPanel emailPanel = new JPanel(new BorderLayout());
        emailPanel.setBackground(new Color(242, 242, 242));
        emailPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        emailPanel.setPreferredSize(new Dimension(200, 45));

        JLabel emailIcon = new JLabel(scaleIcon("/icon/mail.png", 18, 18));
        emailIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JButton sendOtp = new JButton("Gửi mã OTP");
        sendOtp.setFont(new Font("Segoe UI", Font.BOLD, 20));
        sendOtp.setBackground(new Color(187, 220, 182));
        sendOtp.setForeground(new Color(16, 110, 0));
        sendOtp.setBorderPainted(false);
        sendOtp.setPreferredSize(new Dimension(200, 50));

        emailPanel.add(emailIcon, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);

        JPanel contactRow = new JPanel(new BorderLayout(20, 0));
        contactRow.setBackground(new Color(242, 242, 242));
        contactRow.add(emailPanel, BorderLayout.CENTER);
        contactRow.add(sendOtp, BorderLayout.EAST);


        // ===== OTP =====
        JPasswordField otp = new JPasswordField();
        otp.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        otp.setBackground(new Color(242, 242, 242));
        otp.setBorder(null);
        otp.putClientProperty("JTextField.placeholderText", "Nhập OTP gồm 6 số");

        JPanel otpPanel = new JPanel(new BorderLayout());
        otpPanel.setBackground(new Color(242, 242, 242));
        otpPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));

        JLabel otpIcon = new JLabel(scaleIcon("/icon/pass.png", 18, 18));
        otpIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        otpPanel.add(otpIcon, BorderLayout.WEST);
        otpPanel.add(otp, BorderLayout.CENTER);

        JButton checkOtp = new JButton("Xác nhận OTP");
        checkOtp.setFont(new Font("Segoe UI", Font.BOLD, 20));
        checkOtp.setBackground(new Color(187, 220, 182));
        checkOtp.setForeground(new Color(16, 110, 0));
        checkOtp.setBorderPainted(false);
        checkOtp.setPreferredSize(new Dimension(200, 50));

        JPanel contactRow1 = new JPanel(new BorderLayout(20, 0));
        contactRow1.setBackground(new Color(242, 242, 242));
        contactRow1.add(otpPanel, BorderLayout.CENTER);
        contactRow1.setPreferredSize(new Dimension(2, 45));
        contactRow1.add(checkOtp, BorderLayout.EAST);
        //contactRow1.setVisible(false);

        // ===== NEW PASSWORD =====
        JPasswordField password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        password.setBackground(new Color(242, 242, 242));
        password.setBorder(null);
        password.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu mới");

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(new Color(242, 242, 242));
        passPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        passPanel.setPreferredSize(new Dimension(200, 45));

        JLabel passIcon = new JLabel(scaleIcon("/icon/pass.png", 18, 18));
        passIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        passPanel.add(passIcon, BorderLayout.WEST);
        passPanel.add(password, BorderLayout.CENTER);

        // ===== CHECK NEW PASSWORD =====
        JPasswordField checkPassword = new JPasswordField();
        checkPassword.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        checkPassword.setBackground(new Color(242, 242, 242));
        checkPassword.setBorder(null);
        checkPassword.putClientProperty("JTextField.placeholderText", "Xác nhận mật khẩu mới");

        JPanel checkpassPanel = new JPanel(new BorderLayout());
        checkpassPanel.setBackground(new Color(242, 242, 242));
        checkpassPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        checkpassPanel.setPreferredSize(new Dimension(200, 45));

        JLabel checkpassIcon = new JLabel(scaleIcon("/icon/pass.png", 18, 18));
        checkpassIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        checkpassPanel.add(checkpassIcon, BorderLayout.WEST);
        checkpassPanel.add(checkPassword, BorderLayout.CENTER);
        //passPanel.setVisible(true);


        // ===== CHECK BUTTON =====
        JButton checkBtn = new JButton("Xác nhận") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(57, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
                g2.dispose();
            }
        };

        checkBtn.setForeground(Color.BLACK);
        checkBtn.setFont(AppFonts.lexendBold(18f));
        checkBtn.setFocusPainted(false);
        checkBtn.setContentAreaFilled(false);
        checkBtn.setBorderPainted(false);
        checkBtn.setPreferredSize(new Dimension(200, 45));

        // ===== EVENT =====

        sendOtp.addActionListener(e -> {
            AuthResult result = authController.sendResetPasswordOtp(
                    username.getText().trim(),
                    emailField.getText().trim()
            );
            if (result.success()) {
                AppDialog.showInfo(this, result.message());
                contactRow1.setVisible(true);
                this.revalidate();
                this.repaint();
            } else {
                AppDialog.showError(this, result.message());
            }
        });

        checkOtp.addActionListener(e -> {
            AuthResult result = authController.verifyResetPasswordOtp(
                    username.getText().trim(),
                    emailField.getText().trim(),
                    new String(otp.getPassword()).trim()
            );
            if (result.success()) {
                AppDialog.showInfo(this, result.message());
                checkpassPanel.setVisible(true);
                this.revalidate();
                this.repaint();
            } else {
                AppDialog.showError(this, result.message());
            }
        });


        checkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBtn.addActionListener(e -> {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    username.getText().trim(),
                    emailField.getText().trim(),
                    new String(checkPassword.getPassword())
            );
            AuthResult result = authController.resetPassword(request);
            if (result.success()) {
                AppDialog.showInfo(this, result.message());
            } else {
                AppDialog.showError(this, result.message());
            }
            if (result.success()) {
                cardLayout.show(parentPanel, "LOGIN");
            }
        });

        // ===== ADD COMPONENTS =====
        r.gridy = 0;
        rightPanel.add(backToLogin, r);

        r.gridy++;
        rightPanel.add(title, r);

        r.gridy++;
        rightPanel.add(subtitle, r);

        // Username
        r.gridy++;
        rightPanel.add(userPanel, r);

        // Email
        r.gridy++;
        rightPanel.add(contactRow, r);

        // Otp
        r.gridy++;
        rightPanel.add(contactRow1, r);

        //New Password
        r.gridy++;
        rightPanel.add(passPanel, r);

        //Check New Password
        r.gridy++;
        rightPanel.add(checkpassPanel, r);

        // Button
        r.gridy++;
        rightPanel.add(checkBtn, r);


        // ===== ADD TO FRAME =====
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

}