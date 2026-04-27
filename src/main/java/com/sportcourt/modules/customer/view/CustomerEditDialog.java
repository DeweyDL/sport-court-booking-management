package com.sportcourt.modules.customer.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.UpdateCustomerRequest;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.YearMonth;
import java.text.ParseException;
import java.util.function.Consumer;

final class CustomerEditDialog {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);
    private static final DateTimeFormatter DATE_PARSE_FORMAT = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private CustomerEditDialog() {
    }

    static UpdateCustomerRequest show(Component parent, CustomerProfile profile) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Cập nhật khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Cập nhật khách hàng");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Chỉnh sửa các thông tin cơ bản của khách hàng.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField txtMaKh = readonlyField(profile.maKhachHang());
        JTextField txtHoTen = editableField(profile.hoTen());
        JTextField txtSdt = editableField(profile.sdt());
        JTextField txtEmail = editableField(profile.emailHeThong());
        JTextField txtDiaChi = editableField(profile.diaChi());
        LocalDate[] ngaySinhValue = new LocalDate[]{profile.ngaySinh()};
        JFormattedTextField txtNgaySinh = dateField(profile.ngaySinh());
        JButton btnPickDate = new JButton(scaleIcon("/icon/calendar.png", 16, 16));
        btnPickDate.setToolTipText("Chọn ngày từ lịch");
        styleDateButton(btnPickDate);
        btnPickDate.addActionListener(e -> {
            CalendarPickerPopup.show(btnPickDate, ngaySinhValue[0], selected -> {
                ngaySinhValue[0] = selected;
                txtNgaySinh.setText(formatDate(selected));
            });
        });
        JPanel ngaySinhPanel = new JPanel(new BorderLayout(8, 0));
        ngaySinhPanel.setOpaque(false);
        ngaySinhPanel.add(txtNgaySinh, BorderLayout.CENTER);
        ngaySinhPanel.add(btnPickDate, BorderLayout.EAST);
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        cbTrangThai.setSelectedItem(profile.trangThai());
        cbTrangThai.setFont(AppFonts.lexendRegular(14f));
        cbTrangThai.setEnabled(false);
        cbTrangThai.setFocusable(false);
        cbTrangThai.setBorder(new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS));
        cbTrangThai.setBackground(Color.WHITE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(card, g, 0, "Mã khách hàng", txtMaKh);
        addField(card, g, 1, "Họ tên", txtHoTen);
        addField(card, g, 2, "Số điện thoại", txtSdt);
        addField(card, g, 3, "Email hệ thống", txtEmail);
        addField(card, g, 4, "Địa chỉ", txtDiaChi);
        addField(card, g, 5, "Ngày sinh", ngaySinhPanel);
        addField(card, g, 6, "Trạng thái", cbTrangThai);
        JScrollPane formScroll = new JScrollPane(card);
        formScroll.setBorder(null);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getViewport().setBackground(DIALOG_BG);
        root.add(formScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnSave = button("Lưu thay đổi", BRAND_BLUE, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        final UpdateCustomerRequest[] result = new UpdateCustomerRequest[1];
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
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

    private static void addField(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(AppFonts.lexendBold(12f));
        lb.setForeground(TEXT_DARK);
        panel.add(lb, g);

        g.gridy = row * 2 + 1;
        panel.add(field, g);
    }

    private static JTextField readonlyField(String value) {
        JTextField field = baseField(value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setRequestFocusEnabled(false);
        field.setCursor(Cursor.getDefaultCursor());
        field.setBackground(new Color(241, 245, 249));
        return field;
    }

    private static JTextField editableField(String value) {
        return baseField(value);
    }

    private static JTextField baseField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(AppFonts.lexendRegular(14f));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(Color.WHITE);
        return field;
    }

    private static JFormattedTextField dateField(LocalDate value) {
        try {
            MaskFormatter formatter = new MaskFormatter("##/##/####");
            formatter.setPlaceholderCharacter('_');
            JFormattedTextField field = new JFormattedTextField(formatter);
            field.setFont(AppFonts.lexendRegular(14f));
            field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)
            ));
            field.setBackground(Color.WHITE);
            field.setText(value == null ? "" : formatDate(value));
            return field;
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
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

    private static void styleDateButton(JButton button) {
        button.setFont(AppFonts.lexendRegular(12f));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        button.setBackground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static Icon scaleIcon(String path, int width, int height) {
        URL resource = CustomerEditDialog.class.getResource(path);
        if (resource == null) {
            return new ImageIcon();
        }
        Image image = new ImageIcon(resource).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private static JButton button(String text, Color background, Color foreground) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(AppFonts.lexendBold(13f));
        btn.setForeground(foreground);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                    BorderFactory.createLineBorder(new Color(203, 213, 225)),
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
            monthCombo.setFont(AppFonts.lexendRegular(12f));
            monthCombo.setFocusable(false);
            Integer currentYear = YearMonth.now().getYear();
            JComboBox<Integer> yearCombo = new JComboBox<>();
            for (int y = currentYear - 120; y <= currentYear + 10; y++) {
                yearCombo.addItem(y);
            }
            yearCombo.setFont(AppFonts.lexendRegular(12f));
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
                    dayName.setFont(AppFonts.lexendBold(11f));
                    dayName.setForeground(new Color(100, 116, 139));
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
                    dayButton.setFont(AppFonts.lexendRegular(12f));
                    dayButton.setBackground(Color.WHITE);
                    dayButton.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
                    if (currentValue != null && currentValue.equals(date)) {
                        dayButton.setBackground(new Color(219, 234, 254));
                        dayButton.setBorder(BorderFactory.createLineBorder(new Color(37, 99, 235)));
                    }
                    dayButton.addActionListener(e -> {
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

            prev.addActionListener(e -> {
                viewingMonth[0] = viewingMonth[0].minusMonths(1);
                renderCalendar.run();
            });
            next.addActionListener(e -> {
                viewingMonth[0] = viewingMonth[0].plusMonths(1);
                renderCalendar.run();
            });
            monthCombo.addActionListener(e -> {
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
            yearCombo.addActionListener(e -> {
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

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(1, 1, 1, 1);
            return insets;
        }
    }
}

