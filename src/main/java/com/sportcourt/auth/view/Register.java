package com.sportcourt.auth.view;

import com.sportcourt.auth.controller.AuthController;
import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.style.AppDialog;
import com.sportcourt.style.AppFonts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class Register extends JPanel {
    private final AuthController authController = new AuthController();
    private CardLayout cardLayout;
    private JPanel parentPanel;

    public Register(CardLayout cardLayout, JPanel parentPanel) {
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
        rightPanel.setBackground(new Color(250, 249, 250));
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBackground(new Color(250, 249, 250));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(10, 180, 10, 180);
        r.gridx = 0;
        r.weightx = 1;
        r.weighty = 0;
        r.fill = GridBagConstraints.HORIZONTAL;
        r.anchor = GridBagConstraints.CENTER;

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 11;
        filler.weightx = 1;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;

        JLabel backToLogin = new JLabel("< Quay lại đăng nhập");
        backToLogin.setFont(AppFonts.lexendRegular(20f));
        backToLogin.setForeground(new Color(58, 134, 45));
        backToLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));


        // ===== TITLE =====
        JLabel title = new JLabel("ĐĂNG KÍ TÀI KHOẢN");
        title.setFont(AppFonts.lexendBold(40f));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("Vui lòng điền thông tin của bạn.");
        subtitle.setFont(AppFonts.lexendRegular(18f));
        subtitle.setForeground(new Color(120, 120, 120));

        // ===== NAME =====
        JTextField username = new JTextField();
        username.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        username.setBackground(new Color(242, 242, 242));
        username.setBorder(null);
        username.putClientProperty("JTextField.placeholderText", "Họ và tên");

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(242, 242, 242));
        userPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        userPanel.setPreferredSize(new Dimension(200, 45));

        JLabel userIcon = new JLabel(scaleIcon("/icon/user.png", 18, 18));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(username, BorderLayout.CENTER);

        // ---- PHONE ----
        JTextField phonenum = new JTextField();
        phonenum.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        phonenum.setBackground(new Color(242, 242, 242));
        phonenum.setBorder(null);
        phonenum.putClientProperty("JTextField.placeholderText", "Số điện thoại");

        JPanel phonePanel = new JPanel(new BorderLayout());
        phonePanel.setBackground(new Color(242, 242, 242));
        phonePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        phonePanel.setPreferredSize(new Dimension(200, 45));

        JLabel phoneIcon = new JLabel(scaleIcon("/icon/phone.png", 18, 18));
        phoneIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        phonePanel.add(phoneIcon, BorderLayout.WEST);
        phonePanel.add(phonenum, BorderLayout.CENTER);

        // ---- EMAIL ----
        JTextField email = new JTextField();
        email.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        email.setBackground(new Color(242, 242, 242));
        email.setBorder(null);
        email.putClientProperty("JTextField.placeholderText", "Email");

        JPanel emailPanel = new JPanel(new BorderLayout());
        emailPanel.setBackground(new Color(242, 242, 242));
        emailPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        emailPanel.setPreferredSize(new Dimension(200, 45));

        JLabel emailIcon = new JLabel(scaleIcon("/icon/mail.png", 18, 18));
        emailIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        emailPanel.add(emailIcon, BorderLayout.WEST);
        emailPanel.add(email, BorderLayout.CENTER);

        JButton sendOtpBtn = new JButton("Gửi OTP");
        sendOtpBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        sendOtpBtn.setBackground(new Color(187, 220, 182));
        sendOtpBtn.setForeground(new Color(16, 110, 0));
        sendOtpBtn.setBorderPainted(false);
        sendOtpBtn.setPreferredSize(new Dimension(200, 50));
        // ---- ROW chứa 2 field ----
        JPanel contactRow = new JPanel(new BorderLayout(20, 0));
        contactRow.setBackground(new Color(242, 242, 242));

        contactRow.add(emailPanel, BorderLayout.CENTER);
        contactRow.add(sendOtpBtn, BorderLayout.EAST);

        JTextField otpField = new JTextField();
        otpField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        otpField.setBackground(new Color(242, 242, 242));
        otpField.setBorder(null);
        otpField.putClientProperty("JTextField.placeholderText", "Nhập OTP email");

        JLabel otpIcon = new JLabel(scaleIcon("/icon/otp.png", 18, 18));
        otpIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JPanel otpPanel = new JPanel(new BorderLayout());
        otpPanel.setBackground(new Color(242, 242, 242));
        otpPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        otpPanel.setPreferredSize(new Dimension(200, 45));
        otpPanel.add(otpField, BorderLayout.CENTER);
        otpPanel.add(otpIcon, BorderLayout.WEST);

        JButton verifyOtpBtn = new JButton("Xác thực");
        verifyOtpBtn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        verifyOtpBtn.setBackground(new Color(187, 220, 182));
        verifyOtpBtn.setForeground(new Color(16, 110, 0));
        verifyOtpBtn.setBorderPainted(false);
        verifyOtpBtn.setPreferredSize(new Dimension(200, 45));

        JPanel otpRow = new JPanel(new BorderLayout(20, 0));
        otpRow.setBackground(new Color(242, 242, 242));
        otpRow.add(otpPanel, BorderLayout.CENTER);
        otpRow.add(verifyOtpBtn, BorderLayout.EAST);

        // ===== PASSWORD =====
        JPasswordField password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        password.setBackground(new Color(242, 242, 242));
        password.setBorder(null);
        password.putClientProperty("JTextField.placeholderText", "Mật khẩu");

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(new Color(242, 242, 242));
        passPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        passPanel.setPreferredSize(new Dimension(200, 45));

        JLabel passIcon = new JLabel(scaleIcon("/icon/pass.png", 18, 18));
        passIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        passPanel.add(passIcon, BorderLayout.WEST);
        passPanel.add(password, BorderLayout.CENTER);

        // ===== XÁC NHẬN PASSWORD =====
        JPasswordField checkPassword = new JPasswordField();
        checkPassword.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        checkPassword.setBackground(new Color(242, 242, 242));
        checkPassword.setBorder(null);
        checkPassword.putClientProperty("JTextField.placeholderText", "Xác nhận mật khẩu");

        JPanel checkpassPanel = new JPanel(new BorderLayout());
        checkpassPanel.setBackground(new Color(242, 242, 242));
        checkpassPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        checkpassPanel.setPreferredSize(new Dimension(200, 45));

        JLabel checkpassIcon = new JLabel(scaleIcon("/icon/pass.png", 18, 18));
        checkpassIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        checkpassPanel.add(checkpassIcon, BorderLayout.WEST);
        checkpassPanel.add(checkPassword, BorderLayout.CENTER);


        // ===== REGISTER BUTTON =====
        JButton registerBtn = new JButton("Đăng kí") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(57, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                super.paintComponent(g);
                g2.dispose();
            }
        };

        registerBtn.setForeground(Color.BLACK);
        registerBtn.setFont(AppFonts.lexendBold(22f));
        registerBtn.setFocusPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setPreferredSize(new Dimension(200, 50));
        final boolean[] registerOtpVerified = {false};

        // ===== CREATE ACCOUNT =====
        JLabel label = new JLabel(
                "<html><div style='font-size:12px;'>" +
                        "Bạn đã có tài khoản? <font color='#6C757D'> </font>" +
                        "<font color='#3a862d'><b>Đăng nhập ngay</b></font>" +
                        "</div></html>"
        );
        label.setHorizontalAlignment(SwingConstants.CENTER);

        // ================= LOGIC XỬ LÝ SỰ KIỆN =================
        //final boolean[] registerOtpVerified = {false};

        backToLogin.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { cardLayout.show(parentPanel, "LOGIN"); }
        });

        label.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { cardLayout.show(parentPanel, "LOGIN"); }
        });

        sendOtpBtn.addActionListener(e -> {
            AuthResult res = authController.sendRegisterOtp(email.getText().trim());
            if (res.success()) AppDialog.showInfo(this, res.message()); else AppDialog.showError(this, res.message());
        });

        verifyOtpBtn.addActionListener(e -> {
            AuthResult res = authController.verifyRegisterOtp(email.getText().trim(), otpField.getText().trim());
            if (res.success()) {
                registerOtpVerified[0] = true;
                registerBtn.setEnabled(true);
                AppDialog.showInfo(this, res.message());
            } else {
                AppDialog.showError(this, res.message());
            }
        });

//        registerBtn.addActionListener(e -> {
//            RegisterRequest req = new RegisterRequest(
//                    username.getText().trim(), new String(password.getPassword()), username.getText().trim(),
//                    phonenum.getText().trim(), email.getText().trim(), birthdayPicker.getDate(), address.getText().trim()
//            );
//            AuthResult res = authController.register(req);
//            if (res.success()) {
//                AppDialog.showInfo(this, res.message());
//                cardLayout.show(parentPanel, "LOGIN");
//            } else {
//                AppDialog.showError(this, res.message());
//            }
//        });

        // ================= ADD COMPONENTS VÀO RIGHTPANEL =================
        r.gridy = 0; add(backToLogin, r);
        r.gridy++; add(title, r);
        r.gridy++; add(subtitle, r);
        r.gridy++; add(userPanel, r);
        r.gridy++; add(phonePanel, r);
        r.gridy++; add(contactRow, r);
        r.gridy++; add(otpRow, r);
        r.gridy++; add(passPanel, r);
        r.gridy++; add(checkpassPanel, r);
        r.gridy++; add(registerBtn, r);
        r.gridy++; add(label, r);


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

