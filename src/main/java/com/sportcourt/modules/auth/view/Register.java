package com.sportcourt.modules.auth.view;

import com.sportcourt.modules.auth.controller.AuthController;
import com.sportcourt.modules.auth.dto.AuthResult;
import com.sportcourt.modules.auth.dto.RegisterRequest;
import com.sportcourt.common.style.AppDialog;
import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.UIScale;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class Register extends JPanel {
    private static final int FORM_SIDE_INSET = UIScale.scale(48);
    private static final int OTP_ROW_GAP = UIScale.scale(8);
    private static final Insets OTP_BUTTON_MARGIN = new Insets(0, UIScale.scale(8), 0, UIScale.scale(8));
    private static final Dimension OTP_BUTTON_SIZE = new Dimension(UIScale.scale(150), UIScale.scale(44));

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
        rightPanel.setBorder(BorderFactory.createEmptyBorder(
                UIScale.scale(40), UIScale.scale(40), UIScale.scale(40), UIScale.scale(40)));

        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(UIScale.scale(8), FORM_SIDE_INSET, UIScale.scale(8), FORM_SIDE_INSET);
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
        username.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(20)));
        username.setBackground(new Color(242, 242, 242));
        username.setBorder(null);
        username.putClientProperty("JTextField.placeholderText", "Họ và tên");

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(242, 242, 242));
        userPanel.setBorder(AuthViewStyle.createInputUnderlineBorder());
        userPanel.setPreferredSize(new Dimension(200, UIScale.scale(45)));

        JLabel userIcon = new JLabel(scaleIcon("/icon/user.png", UIScale.scale(18), UIScale.scale(18)));
        userIcon.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(5), 0, UIScale.scale(5)));

        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(username, BorderLayout.CENTER);

        // ---- PHONE ----
        JTextField phonenum = new JTextField();
        phonenum.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(20)));
        phonenum.setBackground(new Color(242, 242, 242));
        phonenum.setBorder(null);
        phonenum.putClientProperty("JTextField.placeholderText", "Số điện thoại");

        JPanel phonePanel = new JPanel(new BorderLayout());
        phonePanel.setBackground(new Color(242, 242, 242));
        phonePanel.setBorder(AuthViewStyle.createInputUnderlineBorder());
        phonePanel.setPreferredSize(new Dimension(200, UIScale.scale(45)));

        JLabel phoneIcon = new JLabel(scaleIcon("/icon/phone.png", UIScale.scale(18), UIScale.scale(18)));
        phoneIcon.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(5), 0, UIScale.scale(5)));

        phonePanel.add(phoneIcon, BorderLayout.WEST);
        phonePanel.add(phonenum, BorderLayout.CENTER);

        // ---- EMAIL ----
        JTextField email = new JTextField();
        email.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(20)));
        email.setBackground(new Color(242, 242, 242));
        email.setBorder(null);
        email.putClientProperty("JTextField.placeholderText", "Email");

        JPanel emailPanel = new JPanel(new BorderLayout());
        emailPanel.setBackground(new Color(242, 242, 242));
        emailPanel.setBorder(AuthViewStyle.createInputUnderlineBorder());
        emailPanel.setPreferredSize(new Dimension(200, UIScale.scale(45)));

        JLabel emailIcon = new JLabel(scaleIcon("/icon/mail.png", UIScale.scale(18), UIScale.scale(18)));
        emailIcon.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(5), 0, UIScale.scale(5)));

        emailPanel.add(emailIcon, BorderLayout.WEST);
        emailPanel.add(email, BorderLayout.CENTER);

        JButton sendOtpBtn = new JButton("Gửi mã OTP");
        sendOtpBtn.setFont(new Font("Segoe UI", Font.BOLD, UIScale.scale(15)));
        sendOtpBtn.setBackground(new Color(187, 220, 182));
        sendOtpBtn.setForeground(new Color(16, 110, 0));
        sendOtpBtn.setBorderPainted(false);
        sendOtpBtn.setMargin(OTP_BUTTON_MARGIN);
        sendOtpBtn.setPreferredSize(OTP_BUTTON_SIZE);
        // ---- ROW chứa 2 field ----
        JPanel contactRow = new JPanel(new BorderLayout(OTP_ROW_GAP, 0));
        contactRow.setBackground(new Color(242, 242, 242));

        contactRow.add(emailPanel, BorderLayout.CENTER);
        contactRow.add(sendOtpBtn, BorderLayout.EAST);

        JTextField otpField = new JTextField();
        otpField.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(20)));
        otpField.setBackground(new Color(242, 242, 242));
        otpField.setBorder(null);
        otpField.putClientProperty("JTextField.placeholderText", "Nhập OTP email");

        JLabel otpIcon = new JLabel(scaleIcon("/icon/otp.png", UIScale.scale(18), UIScale.scale(18)));
        otpIcon.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(5), 0, UIScale.scale(5)));

        JPanel otpPanel = new JPanel(new BorderLayout());
        otpPanel.setBackground(new Color(242, 242, 242));
        otpPanel.setBorder(AuthViewStyle.createInputUnderlineBorder());
        otpPanel.setPreferredSize(new Dimension(200, UIScale.scale(45)));
        otpPanel.add(otpField, BorderLayout.CENTER);
        otpPanel.add(otpIcon, BorderLayout.WEST);

        JButton verifyOtpBtn = new JButton("Xác thực OTP");
        verifyOtpBtn.setFont(new Font("Segoe UI", Font.BOLD, UIScale.scale(15)));
        verifyOtpBtn.setBackground(new Color(187, 220, 182));
        verifyOtpBtn.setForeground(new Color(16, 110, 0));
        verifyOtpBtn.setBorderPainted(false);
        verifyOtpBtn.setMargin(OTP_BUTTON_MARGIN);
        verifyOtpBtn.setPreferredSize(OTP_BUTTON_SIZE);

        JPanel otpRow = new JPanel(new BorderLayout(OTP_ROW_GAP, 0));
        otpRow.setBackground(new Color(242, 242, 242));
        otpRow.add(otpPanel, BorderLayout.CENTER);
        otpRow.add(verifyOtpBtn, BorderLayout.EAST);

        // ===== PASSWORD =====
        JPasswordField password = new JPasswordField();
        password.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(20)));
        password.setBackground(new Color(242, 242, 242));
        password.setBorder(null);
        password.putClientProperty("JTextField.placeholderText", "Mật khẩu");

        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(new Color(242, 242, 242));
        passPanel.setBorder(AuthViewStyle.createInputUnderlineBorder());
        passPanel.setPreferredSize(new Dimension(200, UIScale.scale(45)));

        JLabel passIcon = new JLabel(scaleIcon("/icon/pass.png", UIScale.scale(18), UIScale.scale(18)));
        passIcon.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(5), 0, UIScale.scale(5)));

        passPanel.add(passIcon, BorderLayout.WEST);
        passPanel.add(password, BorderLayout.CENTER);

        // ===== XÁC NHẬN PASSWORD =====
        JPasswordField checkPassword = new JPasswordField();
        checkPassword.setFont(new Font("Segoe UI", Font.PLAIN, UIScale.scale(20)));
        checkPassword.setBackground(new Color(242, 242, 242));
        checkPassword.setBorder(null);
        checkPassword.putClientProperty("JTextField.placeholderText", "Xác nhận mật khẩu");

        JPanel checkpassPanel = new JPanel(new BorderLayout());
        checkpassPanel.setBackground(new Color(242, 242, 242));
        checkpassPanel.setBorder(AuthViewStyle.createInputUnderlineBorder());
        checkpassPanel.setPreferredSize(new Dimension(200, UIScale.scale(45)));

        JLabel checkpassIcon = new JLabel(scaleIcon("/icon/pass.png", UIScale.scale(18), UIScale.scale(18)));
        checkpassIcon.setBorder(BorderFactory.createEmptyBorder(0, UIScale.scale(5), 0, UIScale.scale(5)));

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
        registerBtn.setPreferredSize(new Dimension(200, UIScale.scale(50)));
        registerBtn.setEnabled(true);
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
                AppDialog.showInfo(this, res.message());
            } else {
                AppDialog.showError(this, res.message());
            }
        });

        registerBtn.addActionListener(e -> {
            String fullName = username.getText().trim();
            String phone = phonenum.getText().trim();
            String emailValue = email.getText().trim();
            String passwordValue = new String(password.getPassword());
            String confirmPassword = new String(checkPassword.getPassword());

            if (phone.isEmpty()) {
                AppDialog.showError(this, "Vui lòng nhập số điện thoại.");
                return;
            }
            if (emailValue.isEmpty()) {
                AppDialog.showError(this, "Vui lòng nhập email.");
                return;
            }
            if (fullName.isEmpty()) {
                AppDialog.showError(this, "Vui lòng nhập họ và tên.");
                return;
            }
            if (passwordValue.isEmpty()) {
                AppDialog.showError(this, "Vui lòng nhập mật khẩu.");
                return;
            }
            if (confirmPassword.isEmpty()) {
                AppDialog.showError(this, "Vui lòng nhập xác nhận mật khẩu.");
                return;
            }
            if (!passwordValue.equals(confirmPassword)) {
                AppDialog.showError(this, "Mật khẩu xác nhận không khớp.");
                return;
            }
            if (!registerOtpVerified[0]) {
                AppDialog.showError(this, "Vui lòng xác thực OTP trước khi đăng ký.");
                return;
            }

            RegisterRequest req = new RegisterRequest(
                    passwordValue,
                    fullName,
                    phone,
                    emailValue

            );
            AuthResult res = authController.register(req);
            if (res.success()) {
                AppDialog.showInfo(this, res.message());
                cardLayout.show(parentPanel, "LOGIN");
            } else {
                AppDialog.showError(this, res.message());
            }
        });

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

