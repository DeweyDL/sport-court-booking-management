package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.YearMonth;

public class StaffFormDialog extends JDialog {
    public interface FormSubmitHandler<T> {
        void submit(T request) throws Exception;
    }

    private static final Color BG = new Color(246, 247, 251);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color BORDER = new Color(221, 226, 235);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color CANCEL_BG = new Color(226, 232, 240);

    private final boolean updateMode;
    private final StaffDetailResponse detail;

    private JTextField txtHoTen;
    private JTextField txtSdt;
    private JTextField txtCccd;
    private JTextField txtEmail;
    private JTextField txtNgaySinh;
    private JTextField txtDiaChi;
    private JTextField txtNgayVaoLam;

    private JRadioButton rdoQuanLy;
    private JRadioButton rdoNhanVien;

    private StaffCreateRequest createRequest;
    private StaffUpdateRequest updateRequest;

    private FormSubmitHandler<StaffCreateRequest> createSubmitHandler;
    private FormSubmitHandler<StaffUpdateRequest> updateSubmitHandler;

    private StaffFormDialog(boolean updateMode, StaffDetailResponse detail) {
        this.updateMode = updateMode;
        this.detail = detail;

        setTitle(updateMode ? "Cập nhật nhân viên" : "Thêm nhân viên");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 900);
        setResizable(false);

        initComponents();
        initLayout();
        fillDataIfNeeded();
    }

    public static StaffFormDialog createMode() {
        return new StaffFormDialog(false, null);
    }

    public static StaffFormDialog updateMode(StaffDetailResponse detail) {
        return new StaffFormDialog(true, detail);
    }

    public void setCreateSubmitHandler(FormSubmitHandler<StaffCreateRequest> createSubmitHandler) {
        this.createSubmitHandler = createSubmitHandler;
    }

    public void setUpdateSubmitHandler(FormSubmitHandler<StaffUpdateRequest> updateSubmitHandler) {
        this.updateSubmitHandler = updateSubmitHandler;
    }

    private void initComponents() {
        txtHoTen = createTextField();
        txtSdt = createTextField();
        txtCccd = createTextField();
        txtEmail = createTextField();
        txtNgaySinh = createTextField();
        txtDiaChi = createTextField();
        txtNgayVaoLam = createTextField();

        txtNgaySinh.setEditable(false);
        txtNgayVaoLam.setEditable(false);

        rdoQuanLy = createRadio("Quản lý");
        rdoNhanVien = createRadio("Nhân viên");

        ButtonGroup group = new ButtonGroup();
        group.add(rdoQuanLy);
        group.add(rdoNhanVien);
        rdoNhanVien.setSelected(true);
    }

    private void initLayout() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG);
        outer.setBorder(BorderFactory.createEmptyBorder(28, 44, 28, 44));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(22, 34, 24, 34));

        JLabel title = new JLabel(updateMode ? "Cập nhật nhân viên" : "Thêm nhân viên mới", SwingConstants.CENTER);
        title.setFont(fontBold(30));
        title.setForeground(TEXT_DARK);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 22, 0));

        JPanel form = new JPanel();
        form.setBackground(CARD_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        addFormRow(form, createFieldRow("Họ và tên nhân viên", txtHoTen), 72);
        addFormRow(form, createFieldRow("Số điện thoại", txtSdt), 72);
        addFormRow(form, createFieldRow("Căn cước công dân", txtCccd), 72);
        addFormRow(form, createFieldRow("Email", txtEmail), 72);
        addFormRow(form, createDateRow("Ngày sinh", txtNgaySinh), 76);
        addFormRow(form, createFieldRow("Địa chỉ", txtDiaChi), 72);
        addFormRow(form, createDateRow("Ngày vào làm", txtNgayVaoLam), 76);
        addFormRow(form, createRoleRow(), 88);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 20, 0));
        buttons.setBackground(CARD_BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JButton btnCancel = new RoundButton("Hủy", CANCEL_BG, Color.BLACK);
        JButton btnSave = new RoundButton(updateMode ? "Lưu thay đổi" : "Tạo nhân viên", GREEN, Color.WHITE);

        btnCancel.addActionListener(e -> {
            createRequest = null;
            updateRequest = null;
            dispose();
        });

        btnSave.addActionListener(e -> save());

        buttons.add(btnCancel);
        buttons.add(btnSave);

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        outer.add(card, BorderLayout.CENTER);
        getContentPane().add(outer, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel form, JPanel row, int height) {
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        row.setPreferredSize(new Dimension(0, height));
        form.add(row);
        form.add(Box.createVerticalStrut(8));
    }

    private JPanel createFieldRow(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);

        JLabel lbl = new JLabel(label);
        lbl.setFont(fontBold(15));
        lbl.setForeground(TEXT_DARK);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDateRow(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(CARD_BG);

        JLabel lbl = new JLabel(label);
        lbl.setFont(fontBold(15));
        lbl.setForeground(TEXT_DARK);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(CARD_BG);

        JButton btnChoose = new JButton("Chọn");
        JButton btnClear = new JButton("Xóa");

        styleSmallButton(btnChoose);
        styleSmallButton(btnClear);

        btnChoose.addActionListener(e -> {
            LocalDate current = parseDate(field.getText());
            LocalDate selected = DatePickerDialog.showPicker(this, current == null ? LocalDate.now() : current);
            if (selected != null) {
                field.setText(formatDate(selected));
            }
        });

        btnClear.addActionListener(e -> field.setText(""));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(CARD_BG);
        buttonPanel.add(btnChoose);
        buttonPanel.add(btnClear);

        inputPanel.add(field, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRoleRow() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD_BG);

        JLabel lbl = new JLabel("Chức vụ");
        lbl.setFont(fontBold(15));
        lbl.setForeground(TEXT_DARK);

        JPanel rolePanel = new JPanel(new GridLayout(1, 2, 16, 0));
        rolePanel.setBackground(CARD_BG);

        rolePanel.add(createRoleOptionBox(rdoQuanLy));
        rolePanel.add(createRoleOptionBox(rdoNhanVien));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(rolePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRoleOptionBox(JRadioButton radio) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        radio.setHorizontalAlignment(SwingConstants.LEFT);
        radio.setVerticalAlignment(SwingConstants.CENTER);
        radio.setFont(fontBold(16));
        radio.setForeground(TEXT_DARK);
        radio.setBackground(CARD_BG);
        radio.setFocusPainted(false);
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.add(radio, BorderLayout.CENTER);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                radio.setSelected(true);
            }
        });

        return panel;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(0, 38));
        field.setFont(fontPlain(16));
        field.setForeground(TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)
        ));
        return field;
    }

    private JRadioButton createRadio(String text) {
        JRadioButton radio = new JRadioButton(text);
        radio.setFont(fontBold(16));
        radio.setForeground(TEXT_DARK);
        radio.setBackground(CARD_BG);
        radio.setFocusPainted(false);
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return radio;
    }

    private void styleSmallButton(JButton button) {
        button.setPreferredSize(new Dimension(96, 38));
        button.setFont(fontPlain(15));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void fillDataIfNeeded() {
        if (!updateMode || detail == null) {
            txtNgayVaoLam.setText(formatDate(LocalDate.now()));
            return;
        }

        txtHoTen.setText(safe(detail.getHoTen()));
        txtSdt.setText(safe(detail.getSdt()));
        txtCccd.setText(safe(detail.getCccd()));
        txtEmail.setText(safe(detail.getEmail()));
        txtDiaChi.setText(safe(detail.getDiaChi()));

        if (detail.getNgaySinh() != null) {
            txtNgaySinh.setText(formatDate(detail.getNgaySinh()));
        }

        if (detail.getNgayVaoLam() != null) {
            txtNgayVaoLam.setText(formatDate(detail.getNgayVaoLam()));
        }

        if (detail.isQuanLy()) {
            rdoQuanLy.setSelected(true);
        } else {
            rdoNhanVien.setSelected(true);
        }
    }

    private void save() {
        StaffCreateRequest createData = null;
        StaffUpdateRequest updateData = null;

        if (isBlank(txtHoTen.getText())) {
            showInvalid("Họ tên không được để trống.");
            txtHoTen.requestFocus();
            return;
        }

        if (isBlank(txtSdt.getText())) {
            showInvalid("Số điện thoại không được để trống.");
            txtSdt.requestFocus();
            return;
        }

        if (isBlank(txtCccd.getText())) {
            showInvalid("Căn cước công dân không được để trống.");
            txtCccd.requestFocus();
            return;
        }

        if (isBlank(txtNgayVaoLam.getText())) {
            showInvalid("Ngày vào làm không được để trống.");
            return;
        }

        LocalDate ngaySinh = parseDate(txtNgaySinh.getText());
        LocalDate ngayVaoLam = parseDate(txtNgayVaoLam.getText());

        if (!isBlank(txtNgaySinh.getText()) && ngaySinh == null) {
            showInvalid("Ngày sinh không hợp lệ.");
            return;
        }

        if (ngayVaoLam == null) {
            showInvalid("Ngày vào làm không hợp lệ.");
            return;
        }

        try {
            if (updateMode) {
                updateData = buildUpdateRequest(ngaySinh, ngayVaoLam);

                if (updateSubmitHandler != null) {
                    updateSubmitHandler.submit(updateData);
                }

                updateRequest = updateData;
            } else {
                createData = buildCreateRequest(ngaySinh, ngayVaoLam);

                if (createSubmitHandler != null) {
                    createSubmitHandler.submit(createData);
                }

                createRequest = createData;
            }

            dispose();
        } catch (Exception ex) {
            String message = ex.getMessage();

            if (message == null || message.trim().isEmpty()) {
                message = "Không thể lưu thông tin nhân viên.";
            }

            showInvalid(message);
        }
    }

    private StaffCreateRequest buildCreateRequest(LocalDate ngaySinh, LocalDate ngayVaoLam) {
        StaffCreateRequest request = new StaffCreateRequest();

        request.setHoTen(txtHoTen.getText().trim());
        request.setSdt(txtSdt.getText().trim());
        request.setCccd(txtCccd.getText().trim());
        request.setEmail(txtEmail.getText().trim());
        request.setNgaySinh(ngaySinh);
        request.setDiaChi(txtDiaChi.getText().trim());
        request.setNgayVaoLam(ngayVaoLam);
        request.setQuanLy(rdoQuanLy.isSelected());
        request.setMaLoaiNv(null);

        return request;
    }

    private StaffUpdateRequest buildUpdateRequest(LocalDate ngaySinh, LocalDate ngayVaoLam) {
        StaffUpdateRequest request = new StaffUpdateRequest();

        request.setMaNv(detail.getMaNv());
        request.setUserId(detail.getUserId());
        request.setHoTen(txtHoTen.getText().trim());
        request.setSdt(txtSdt.getText().trim());
        request.setCccd(txtCccd.getText().trim());
        request.setEmail(txtEmail.getText().trim());
        request.setNgaySinh(ngaySinh);
        request.setDiaChi(txtDiaChi.getText().trim());
        request.setNgayVaoLam(ngayVaoLam);
        request.setQuanLy(rdoQuanLy.isSelected());

        if (detail.isQuanLy() == rdoQuanLy.isSelected()) {
            request.setMaLoaiNv(detail.getMaLoaiNv());
        } else {
            request.setMaLoaiNv(null);
        }

        return request;
    }

    private LocalDate parseDate(String value) {
        try {
            if (isBlank(value)) {
                return null;
            }

            String text = value.trim();

            if (text.matches("\\d{2}/\\d{2}/\\d{4}")) {
                String[] parts = text.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);

                return LocalDate.of(year, month, day);
            }

            if (text.matches("\\d{2}/\\d{2}/\\d{2}")) {
                String[] parts = text.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int yearTwoDigits = Integer.parseInt(parts[2]);
                int year = yearTwoDigits >= 50 ? 1900 + yearTwoDigits : 2000 + yearTwoDigits;

                return LocalDate.of(year, month, day);
            }

            return LocalDate.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }

        return String.format(
                "%02d/%02d/%04d",
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear()
        );
    }

    private void showInvalid(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
    }

    public StaffCreateRequest getCreateRequest() {
        return createRequest;
    }

    public StaffUpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    private Font fontPlain(int size) {
        return new Font("Segoe UI", Font.PLAIN, size);
    }

    private Font fontBold(int size) {
        return new Font("Segoe UI", Font.BOLD, size);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class RoundButton extends JButton {
        private final Color bg;
        private final Color fg;

        RoundButton(String text, Color bg, Color fg) {
            super(text);
            this.bg = bg;
            this.fg = fg;

            setForeground(fg);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(0, 52));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D graphics = (java.awt.Graphics2D) g.create();

            graphics.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setColor(bg);
            graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            graphics.dispose();

            super.paintComponent(g);
        }
    }

    private static class DatePickerDialog extends JDialog {
        private JComboBox<Integer> cboYear;
        private JComboBox<Integer> cboMonth;
        private JPanel daysPanel;
        private LocalDate selectedDate;

        private DatePickerDialog(Window owner, LocalDate initialDate) {
            super(owner, "Chọn ngày", ModalityType.APPLICATION_MODAL);

            selectedDate = null;

            setSize(520, 460);
            setResizable(false);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(12, 12));

            JPanel top = new JPanel(new GridLayout(1, 2, 14, 0));
            top.setBorder(BorderFactory.createEmptyBorder(18, 18, 6, 18));

            cboYear = new JComboBox<>();
            cboMonth = new JComboBox<>();

            for (int year = 1950; year <= 2035; year++) {
                cboYear.addItem(year);
            }

            for (int month = 1; month <= 12; month++) {
                cboMonth.addItem(month);
            }

            styleCombo(cboYear);
            styleCombo(cboMonth);

            cboYear.setSelectedItem(initialDate.getYear());
            cboMonth.setSelectedItem(initialDate.getMonthValue());

            top.add(cboYear);
            top.add(cboMonth);

            daysPanel = new JPanel(new GridLayout(0, 7, 8, 8));
            daysPanel.setBorder(BorderFactory.createEmptyBorder(8, 18, 18, 18));

            add(top, BorderLayout.NORTH);
            add(daysPanel, BorderLayout.CENTER);

            cboYear.addActionListener(e -> renderDays(initialDate));
            cboMonth.addActionListener(e -> renderDays(initialDate));

            renderDays(initialDate);
        }

        private void styleCombo(JComboBox<Integer> combo) {
            combo.setEditable(false);
            combo.setFocusable(false);
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            combo.setPreferredSize(new Dimension(0, 36));
            combo.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
            combo.setBackground(Color.WHITE);
        }

        private void renderDays(LocalDate initialDate) {
            daysPanel.removeAll();

            String[] headers = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

            for (String header : headers) {
                JLabel label = new JLabel(header, SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 15));
                daysPanel.add(label);
            }

            int year = (Integer) cboYear.getSelectedItem();
            int month = (Integer) cboMonth.getSelectedItem();

            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate firstDay = yearMonth.atDay(1);
            int firstColumn = firstDay.getDayOfWeek().getValue();

            for (int i = 1; i < firstColumn; i++) {
                daysPanel.add(new JLabel(""));
            }

            int totalDays = yearMonth.lengthOfMonth();

            for (int day = 1; day <= totalDays; day++) {
                JButton button = new JButton(String.valueOf(day));
                button.setFont(new Font("Segoe UI", Font.BOLD, 15));
                button.setFocusPainted(false);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                LocalDate value = LocalDate.of(year, month, day);

                if (value.equals(initialDate)) {
                    button.setBackground(new Color(219, 234, 254));
                } else {
                    button.setBackground(Color.WHITE);
                }

                button.addActionListener(e -> {
                    selectedDate = value;
                    dispose();
                });

                daysPanel.add(button);
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        }

        static LocalDate showPicker(Window owner, LocalDate initialDate) {
            DatePickerDialog dialog = new DatePickerDialog(owner, initialDate);
            dialog.setVisible(true);
            return dialog.selectedDate;
        }
    }
}
