package com.sportcourt.auth.view;

import com.sportcourt.auth.controller.AuthController;
import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.ResetPasswordRequest;
import com.sportcourt.style.AppDialog;
import com.sportcourt.style.BackgroundPanel;
import com.sportcourt.style.AppFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class ForgotPassword extends JFrame {
    private final AuthController authController = new AuthController();

    public ForgotPassword() {
        AppFonts.register();
        setTitle("ForgotPassword");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1;

        // ================= LEFT PANEL =================
        BackgroundPanel leftPanel = new BackgroundPanel("/image/bg2.png");
        leftPanel.setLayout(new GridBagLayout());

        // ================= RIGHT PANEL =================
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(250, 249, 250));
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(10, 10, 10, 10);
        r.fill = GridBagConstraints.HORIZONTAL;
        r.gridx = 0;

        JLabel backToLogin = new JLabel("< Quay lại đăng nhập");
        backToLogin.setFont(AppFonts.lexendRegular(13f));
        backToLogin.setForeground(new Color(58, 134, 45));
        backToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginScreen().setVisible(true);
                dispose();
            }
        });

        // ===== TITLE =====
        JLabel title = new JLabel("ĐẶT LẠI MẬT KHẨU");
        title.setFont(AppFonts.lexendBold(25f));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("Vui lòng điền thông tin để nhận mã xác thực.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(new Color(120, 120, 120));

        // ===== NAME =====
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

        // ---- PHONE ----
        JTextField phonenum = new JTextField();
        phonenum.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        phonenum.setBackground(new Color(250, 249, 250));
        phonenum.setBorder(null);
        phonenum.putClientProperty("JTextField.placeholderText", "Số điện thoại");

        JPanel phonePanel = new JPanel(new BorderLayout());
        phonePanel.setBackground(new Color(250, 249, 250));
        phonePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        phonePanel.setPreferredSize(new Dimension(200, 45));

        JLabel phoneIcon = new JLabel(scaleIcon("/icon/phone.png", 13, 13));
        phoneIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JButton sendOtp = new JButton("Gửi mã OTP");
        sendOtp.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        sendOtp.setBackground(new Color(231, 250, 229));
        sendOtp.setForeground(new Color(16, 110, 0));
        sendOtp.setBorderPainted(false);

        phonePanel.add(phoneIcon, BorderLayout.WEST);
        phonePanel.add(phonenum, BorderLayout.CENTER);

        JPanel contactRow = new JPanel(new BorderLayout(15,0));
        contactRow.setBackground(new Color(250, 249, 250));
        contactRow.add(phonePanel, BorderLayout.CENTER);
        sendOtp.setPreferredSize(new Dimension(130, 45));
        contactRow.add(sendOtp, BorderLayout.EAST);


        // ===== OTP =====
        JPasswordField otp = new JPasswordField();
        otp.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        otp.setBackground(new Color(250, 249, 250));
        otp.setBorder(null);
        otp.putClientProperty("JTextField.placeholderText", "Nhập OTP gồm 6 số");

        JPanel otpPanel = new JPanel(new BorderLayout());
        otpPanel.setBackground(new Color(250, 249, 250));
        otpPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        otpPanel.setPreferredSize(new Dimension(200, 45));

        JLabel otpIcon = new JLabel(scaleIcon("/icon/pass.png", 13, 13));
        otpIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        otpPanel.add(otpIcon, BorderLayout.WEST);
        otpPanel.add(otp, BorderLayout.CENTER);

        JButton checkOtp = new JButton("Xác nhận OTP");
        checkOtp.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        checkOtp.setBackground(new Color(231, 250, 229));
        checkOtp.setForeground(new Color(16, 110, 0));
        checkOtp.setBorderPainted(false);

        JPanel contactRow1 = new JPanel(new BorderLayout(15,0));
        contactRow1.setBackground(new Color(250, 249, 250));
        contactRow1.add(otpPanel, BorderLayout.CENTER);
        contactRow1.setPreferredSize(new Dimension(130,45));
        contactRow1.add(checkOtp, BorderLayout.EAST);
        contactRow1.setVisible(false);

        // ===== NEW PASSWORD =====
        JPasswordField password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        password.setBackground(new Color(250, 249, 250));
        password.setBorder(null);
        password.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu mới");

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(new Color(250, 249, 250));
        passPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        passPanel.setPreferredSize(new Dimension(200, 45));

        JLabel passIcon = new JLabel(scaleIcon("/icon/pass.png", 13, 13));
        passIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        passPanel.add(passIcon, BorderLayout.WEST);
        passPanel.add(password, BorderLayout.CENTER);
        passPanel.setVisible(false);


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

                    if (phonenum.getText().isEmpty()) {
                        AppDialog.showError(this, "Vui lòng nhập số điện thoại!");
                        return;
                    }
                    contactRow1.setVisible(true);
                    this.revalidate();
                    this.repaint();
                    //checkOtp.setText("Gửi lại mã");
        });

        checkOtp.addActionListener(e -> {

            if (otp.getText().isEmpty()) {
                AppDialog.showError(this, "Vui lòng nhập OTP!");
                return;
            }
            passPanel.setVisible(true);
            this.revalidate();
            this.repaint();
            //checkOtp.setText("Gửi lại mã");
        });


        checkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBtn.addActionListener(e -> {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    username.getText().trim(),
                    phonenum.getText().trim(),
                    new String(password.getPassword())
            );
            AuthResult result = authController.resetPassword(request);
            if (result.success()) {
                AppDialog.showInfo(this, result.message());
            } else {
                AppDialog.showError(this, result.message());
            }
            if (result.success()) {
                new LoginScreen().setVisible(true);
                dispose();
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

        //Phonenum
        r.gridy++;
        rightPanel.add(contactRow,r);

        // Otp
        r.gridy++;
        rightPanel.add(contactRow1, r);

        //New Password
        r.gridy++;
        rightPanel.add(passPanel,r);

        // Button
        r.gridy++;
        rightPanel.add(checkBtn, r);


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

}