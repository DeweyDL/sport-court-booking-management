package com.sportcourt.modules.customer.view;

import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.function.Consumer;

final class CustomerEditDialog {
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATE_PARSE_FORMAT = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(29, 78, 216);
    private static final Color BRAND_BLUE_BG = new Color(239, 246, 255);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color READONLY_BG = new Color(241, 245, 249);

    private CustomerEditDialog() {
    }

    static UpdateCustomerRequest show(Component parent, CustomerProfile profile) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Cập nhật khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        dialog.setContentPane(root);

        JLabel title = new JLabel("Cập nhật khách hàng");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Chỉnh sửa thông tin cho khách hàng " + profile.maKhachHang() + ".");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        JTextField txtMaKh = createReadOnlyField(profile.maKhachHang());
        JTextField txtHoTen = createEditableField(profile.hoTen());
        JTextField txtSdt = createEditableField(profile.sdt());
        JTextField txtEmail = createEditableField(profile.emailHeThong());
        JTextField txtDiaChi = createEditableField(profile.diaChi());

        LocalDate[] ngaySinhValue = new LocalDate[]{profile.ngaySinh()};
        JFormattedTextField txtNgaySinh = createDateField(profile.ngaySinh());
        JButton btnPickDate = new JButton(loadIcon("/icon/calendar.png", 16, 16));
        btnPickDate.setToolTipText("Chọn ngày từ lịch");
        styleDateButton(btnPickDate);
        btnPickDate.addActionListener(event ->
                CalendarPickerPopup.show(btnPickDate, ngaySinhValue[0], selected -> {
                    ngaySinhValue[0] = selected;
                    txtNgaySinh.setText(formatDate(selected));
                })
        );

        JPanel ngaySinhPanel = new JPanel(new BorderLayout(8, 0));
        ngaySinhPanel.setOpaque(false);
        ngaySinhPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        ngaySinhPanel.add(txtNgaySinh, BorderLayout.CENTER);
        ngaySinhPanel.add(btnPickDate, BorderLayout.EAST);

        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        cbTrangThai.setSelectedItem(profile.trangThai());
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbTrangThai.setEnabled(false);
        cbTrangThai.setFocusable(false);
        cbTrangThai.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 18),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        cbTrangThai.setBackground(READONLY_BG);
        cbTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        form.add(createField("Mã khách hàng", txtMaKh));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Họ tên", txtHoTen));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Số điện thoại", txtSdt));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Email hệ thống", txtEmail));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Địa chỉ", txtDiaChi));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Ngày sinh", ngaySinhPanel));
        form.add(Box.createVerticalStrut(14));
        form.add(createField("Trạng thái", cbTrangThai));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getViewport().setBackground(DIALOG_BG);
        root.add(formScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy", new Color(229, 231, 235), new Color(31, 41, 55));
        JButton saveBtn = createPillButton("Lưu thay đổi", BRAND_BLUE_BG, BRAND_BLUE);
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        final UpdateCustomerRequest[] result = new UpdateCustomerRequest[1];

        cancelBtn.addActionListener(event -> dialog.dispose());
        saveBtn.addActionListener(event -> {
            String hoTen = txtHoTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim();
            String trangThai = profile.trangThai();

            if (hoTen.isEmpty() || sdt.isEmpty()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Họ tên và số điện thoại là bắt buộc.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            LocalDate ngaySinh;
            try {
                ngaySinh = parseOptionalDate(txtNgaySinh.getText());
                txtNgaySinh.setText(formatDate(ngaySinh));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Ngày sinh phải đúng định dạng dd/MM/yyyy.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            result[0] = new UpdateCustomerRequest(
                    hoTen,
                    sdt,
                    trangThai,
                    normalizeOptional(email),
                    normalizeOptional(sdt),
                    normalizeOptional(diaChi),
                    ngaySinh
            );
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 560), 520);
        dialog.setMinimumSize(new Dimension(560, 520));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static JTextField createReadOnlyField(String value) {
        JTextField field = createBaseField(value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setRequestFocusEnabled(false);
        field.setCursor(Cursor.getDefaultCursor());
        field.setFont(new Font("Segoe UI", Font.BOLD, 15));
        field.setBackground(READONLY_BG);
        return field;
    }

    private static JTextField createEditableField(String value) {
        JTextField field = createBaseField(value);
        field.setBackground(new Color(249, 250, 251));
        return field;
    }

    private static JTextField createBaseField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 18),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }

    private static JFormattedTextField createDateField(LocalDate value) {
        try {
            MaskFormatter formatter = new MaskFormatter("##/##/####");
            formatter.setPlaceholderCharacter('_');
            JFormattedTextField field = new JFormattedTextField(formatter);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            field.setForeground(new Color(31, 41, 55));
            field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(BORDER_COLOR, 18),
                    BorderFactory.createEmptyBorder(9, 14, 9, 14)
            ));
            field.setBackground(new Color(249, 250, 251));
            field.setText(value == null ? "" : formatDate(value));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            return field;
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    private static JPanel createField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private static void styleDateButton(JButton button) {
        Dimension size = new Dimension(42, 40);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 18),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        button.setBackground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMAT.format(date);
    }

    private static LocalDate parseOptionalDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim().replace("_", "").replace(" ", "");
        if (normalized.isEmpty() || normalized.equals("//")) {
            return null;
        }
        return LocalDate.parse(normalized, DATE_PARSE_FORMAT);
    }

    private static Icon loadIcon(String path, int width, int height) {
        URL resource = CustomerEditDialog.class.getResource(path);
        if (resource == null) {
            return new ImageIcon();
        }
        Image image = new ImageIcon(resource).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private static JButton createPillButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private static final class CalendarPickerPopup {
        private CalendarPickerPopup() {
        }

        static void show(JComponent anchor, LocalDate currentValue, Consumer<LocalDate> onSelected) {
            LocalDate selected = currentValue == null ? LocalDate.now() : currentValue;
            YearMonth[] viewingMonth = new YearMonth[]{YearMonth.from(selected)};
            Window owner = SwingUtilities.getWindowAncestor(anchor);
            JDialog popup = new JDialog(owner);
            popup.setUndecorated(true);
            popup.setModal(false);
            popup.setAlwaysOnTop(true);
            popup.setFocusableWindowState(true);
            popup.addWindowFocusListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowLostFocus(java.awt.event.WindowEvent e) {
                    popup.dispose();
                }
            });

            JPanel root = new JPanel(new BorderLayout(0, 10));
            root.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR),
                    new EmptyBorder(10, 10, 10, 10)
            ));
            root.setBackground(Color.WHITE);

            JPanel topBar = new JPanel(new BorderLayout(8, 0));
            topBar.setOpaque(false);
            JButton prev = new JButton("<");
            JButton next = new JButton(">");
            styleDateButton(prev);
            styleDateButton(next);

            JComboBox<Integer> monthCombo = new JComboBox<>();
            for (int m = 1; m <= 12; m++) {
                monthCombo.addItem(m);
            }
            monthCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            monthCombo.setFocusable(false);

            Integer currentYear = YearMonth.now().getYear();
            JComboBox<Integer> yearCombo = new JComboBox<>();
            for (int y = currentYear - 120; y <= currentYear + 10; y++) {
                yearCombo.addItem(y);
            }
            yearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            yearCombo.setFocusable(false);

            JPanel monthYearPanel = new JPanel(new GridLayout(1, 2, 6, 0));
            monthYearPanel.setOpaque(false);
            monthYearPanel.add(monthCombo);
            monthYearPanel.add(yearCombo);
            topBar.add(prev, BorderLayout.WEST);
            topBar.add(monthYearPanel, BorderLayout.CENTER);
            topBar.add(next, BorderLayout.EAST);

            JPanel daysPanel = new JPanel(new GridLayout(0, 7, 4, 4));
            daysPanel.setOpaque(false);

            final boolean[] syncing = new boolean[]{false};
            Runnable renderCalendar = () -> {
                daysPanel.removeAll();
                syncing[0] = true;
                monthCombo.setSelectedItem(viewingMonth[0].getMonthValue());
                yearCombo.setSelectedItem(viewingMonth[0].getYear());
                syncing[0] = false;
                String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
                for (String name : dayNames) {
                    JLabel dayName = new JLabel(name, SwingConstants.CENTER);
                    dayName.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    dayName.setForeground(TEXT_MUTED);
                    daysPanel.add(dayName);
                }

                YearMonth ym = viewingMonth[0];
                LocalDate firstDay = ym.atDay(1);
                int startOffset = firstDay.getDayOfWeek().getValue() - 1;
                for (int i = 0; i < startOffset; i++) {
                    daysPanel.add(new JLabel(""));
                }

                for (int day = 1; day <= ym.lengthOfMonth(); day++) {
                    LocalDate date = ym.atDay(day);
                    JButton dayButton = new JButton(String.valueOf(day));
                    dayButton.setFocusPainted(false);
                    dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    dayButton.setBackground(Color.WHITE);
                    dayButton.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
                    if (currentValue != null && currentValue.equals(date)) {
                        dayButton.setBackground(BRAND_BLUE_BG);
                        dayButton.setBorder(BorderFactory.createLineBorder(BRAND_BLUE));
                    }
                    dayButton.addActionListener(event -> {
                        onSelected.accept(date);
                        popup.dispose();
                    });
                    daysPanel.add(dayButton);
                }

                int totalCells = 7 + startOffset + ym.lengthOfMonth();
                int trailing = (7 - (totalCells % 7)) % 7;
                for (int i = 0; i < trailing; i++) {
                    daysPanel.add(new JLabel(""));
                }
                daysPanel.revalidate();
                daysPanel.repaint();
            };

            prev.addActionListener(event -> {
                viewingMonth[0] = viewingMonth[0].minusMonths(1);
                renderCalendar.run();
            });
            next.addActionListener(event -> {
                viewingMonth[0] = viewingMonth[0].plusMonths(1);
                renderCalendar.run();
            });
            monthCombo.addActionListener(event -> {
                if (syncing[0]) {
                    return;
                }
                Integer month = (Integer) monthCombo.getSelectedItem();
                Integer year = (Integer) yearCombo.getSelectedItem();
                if (month != null && year != null) {
                    viewingMonth[0] = YearMonth.of(year, month);
                    renderCalendar.run();
                }
            });
            yearCombo.addActionListener(event -> {
                if (syncing[0]) {
                    return;
                }
                Integer month = (Integer) monthCombo.getSelectedItem();
                Integer year = (Integer) yearCombo.getSelectedItem();
                if (month != null && year != null) {
                    viewingMonth[0] = YearMonth.of(year, month);
                    renderCalendar.run();
                }
            });

            root.add(topBar, BorderLayout.NORTH);
            root.add(daysPanel, BorderLayout.CENTER);
            popup.setContentPane(root);
            renderCalendar.run();
            popup.pack();
            Point anchorLocation = anchor.getLocationOnScreen();
            int x = anchorLocation.x;
            int y = anchorLocation.y - popup.getHeight() - 4;
            if (y < 0) {
                y = anchorLocation.y + anchor.getHeight() + 4;
            }
            popup.setLocation(x, y);
            popup.setVisible(true);
            SwingUtilities.invokeLater(popup::requestFocus);
        }
    }

    private static final class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int arc;

        private RoundedLineBorder(Color color, int arc) {
            this.color = color;
            this.arc = arc;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }
}
