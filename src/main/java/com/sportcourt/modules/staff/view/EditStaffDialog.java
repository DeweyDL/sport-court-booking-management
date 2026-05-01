package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class EditStaffDialog extends JDialog {
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color CANCEL = new Color(226, 232, 240);

    private final JTextField fullNameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField cccdField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField addressField = new JTextField();

    private final DatePickerField birthDateField = new DatePickerField(null);
    private final DatePickerField startDateField = new DatePickerField(null);

    private final JRadioButton managerRadio = new JRadioButton("Quản lý");
    private final JRadioButton staffRadio = new JRadioButton("Thu ngân");

    private final StaffDetailResponse detail;
    private final String currentBranchId;
    private final String managerTypeId;
    private final String staffTypeId;

    private StaffUpdateRequest result;

    public EditStaffDialog(Window owner,
                           StaffDetailResponse detail,
                           String currentBranchId,
                           String managerTypeId,
                           String staffTypeId) {
        super(owner, "Cập nhật nhân viên", ModalityType.APPLICATION_MODAL);

        this.detail = detail;
        this.currentBranchId = currentBranchId;
        this.managerTypeId = managerTypeId;
        this.staffTypeId = staffTypeId;

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(680, 560);
        setMinimumSize(new Dimension(640, 520));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        add(createContent(), BorderLayout.CENTER);
        fillData();
    }

    public StaffUpdateRequest showDialog() {
        result = null;
        setVisible(true);
        return result;
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel form = new JPanel();
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 28, 18, 28));
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Cập nhật nhân viên", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT);
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 14, 0));
        form.add(title);

        form.add(createTextInput("Họ và tên nhân viên", fullNameField));
        form.add(createTextInput("Số điện thoại", phoneField));
        form.add(createTextInput("Căn cước công dân", cccdField));
        form.add(createTextInput("Email", emailField));
        form.add(createDateInput("Ngày sinh", birthDateField));
        form.add(createTextInput("Địa chỉ", addressField));
        form.add(createDateInput("Ngày vào làm", startDateField));
        form.add(createRoleInput());
        form.add(createButtons());

        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(72);
        scrollPane.getVerticalScrollBar().setBlockIncrement(360);

        card.add(scrollPane, BorderLayout.CENTER);
        root.add(card, BorderLayout.CENTER);

        staffRadio.setSelected(true);

        return root;
    }

    private JPanel createTextInput(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel label = createLabel(labelText);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(TEXT);
        field.setPreferredSize(new Dimension(10, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 14, 0, 14)
        ));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDateInput(String labelText, DatePickerField dateField) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel label = createLabel(labelText);

        panel.add(label, BorderLayout.NORTH);
        panel.add(dateField, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRoleInput() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JLabel label = createLabel("Chức vụ");

        ButtonGroup group = new ButtonGroup();
        group.add(managerRadio);
        group.add(staffRadio);

        JPanel roleGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        roleGrid.setOpaque(false);
        roleGrid.setPreferredSize(new Dimension(10, 46));
        roleGrid.setMinimumSize(new Dimension(10, 46));
        roleGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        roleGrid.add(createRoleBox(managerRadio));
        roleGrid.add(createRoleBox(staffRadio));

        panel.add(label, BorderLayout.NORTH);
        panel.add(roleGrid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRoleBox(JRadioButton radioButton) {
        JPanel panel = new RoundedBoxPanel(18, Color.WHITE, BORDER);
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(10, 46));
        panel.setMinimumSize(new Dimension(10, 46));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        panel.setBorder(new EmptyBorder(0, 22, 0, 22));

        radioButton.setOpaque(false);
        radioButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        radioButton.setForeground(TEXT);
        radioButton.setHorizontalAlignment(SwingConstants.LEFT);
        radioButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.add(radioButton, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);

        JButton cancelButton = new RoundedButton("Hủy", CANCEL, null, 24);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.setForeground(TEXT);
        cancelButton.setPreferredSize(new Dimension(10, 46));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setOpaque(false);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new RoundedButton("Lưu thay đổi", GREEN, null, 24);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        saveButton.setForeground(Color.WHITE);
        saveButton.setPreferredSize(new Dimension(10, 46));
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setOpaque(false);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> save());

        panel.add(cancelButton);
        panel.add(saveButton);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT);
        return label;
    }


    private void fillData() {
        if (detail == null) {
            staffRadio.setSelected(true);
            return;
        }

        fullNameField.setText(nullToEmpty(detail.getHoTen()));
        phoneField.setText(nullToEmpty(detail.getSdt()));
        cccdField.setText(nullToEmpty(detail.getCccd()));
        emailField.setText(nullToEmpty(detail.getEmail()));
        addressField.setText(nullToEmpty(detail.getDiaChi()));

        birthDateField.setDate(detail.getNgaySinh());
        startDateField.setDate(detail.getNgayVaoLam());

        if (detail.isQuanLy()) {
            managerRadio.setSelected(true);
        } else {
            staffRadio.setSelected(true);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void save() {
        String hoTen = text(fullNameField);
        String sdt = text(phoneField);
        String cccd = text(cccdField);
        String email = text(emailField);
        String diaChi = text(addressField);

        if (hoTen.isEmpty()) {
            showInputError("Vui lòng nhập họ tên nhân viên.");
            fullNameField.requestFocusInWindow();
            return;
        }

        if (!sdt.matches("^0\\d{9}$")) {
            showInputError("Số điện thoại phải có 10 số và bắt đầu bằng 0.");
            phoneField.requestFocusInWindow();
            return;
        }

        if (!cccd.matches("^\\d{12}$")) {
            showInputError("Căn cước công dân phải gồm đúng 12 số.");
            cccdField.requestFocusInWindow();
            return;
        }

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showInputError("Email không đúng định dạng.");
            emailField.requestFocusInWindow();
            return;
        }

        if (diaChi.isEmpty()) {
            showInputError("Vui lòng nhập địa chỉ.");
            addressField.requestFocusInWindow();
            return;
        }

        if (birthDateField.getDate() == null) {
            showInputError("Vui lòng chọn ngày sinh.");
            return;
        }

        if (startDateField.getDate() == null) {
            showInputError("Vui lòng chọn ngày vào làm.");
            return;
        }

        StaffUpdateRequest request = new StaffUpdateRequest();

        request.setMaNv(detail.getMaNv());
        request.setUserId(detail.getUserId());
        request.setHoTen(hoTen);
        request.setSdt(sdt);
        request.setCccd(cccd);
        request.setEmail(email);
        request.setDiaChi(diaChi);
        request.setNgaySinh(birthDateField.getDate());
        request.setNgayVaoLam(startDateField.getDate());
        request.setMaCn(currentBranchId);
        request.setQuanLy(managerRadio.isSelected());
        request.setMaLoaiNv(managerRadio.isSelected() ? managerTypeId : staffTypeId);

        result = request;
        dispose();
    }

    private String text(JTextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void showInputError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Dữ liệu không hợp lệ",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private class DatePickerField extends JPanel {
        private static final String DISPLAY_PATTERN = "dd/MM/yyyy";

        private final JTextField displayField = new JTextField();
        private final JButton chooseButton = new RoundedButton("Chọn", Color.WHITE, BORDER, 10);
        private final JButton clearButton = new RoundedButton("Xóa", Color.WHITE, BORDER, 10);
        private LocalDate date;

        DatePickerField(LocalDate defaultDate) {
            this.date = defaultDate;

            setLayout(new BorderLayout(10, 0));
            setOpaque(false);

            displayField.setEditable(false);
            displayField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            displayField.setForeground(TEXT);
            displayField.setPreferredSize(new Dimension(10, 38));
            displayField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    new EmptyBorder(0, 14, 0, 14)
            ));

            chooseButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            chooseButton.setPreferredSize(new Dimension(82, 38));
            chooseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            chooseButton.setFocusPainted(false);
            chooseButton.setBorderPainted(false);
            chooseButton.setContentAreaFilled(false);
            chooseButton.setOpaque(false);

            clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            clearButton.setPreferredSize(new Dimension(70, 38));
            clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            clearButton.setFocusPainted(false);
            clearButton.setBorderPainted(false);
            clearButton.setContentAreaFilled(false);
            clearButton.setOpaque(false);

            JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
            actions.setOpaque(false);
            actions.add(chooseButton);
            actions.add(clearButton);

            add(displayField, BorderLayout.CENTER);
            add(actions, BorderLayout.EAST);

            chooseButton.addActionListener(e -> openPicker());
            clearButton.addActionListener(e -> setDate(null));

            refreshDisplay();
        }

        LocalDate getDate() {
            return date;
        }

        void setDate(LocalDate date) {
            this.date = date;
            refreshDisplay();
        }

        private void refreshDisplay() {
            if (date == null) {
                displayField.setText("");
                return;
            }

            displayField.setText(date.format(DateTimeFormatter.ofPattern(DISPLAY_PATTERN)));
        }

        private void openPicker() {
            LocalDate baseDate = date == null ? LocalDate.now() : date;
            CalendarPicker pickerPanel = new CalendarPicker(baseDate);

            JDialog picker = new JDialog(EditStaffDialog.this, "Chọn ngày", true);
            picker.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            picker.setResizable(false);
            picker.setLayout(new BorderLayout());
            picker.add(pickerPanel, BorderLayout.CENTER);
            picker.pack();
            picker.setLocationRelativeTo(EditStaffDialog.this);

            pickerPanel.setOnSelected(selectedDate -> {
                setDate(selectedDate);
                picker.dispose();
            });

            picker.setVisible(true);
        }
    }

    private class CalendarPicker extends JPanel {
        private final JComboBox<Integer> yearBox = new JComboBox<>();
        private final JComboBox<Integer> monthBox = new JComboBox<>();
        private final JPanel dayGrid = new JPanel(new GridLayout(0, 7, 10, 10));
        private java.util.function.Consumer<LocalDate> onSelected;
        private boolean adjusting;

        CalendarPicker(LocalDate baseDate) {
            setLayout(new BorderLayout(0, 16));
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(16, 18, 18, 18));

            int currentYear = LocalDate.now().getYear();

            for (int year = currentYear - 80; year <= currentYear + 2; year++) {
                yearBox.addItem(year);
            }

            for (int month = 1; month <= 12; month++) {
                monthBox.addItem(month);
            }

            yearBox.setSelectedItem(baseDate.getYear());
            monthBox.setSelectedItem(baseDate.getMonthValue());

            JPanel top = new JPanel(new GridLayout(1, 2, 12, 0));
            top.setOpaque(false);
            top.add(yearBox);
            top.add(monthBox);

            yearBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            monthBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            yearBox.addActionListener(e -> refreshDays(null));
            monthBox.addActionListener(e -> refreshDays(null));

            add(top, BorderLayout.NORTH);
            add(dayGrid, BorderLayout.CENTER);

            refreshDays(baseDate.getDayOfMonth());
        }

        void setOnSelected(java.util.function.Consumer<LocalDate> onSelected) {
            this.onSelected = onSelected;
        }

        private void refreshDays(Integer selectedDay) {
            if (adjusting) {
                return;
            }

            Integer year = (Integer) yearBox.getSelectedItem();
            Integer month = (Integer) monthBox.getSelectedItem();

            if (year == null || month == null) {
                return;
            }

            adjusting = true;
            dayGrid.removeAll();
            dayGrid.setOpaque(false);

            String[] weekDays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            for (String weekDay : weekDays) {
                JLabel label = new JLabel(weekDay, SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setForeground(TEXT);
                dayGrid.add(label);
            }

            LocalDate firstDay = LocalDate.of(year, month, 1);
            int blankCells = firstDay.getDayOfWeek().getValue() - 1;
            int lengthOfMonth = YearMonth.of(year, month).lengthOfMonth();

            for (int i = 0; i < blankCells; i++) {
                dayGrid.add(new JLabel(""));
            }

            for (int day = 1; day <= lengthOfMonth; day++) {
                JButton button = new RoundedButton(String.valueOf(day), Color.WHITE, BORDER, 8);
                button.setPreferredSize(new Dimension(54, 42));
                button.setFont(new Font("Segoe UI", Font.BOLD, 13));
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setContentAreaFilled(false);
                button.setOpaque(false);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                if (selectedDay != null && selectedDay == day) {
                    button.setBackground(new Color(219, 234, 254));
                    button.setForeground(BLUE);
                } else {
                    button.setForeground(TEXT);
                }

                final int selected = day;
                button.addActionListener(e -> {
                    if (onSelected != null) {
                        onSelected.accept(LocalDate.of(year, month, selected));
                    }
                });

                dayGrid.add(button);
            }

            adjusting = false;
            revalidate();
            repaint();
        }
    }

    private static class RoundedBoxPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;
        private final Color borderColor;

        RoundedBoxPanel(int radius, Color backgroundColor, Color borderColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    private static class RoundedButton extends JButton {
        private final Color backgroundColor;
        private final Color borderColor;
        private final int radius;

        RoundedButton(String text, Color backgroundColor, Color borderColor, int radius) {
            super(text);
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.radius = radius;
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
