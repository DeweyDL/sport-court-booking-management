package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class StaffFormDialog extends JDialog {
    private enum Mode {
        CREATE,
        UPDATE
    }

    private final Mode mode;
    private final StaffDetailResponse detail;
    private boolean submitted;

    private JTextField txtHoTen;
    private JTextField txtNgaySinh;
    private JTextField txtSdt;
    private JTextField txtEmail;
    private JTextField txtDiaChi;

    private JTextField txtMaCn;
    private JTextField txtMaLoaiNv;
    private JTextField txtNgayVaoLam;
    private JTextField txtCccd;
    private JCheckBox chkQuanLy;

    private JPanel accountPanel;
    private JCheckBox chkCreateAccount;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtRoleGroupId;

    private JButton btnSave;
    private JButton btnCancel;

    private StaffFormDialog(Mode mode, StaffDetailResponse detail) {
        this.mode = mode;
        this.detail = detail;
        this.submitted = false;

        setModal(true);
        setTitle(mode == Mode.CREATE ? "Thêm mới nhân viên" : "Cập nhật thông tin nhân viên");
        setSize(560, 680);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
        initLayout();
        initEvents();

        if (mode == Mode.UPDATE && detail != null) {
            fillData(detail);
            hideAccountFields();
        }

        toggleAccountFields();
    }

    public static StaffFormDialog createMode() {
        return new StaffFormDialog(Mode.CREATE, null);
    }

    public static StaffFormDialog updateMode(StaffDetailResponse detail) {
        return new StaffFormDialog(Mode.UPDATE, detail);
    }

    private void initComponents() {
        txtHoTen = new JTextField();
        txtNgaySinh = new JTextField();
        txtSdt = new JTextField();
        txtEmail = new JTextField();
        txtDiaChi = new JTextField();

        txtMaCn = new JTextField();
        txtMaLoaiNv = new JTextField();
        txtNgayVaoLam = new JTextField();
        txtCccd = new JTextField();
        chkQuanLy = new JCheckBox("Là quản lý chi nhánh");

        chkCreateAccount = new JCheckBox("Tạo tài khoản đăng nhập");
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtRoleGroupId = new JTextField();

        btnSave = StaffTheme.primaryButton("Lưu");
        btnCancel = StaffTheme.secondaryButton("Huỷ");

        txtNgaySinh.setToolTipText("Định dạng: yyyy-MM-dd");
        txtNgayVaoLam.setToolTipText("Định dạng: yyyy-MM-dd");
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        mainPanel.setBackground(StaffTheme.BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        StaffTheme.applyCard(formPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addSectionTitle(formPanel, gbc, row++, "Thông tin cá nhân");
        addRow(formPanel, gbc, row++, "Họ tên:", txtHoTen);
        addRow(formPanel, gbc, row++, "Ngày sinh:", txtNgaySinh);
        addRow(formPanel, gbc, row++, "Số điện thoại:", txtSdt);
        addRow(formPanel, gbc, row++, "Email:", txtEmail);
        addRow(formPanel, gbc, row++, "Địa chỉ:", txtDiaChi);

        addSectionTitle(formPanel, gbc, row++, "Thông tin nhân viên");
        addRow(formPanel, gbc, row++, "Mã chi nhánh:", txtMaCn);
        addRow(formPanel, gbc, row++, "Mã loại nhân viên:", txtMaLoaiNv);
        addRow(formPanel, gbc, row++, "Ngày vào làm:", txtNgayVaoLam);
        addRow(formPanel, gbc, row++, "CCCD:", txtCccd);

        gbc.gridx = 1;
        gbc.gridy = row++;
        gbc.weightx = 1;
        formPanel.add(chkQuanLy, gbc);

        accountPanel = new JPanel(new GridBagLayout());
        accountPanel.setOpaque(false);

        GridBagConstraints accountGbc = new GridBagConstraints();
        accountGbc.insets = new Insets(7, 7, 7, 7);
        accountGbc.fill = GridBagConstraints.HORIZONTAL;

        int accountRow = 0;
        addSectionTitle(accountPanel, accountGbc, accountRow++, "Tài khoản đăng nhập");

        accountGbc.gridx = 1;
        accountGbc.gridy = accountRow++;
        accountGbc.weightx = 1;
        accountPanel.add(chkCreateAccount, accountGbc);

        addRow(accountPanel, accountGbc, accountRow++, "Username:", txtUsername);
        addRow(accountPanel, accountGbc, accountRow++, "Password:", txtPassword);
        addRow(accountPanel, accountGbc, accountRow++, "Mã nhóm quyền:", txtRoleGroupId);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        formPanel.add(accountPanel, gbc);
        gbc.gridwidth = 1;

        buttonPanel.setOpaque(false);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    private void addSectionTitle(JPanel panel, GridBagConstraints gbc, int row, String title) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        JLabel label = new JLabel(title);
        label.setFont(StaffTheme.fontBold(15));
        panel.add(label, gbc);

        gbc.gridwidth = 1;
    }

    private void initEvents() {
        btnSave.addActionListener(e -> handleSave());

        btnCancel.addActionListener(e -> {
            submitted = false;
            dispose();
        });

        chkCreateAccount.addActionListener(e -> toggleAccountFields());
    }

    private void hideAccountFields() {
        chkCreateAccount.setSelected(false);
        accountPanel.setVisible(false);
    }

    private void toggleAccountFields() {
        boolean enabled = mode == Mode.CREATE && chkCreateAccount.isSelected();

        txtUsername.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
        txtRoleGroupId.setEnabled(enabled);
    }

    private void fillData(StaffDetailResponse detail) {
        txtHoTen.setText(safe(detail.getHoTen()));
        txtNgaySinh.setText(detail.getNgaySinh() == null ? "" : detail.getNgaySinh().toString());
        txtSdt.setText(safe(detail.getSdt()));
        txtEmail.setText(safe(detail.getEmail()));
        txtDiaChi.setText(safe(detail.getDiaChi()));

        txtMaCn.setText(safe(detail.getMaCn()));
        txtMaLoaiNv.setText(safe(detail.getMaLoaiNv()));
        txtNgayVaoLam.setText(detail.getNgayVaoLam() == null ? "" : detail.getNgayVaoLam().toString());
        txtCccd.setText(safe(detail.getCccd()));
        chkQuanLy.setSelected(detail.isQuanLy());
    }

    private void handleSave() {
        try {
            validateForm();
            submitted = true;
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Dữ liệu không hợp lệ",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void validateForm() {
        if (isBlank(txtHoTen.getText())) {
            throw new RuntimeException("Họ tên không được để trống.");
        }

        if (parseDate(txtNgaySinh.getText(), "Ngày sinh") == null) {
            throw new RuntimeException("Ngày sinh không được để trống.");
        }

        if (isBlank(txtSdt.getText())) {
            throw new RuntimeException("Số điện thoại không được để trống.");
        }

        if (isBlank(txtEmail.getText())) {
            throw new RuntimeException("Email không được để trống.");
        }

        if (isBlank(txtMaCn.getText())) {
            throw new RuntimeException("Mã chi nhánh không được để trống.");
        }

        if (isBlank(txtMaLoaiNv.getText())) {
            throw new RuntimeException("Mã loại nhân viên không được để trống.");
        }

        if (parseDate(txtNgayVaoLam.getText(), "Ngày vào làm") == null) {
            throw new RuntimeException("Ngày vào làm không được để trống.");
        }

        if (isBlank(txtCccd.getText())) {
            throw new RuntimeException("CCCD không được để trống.");
        }

        if (mode == Mode.CREATE && chkCreateAccount.isSelected()) {
            if (isBlank(txtUsername.getText())) {
                throw new RuntimeException("Username không được để trống.");
            }

            if (txtPassword.getPassword().length == 0) {
                throw new RuntimeException("Password không được để trống.");
            }

            if (isBlank(txtRoleGroupId.getText())) {
                throw new RuntimeException("Mã nhóm quyền không được để trống.");
            }
        }
    }

    public StaffCreateRequest getCreateRequest() {
        if (mode != Mode.CREATE || !submitted) {
            return null;
        }

        StaffCreateRequest request = new StaffCreateRequest();
        request.setHoTen(txtHoTen.getText().trim());
        request.setNgaySinh(parseDate(txtNgaySinh.getText(), "Ngày sinh"));
        request.setSdt(txtSdt.getText().trim());
        request.setEmail(txtEmail.getText().trim());
        request.setDiaChi(txtDiaChi.getText().trim());

        request.setMaCn(txtMaCn.getText().trim());
        request.setMaLoaiNv(txtMaLoaiNv.getText().trim());
        request.setNgayVaoLam(parseDate(txtNgayVaoLam.getText(), "Ngày vào làm"));
        request.setCccd(txtCccd.getText().trim());
        request.setQuanLy(chkQuanLy.isSelected());

        request.setCreateAccount(chkCreateAccount.isSelected());
        request.setUsername(txtUsername.getText().trim());
        request.setPassword(new String(txtPassword.getPassword()));
        request.setRoleGroupId(txtRoleGroupId.getText().trim());

        return request;
    }

    public StaffUpdateRequest getUpdateRequest() {
        if (mode != Mode.UPDATE || !submitted || detail == null) {
            return null;
        }

        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setMaNv(detail.getMaNv());
        request.setUserId(detail.getUserId());

        request.setHoTen(txtHoTen.getText().trim());
        request.setNgaySinh(parseDate(txtNgaySinh.getText(), "Ngày sinh"));
        request.setSdt(txtSdt.getText().trim());
        request.setEmail(txtEmail.getText().trim());
        request.setDiaChi(txtDiaChi.getText().trim());

        request.setMaCn(txtMaCn.getText().trim());
        request.setMaLoaiNv(txtMaLoaiNv.getText().trim());
        request.setNgayVaoLam(parseDate(txtNgayVaoLam.getText(), "Ngày vào làm"));
        request.setCccd(txtCccd.getText().trim());
        request.setQuanLy(chkQuanLy.isSelected());

        return request;
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException(fieldName + " phải có định dạng yyyy-MM-dd.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
