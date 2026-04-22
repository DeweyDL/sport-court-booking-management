package com.sportcourt.auth.view;

import com.sportcourt.auth.controller.AuthController;
import com.sportcourt.auth.dto.AuthResult;
import com.sportcourt.auth.dto.RegisterRequest;
import com.github.lgooddatepicker.components.DatePicker;
import com.sportcourt.style.AppDialog;
import com.sportcourt.style.BackgroundPanel;
import com.sportcourt.style.AppFonts;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDate;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Register extends JFrame {
    private final AuthController authController = new AuthController();

    public Register() {
        AppFonts.register();
        setTitle("Register Login");
        setSize(1100, 800);
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
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(10, 10, 10, 10);
        r.fill = GridBagConstraints.HORIZONTAL;
        r.gridx = 0;
        r.weightx = 1;
        r.anchor = GridBagConstraints.NORTH;

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 11;
        filler.weightx = 1;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.BOTH;

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
        JLabel title = new JLabel("ĐĂNG KÍ TÀI KHOẢN");
        title.setFont(AppFonts.lexendBold(25f));
        title.setForeground(Color.BLACK);

        JLabel subtitle = new JLabel("Vui lòng điền thông tin của bạn.");
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

        // ===== PHONE + EMAIL (1 ROW) =====

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

        phonePanel.add(phoneIcon, BorderLayout.WEST);
        phonePanel.add(phonenum, BorderLayout.CENTER);

        // ---- EMAIL ----
        JTextField email = new JTextField();
        email.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        email.setBackground(new Color(250, 249, 250));
        email.setBorder(null);
        email.putClientProperty("JTextField.placeholderText", "Email");

        JPanel emailPanel = new JPanel(new BorderLayout());
        emailPanel.setBackground(new Color(250, 249, 250));
        emailPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        emailPanel.setPreferredSize(new Dimension(200, 45));

        JLabel emailIcon = new JLabel(scaleIcon("/icon/mail.png", 13, 13));
        emailIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        emailPanel.add(emailIcon, BorderLayout.WEST);
        emailPanel.add(email, BorderLayout.CENTER);

        // ---- ROW chứa 2 field ----
        JPanel contactRow = new JPanel(new GridLayout(1, 2, 15, 0)); // cách nhau 15px
        contactRow.setBackground(new Color(250, 249, 250));

        contactRow.add(phonePanel);
        contactRow.add(emailPanel);

        JTextField otpField = new JTextField();
        otpField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        otpField.setBackground(new Color(250, 249, 250));
        otpField.setBorder(null);
        otpField.putClientProperty("JTextField.placeholderText", "Nhập OTP email");

        JPanel otpPanel = new JPanel(new BorderLayout());
        otpPanel.setBackground(new Color(250, 249, 250));
        otpPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        otpPanel.setPreferredSize(new Dimension(200, 45));
        otpPanel.add(otpField, BorderLayout.CENTER);

        JButton sendOtpBtn = new JButton("Gửi OTP");
        sendOtpBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sendOtpBtn.setBackground(new Color(231, 250, 229));
        sendOtpBtn.setForeground(new Color(16, 110, 0));
        sendOtpBtn.setBorderPainted(false);
        sendOtpBtn.setPreferredSize(new Dimension(100, 45));

        JButton verifyOtpBtn = new JButton("Xác thực");
        verifyOtpBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        verifyOtpBtn.setBackground(new Color(231, 250, 229));
        verifyOtpBtn.setForeground(new Color(16, 110, 0));
        verifyOtpBtn.setBorderPainted(false);
        verifyOtpBtn.setPreferredSize(new Dimension(110, 45));

        JPanel otpRow = new JPanel(new BorderLayout(15, 0));
        otpRow.setBackground(new Color(250, 249, 250));
        otpRow.add(otpPanel, BorderLayout.CENTER);

        JPanel otpActionPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        otpActionPanel.setBackground(new Color(250, 249, 250));
        otpActionPanel.add(sendOtpBtn);
        otpActionPanel.add(verifyOtpBtn);
        otpRow.add(otpActionPanel, BorderLayout.EAST);

        // ===== BIRTHDAY (ICON + LABEL + DATEPICKER 1 HÀNG) =====

        DatePicker birthdayPicker = new DatePicker();
        birthdayPicker.getSettings().setFormatForDatesCommonEra("dd/MM/yyyy");
        birthdayPicker.getSettings().setAllowKeyboardEditing(false);
        birthdayPicker.getComponentDateTextField().setFont(new Font("Segoe UI", Font.PLAIN, 15));
        birthdayPicker.getComponentDateTextField().setBorder(null);
        birthdayPicker.getComponentDateTextField().setBackground(new Color(250, 249, 249));
        birthdayPicker.getComponentDateTextField().setFocusable(false);
        birthdayPicker.getComponentToggleCalendarButton().setText("\uD83D\uDCC5");
        birthdayPicker.getComponentToggleCalendarButton().setFocusable(false);
        birthdayPicker.setBorder(null);
        birthdayPicker.setBackground(new Color(250, 249, 250));
        birthdayPicker.setPreferredSize(new Dimension(140, 45));

        // ===== ICON =====
        JLabel icon = new JLabel(scaleIcon("/icon/calendar.png", 13, 13));
        icon.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        // ===== LABEL =====
        JLabel label1 = new JLabel("Ngày sinh");
        label1.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label1.setForeground(new Color(120, 120, 120));
        birthdayPicker.addDateChangeListener(e -> label1.setVisible(birthdayPicker.getDate() == null));

        // ===== LEFT GROUP (icon + label) =====
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        leftGroup.setBackground(new Color(250, 249, 250));
        leftGroup.add(icon);
        leftGroup.add(label1);

        // ===== ROW =====
        JPanel birthdayRow = new JPanel(new BorderLayout());
        birthdayRow.setBackground(new Color(250, 249, 250));
        birthdayRow.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        birthdayRow.setPreferredSize(new Dimension(200, 45));

        birthdayRow.add(leftGroup, BorderLayout.WEST);
        birthdayRow.add(birthdayPicker, BorderLayout.CENTER);

        // ===== ADDRESS =====

        JTextField address = new JTextField();
        address.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        address.setBackground(new Color(250, 249, 250));
        address.setBorder(null);
        address.putClientProperty("JTextField.placeholderText", "Địa chỉ");

        // scroll (phòng khi dài)
        JScrollPane addressScroll = new JScrollPane(address);
        addressScroll.setBorder(null);
        addressScroll.setBackground(new Color(250, 249, 250));

        // panel style giống input
        JPanel addressPanel = new JPanel(new BorderLayout());
        addressPanel.setBackground(new Color(250, 249, 250));
        addressPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)));
        addressPanel.setPreferredSize(new Dimension(200, 45));

        // icon
        JLabel addressIcon = new JLabel(scaleIcon("/icon/address.png", 13, 13));
        addressIcon.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        addressPanel.add(addressIcon, BorderLayout.WEST);
        addressPanel.add(addressScroll, BorderLayout.CENTER);

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
        registerBtn.setFont(AppFonts.lexendBold(18f));
        registerBtn.setFocusPainted(false);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setPreferredSize(new Dimension(200, 45));
        registerBtn.setEnabled(false);
        final boolean[] registerOtpVerified = {false};

        // ===== CREATE ACCOUNT =====
        JLabel label = new JLabel(
                "<html>Bạn đã có tài khoản? <font color='#6C757D'> </font>" +
                        "<font color='#3a862d'><b>Đăng nhập ngay</b></font></html>"
        );
        label.setHorizontalAlignment(SwingConstants.CENTER);

        // ===== EVENT =====
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LoginScreen().setVisible(true);
                dispose();
            }
        });

        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> {
            LocalDate ngaySinh = birthdayPicker.getDate();

            if (!registerOtpVerified[0]) {
                AppDialog.showError(this, "Vui lòng xác thực OTP email trước khi đăng ký.");
                return;
            }

            RegisterRequest request = new RegisterRequest(
                    username.getText().trim(),
                    new String(password.getPassword()),
                    username.getText().trim(),
                    phonenum.getText().trim(),
                    email.getText().trim(),
                    ngaySinh,
                    address.getText().trim()
            );

            AuthResult result = authController.register(request);
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

        sendOtpBtn.addActionListener(e -> {
            AuthResult result = authController.sendRegisterOtp(email.getText().trim());
            if (result.success()) {
                AppDialog.showInfo(this, result.message());
            } else {
                AppDialog.showError(this, result.message());
            }
        });

        verifyOtpBtn.addActionListener(e -> {
            AuthResult result = authController.verifyRegisterOtp(email.getText().trim(), otpField.getText().trim());
            if (result.success()) {
                registerOtpVerified[0] = true;
                registerBtn.setEnabled(true);
                AppDialog.showInfo(this, result.message());
            } else {
                registerOtpVerified[0] = false;
                registerBtn.setEnabled(false);
                AppDialog.showError(this, result.message());
            }
        });

        email.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                resetOtpState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resetOtpState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                resetOtpState();
            }

            private void resetOtpState() {
                registerOtpVerified[0] = false;
                registerBtn.setEnabled(false);
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

        // SĐT + Email (1 dòng)
        r.gridy++;
        rightPanel.add(contactRow, r);

        // OTP
        r.gridy++;
        rightPanel.add(otpRow, r);

        // Ngày sinh (label + datepicker cùng hàng)
        r.gridy++;
        rightPanel.add(birthdayRow, r);

        // Địa chỉ (2 dòng)
        r.gridy++;
        rightPanel.add(addressPanel, r);

        // Password
        r.gridy++;
        rightPanel.add(passPanel, r);

        // Button
        r.gridy++;
        rightPanel.add(registerBtn, r);

        //Link đăng ký
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

}

