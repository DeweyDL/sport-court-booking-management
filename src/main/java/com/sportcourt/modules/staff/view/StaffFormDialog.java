package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public class StaffFormDialog extends JDialog {
    private enum Mode {
        CREATE,
        UPDATE
    }

    private static final Color PAGE_BG = new Color(246, 247, 251);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color GREEN_DARK = new Color(22, 163, 74);
    private static final Color CANCEL_BG = new Color(226, 232, 240);

    private final Mode mode;
    private final StaffDetailResponse detail;
    private boolean submitted;

    private JTextField txtHoTen;
    private JTextField txtSdt;
    private JTextField txtCccd;
    private JTextField txtEmail;
    private JTextField txtMaLoaiNv;

    private DatePickerField dateNgaySinh;
    private DatePickerField dateNgayVaoLam;

    private JRadioButton rdoQuanLy;
    private JRadioButton rdoNhanVien;

    private JButton btnCancel;
    private JButton btnSave;

    private StaffFormDialog(Mode mode, StaffDetailResponse detail) {
        this.mode = mode;
        this.detail = detail;
        this.submitted = false;

        setModal(true);
        setTitle(mode == Mode.CREATE ? "Thêm nhân viên" : "Cập nhật nhân viên");
        setSize(560, 690);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
        initLayout();
        initEvents();

        if (mode == Mode.UPDATE && detail != null) {
            fillData(detail);
        }
    }

    public static StaffFormDialog createMode() {
        return new StaffFormDialog(Mode.CREATE, null);
    }

    public static StaffFormDialog updateMode(StaffDetailResponse detail) {
        return new StaffFormDialog(Mode.UPDATE, detail);
    }

    private void initComponents() {
        txtHoTen = createTextField();
        txtSdt = createTextField();
        txtCccd = createTextField();
        txtEmail = createTextField();
        txtMaLoaiNv = createTextField();

        dateNgaySinh = new DatePickerField();
        dateNgayVaoLam = new DatePickerField();

        rdoQuanLy = createRoleRadio("Quản lý");
        rdoNhanVien = createRoleRadio("Nhân viên");
        rdoNhanVien.setSelected(true);

        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(rdoQuanLy);
        roleGroup.add(rdoNhanVien);

        btnCancel = new RoundedButton("Hủy", CANCEL_BG, TEXT_DARK);
        btnSave = new RoundedButton(
                mode == Mode.CREATE ? "Tạo nhân viên" : "Lưu thay đổi",
                GREEN,
                Color.WHITE
        );
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(18, 26, 18, 26));

        JLabel title = new JLabel(mode == Mode.CREATE ? "Thêm nhân viên mới" : "Cập nhật nhân viên");
        title.setFont(fontBold(25));
        title.setForeground(TEXT_DARK);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        int row = 0;
        row = addInput(form, gbc, row, "Họ và tên nhân viên", txtHoTen);
        row = addInput(form, gbc, row, "Số điện thoại", txtSdt);
        row = addInput(form, gbc, row, "Căn cước công dân", txtCccd);
        row = addInput(form, gbc, row, "Email", txtEmail);
        row = addInput(form, gbc, row, "Ngày sinh", dateNgaySinh);
        row = addInput(form, gbc, row, "Mã loại nhân viên", txtMaLoaiNv);
        row = addInput(form, gbc, row, "Ngày vào làm", dateNgayVaoLam);

        JLabel lblRole = new JLabel("Chức vụ");
        lblRole.setFont(fontBold(13));
        lblRole.setForeground(TEXT_DARK);

        gbc.gridy = row++;
        gbc.insets = new Insets(6, 0, 6, 0);
        form.add(lblRole, gbc);

        JPanel rolePanel = new JPanel(new GridLayout(1, 2, 12, 0));
        rolePanel.setBackground(CARD_BG);
        rolePanel.add(wrapRoleRadio(rdoQuanLy));
        rolePanel.add(wrapRoleRadio(rdoNhanVien));

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 14, 0);
        form.add(rolePanel, gbc);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 18, 0));
        buttons.setBackground(CARD_BG);
        buttons.add(btnCancel);
        buttons.add(btnSave);

        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private int addInput(JPanel form, GridBagConstraints gbc, int row, String label, JComponent input) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(fontBold(13));
        lbl.setForeground(TEXT_DARK);

        gbc.gridy = row++;
        gbc.insets = new Insets(4, 0, 4, 0);
        form.add(lbl, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 6, 0);
        form.add(input, gbc);

        return row;
    }

    private void initEvents() {
        btnCancel.addActionListener(e -> {
            submitted = false;
            dispose();
        });

        btnSave.addActionListener(e -> handleSubmit());

        btnSave.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSave.setBackground(GREEN_DARK);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnSave.setBackground(GREEN);
            }
        });
    }

    private void handleSubmit() {
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
        require(txtHoTen, "Họ và tên nhân viên");
        require(txtSdt, "Số điện thoại");
        require(txtCccd, "Căn cước công dân");
        require(txtEmail, "Email");
        require(txtMaLoaiNv, "Mã loại nhân viên");

        if (dateNgaySinh.getDate("Ngày sinh") == null) {
            throw new RuntimeException("Ngày sinh không được để trống.");
        }

        if (dateNgayVaoLam.getDate("Ngày vào làm") == null) {
            throw new RuntimeException("Ngày vào làm không được để trống.");
        }

        if (!txtSdt.getText().trim().matches("^0\\d{9}$")) {
            throw new RuntimeException("Số điện thoại phải gồm 10 số và bắt đầu bằng 0.");
        }

        if (!txtCccd.getText().trim().matches("\\d{12}")) {
            throw new RuntimeException("Căn cước công dân phải gồm đúng 12 chữ số.");
        }

        if (!txtEmail.getText().trim().matches("^[A-Za-z0-9._%+-]+@gmail\\.com$")) {
            throw new RuntimeException("Email phải có định dạng Gmail, ví dụ: nhanvien@gmail.com.");
        }
    }

    private void require(JTextField field, String name) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            throw new RuntimeException(name + " không được để trống.");
        }
    }

    public StaffCreateRequest getCreateRequest() {
        if (mode != Mode.CREATE || !submitted) {
            return null;
        }

        StaffCreateRequest request = new StaffCreateRequest();

        request.setHoTen(txtHoTen.getText().trim());
        request.setSdt(txtSdt.getText().trim());
        request.setCccd(txtCccd.getText().trim());
        request.setEmail(txtEmail.getText().trim());
        request.setNgaySinh(dateNgaySinh.getDate("Ngày sinh"));

        request.setDiaChi(null);
        request.setMaCn(null);
        request.setMaLoaiNv(txtMaLoaiNv.getText().trim());
        request.setNgayVaoLam(dateNgayVaoLam.getDate("Ngày vào làm"));
        request.setQuanLy(rdoQuanLy.isSelected());

        request.setCreateAccount(false);
        request.setUsername(null);
        request.setPassword(null);
        request.setRoleGroupId(null);

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
        request.setSdt(txtSdt.getText().trim());
        request.setCccd(txtCccd.getText().trim());
        request.setEmail(txtEmail.getText().trim());
        request.setNgaySinh(dateNgaySinh.getDate("Ngày sinh"));

        request.setDiaChi(null);
        request.setMaCn(null);
        request.setMaLoaiNv(txtMaLoaiNv.getText().trim());
        request.setNgayVaoLam(dateNgayVaoLam.getDate("Ngày vào làm"));
        request.setQuanLy(rdoQuanLy.isSelected());

        return request;
    }

    private void fillData(StaffDetailResponse detail) {
        txtHoTen.setText(safe(detail.getHoTen()));
        txtSdt.setText(safe(detail.getSdt()));
        txtCccd.setText(safe(detail.getCccd()));
        txtEmail.setText(safe(detail.getEmail()));

        dateNgaySinh.setDate(detail.getNgaySinh());
        dateNgayVaoLam.setDate(detail.getNgayVaoLam());

        txtMaLoaiNv.setText(safe(detail.getMaLoaiNv()));

        if (detail.isQuanLy()) {
            rdoQuanLy.setSelected(true);
        } else {
            rdoNhanVien.setSelected(true);
        }
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new java.awt.Dimension(390, 36));
        field.setFont(fontPlain(14));
        field.setForeground(TEXT_DARK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        return field;
    }

    private JRadioButton createRoleRadio(String text) {
        JRadioButton radio = new JRadioButton(text);
        radio.setFont(fontBold(14));
        radio.setForeground(TEXT_DARK);
        radio.setOpaque(false);
        radio.setFocusPainted(false);
        radio.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return radio;
    }

    private JPanel wrapRoleRadio(JRadioButton radio) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panel.add(radio, BorderLayout.CENTER);
        return panel;
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

    private class DatePickerField extends JPanel {
        private final JTextField textField;
        private final JButton btnPick;
        private final JButton btnClear;

        DatePickerField() {
            setLayout(new BorderLayout(8, 0));
            setOpaque(false);

            textField = createTextField();
            textField.setEditable(false);
            textField.setBackground(Color.WHITE);

            btnPick = new JButton("Chọn");
            btnPick.setFont(fontPlain(13));
            btnPick.setPreferredSize(new java.awt.Dimension(68, 36));
            btnPick.setFocusPainted(false);
            btnPick.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btnClear = new JButton("Xóa");
            btnClear.setFont(fontPlain(13));
            btnClear.setPreferredSize(new java.awt.Dimension(56, 36));
            btnClear.setFocusPainted(false);
            btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel rightPanel = new JPanel(new GridLayout(1, 2, 8, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(btnPick);
            rightPanel.add(btnClear);

            add(textField, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);

            btnPick.addActionListener(e -> showDateDialog());
            btnClear.addActionListener(e -> textField.setText(""));
        }

        void setDate(LocalDate date) {
            textField.setText(date == null ? "" : date.toString());
        }

        LocalDate getDate(String fieldName) {
            if (textField.getText() == null || textField.getText().trim().isEmpty()) {
                return null;
            }

            try {
                return LocalDate.parse(textField.getText().trim());
            } catch (DateTimeParseException ex) {
                throw new RuntimeException(fieldName + " không hợp lệ.");
            }
        }

        private void showDateDialog() {
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(owner, "Chọn ngày", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(380, 360);
            dialog.setResizable(false);
            dialog.setLocationRelativeTo(this);

            JPanel root = new JPanel(new BorderLayout(8, 8));
            root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            root.setBackground(Color.WHITE);

            LocalDate currentDate = getSafeCurrentDate();
            YearMonth[] currentMonth = {YearMonth.from(currentDate)};

            JComboBox<Integer> cboYear = new JComboBox<>();
            int currentYear = LocalDate.now().getYear();

            for (int year = currentYear - 80; year <= currentYear + 10; year++) {
                cboYear.addItem(year);
            }

            JComboBox<Integer> cboMonth = new JComboBox<>();
            for (int month = 1; month <= 12; month++) {
                cboMonth.addItem(month);
            }

            cboYear.setSelectedItem(currentMonth[0].getYear());
            cboMonth.setSelectedItem(currentMonth[0].getMonthValue());

            JPanel selectorPanel = new JPanel(new GridLayout(1, 2, 8, 0));
            selectorPanel.setBackground(Color.WHITE);
            selectorPanel.add(cboYear);
            selectorPanel.add(cboMonth);

            JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
            grid.setBackground(Color.WHITE);

            Runnable rebuild = () -> {
                Integer year = (Integer) cboYear.getSelectedItem();
                Integer month = (Integer) cboMonth.getSelectedItem();

                if (year == null || month == null) {
                    return;
                }

                currentMonth[0] = YearMonth.of(year, month);
                rebuildCalendarGrid(grid, currentMonth[0], dialog);
            };

            cboYear.addActionListener(e -> rebuild.run());
            cboMonth.addActionListener(e -> rebuild.run());

            rebuild.run();

            root.add(selectorPanel, BorderLayout.NORTH);
            root.add(grid, BorderLayout.CENTER);

            dialog.setContentPane(root);
            dialog.setVisible(true);
        }

        private LocalDate getSafeCurrentDate() {
            if (textField.getText() == null || textField.getText().trim().isEmpty()) {
                return LocalDate.now();
            }

            try {
                return LocalDate.parse(textField.getText().trim());
            } catch (Exception ex) {
                return LocalDate.now();
            }
        }

        private void rebuildCalendarGrid(JPanel grid, YearMonth month, JDialog dialog) {
            grid.removeAll();

            String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

            for (String day : days) {
                JLabel label = new JLabel(day, SwingConstants.CENTER);
                label.setFont(fontBold(12));
                grid.add(label);
            }

            LocalDate firstDay = month.atDay(1);
            int blankCount = firstDay.getDayOfWeek().getValue() - 1;

            for (int i = 0; i < blankCount; i++) {
                grid.add(new JLabel(""));
            }

            for (int day = 1; day <= month.lengthOfMonth(); day++) {
                int selectedDay = day;
                JButton button = new JButton(String.valueOf(day));
                button.setFocusPainted(false);
                button.setBackground(Color.WHITE);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                button.addActionListener(e -> {
                    LocalDate selectedDate = month.atDay(selectedDay);
                    textField.setText(selectedDate.toString());
                    dialog.dispose();
                });

                grid.add(button);
            }

            grid.revalidate();
            grid.repaint();
        }
    }

    private static class RoundedButton extends JButton {
        RoundedButton(String text, Color background, Color foreground) {
            super(text);

            setBackground(background);
            setForeground(foreground);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setPreferredSize(new java.awt.Dimension(190, 42));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(getBackground());
            graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
            graphics.dispose();

            super.paintComponent(g);
        }
    }
}