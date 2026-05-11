package com.sportcourt.modules.court.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.entity.Court;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

final class CourtEditPanel {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9_]{1,20}$");

    private CourtEditPanel() {
    }

    static Court show(Component parent, CourtTableRow currentCourt, List<String> areaIds) {
        if (currentCourt == null) {
            return null;
        }

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Cập nhật sân con", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Cập nhật sân con");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Mã khu vực theo mẫu A-Z, 0-9, dấu gạch dưới (_), tối đa 20 ký tự.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField txtCourtId = readonlyField(currentCourt.getCourtId());

        List<String> editableAreaIds = new java.util.ArrayList<>(areaIds);
        if (currentCourt.getAreaId() != null
                && !currentCourt.getAreaId().isBlank()
                && !editableAreaIds.contains(currentCourt.getAreaId())) {
            editableAreaIds.add(0, currentCourt.getAreaId());
        }
        JComboBox<String> cbAreaId = new JComboBox<>(editableAreaIds.toArray(new String[0]));
        styleComboBox(cbAreaId);
        cbAreaId.setEnabled(!areaIds.isEmpty());
        cbAreaId.setSelectedItem(currentCourt.getAreaId());

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"ĐANG HOẠT ĐỘNG", "BẢO TRÌ"});
        styleComboBox(cbStatus);
        cbStatus.setSelectedItem(resolveStatus(currentCourt.getStatus()));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Mã sân con", txtCourtId);
        addField(form, g, 1, "Mã khu vực", cbAreaId);
        addField(form, g, 2, "Trạng thái", cbStatus);
        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getViewport().setBackground(DIALOG_BG);
        root.add(formScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnSave = button("Lưu thay đổi", BRAND_BLUE, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        final Court[] result = new Court[1];
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String areaId = cbAreaId.getSelectedItem() == null
                    ? ""
                    : normalizeCode(cbAreaId.getSelectedItem().toString());
            String status = (String) cbStatus.getSelectedItem();
            if (areaId.isEmpty() || status == null || status.isBlank()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Mã khu vực và trạng thái không được để trống.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            if (!isValidCode(areaId)) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Mã khu vực không hợp lệ.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Court court = new Court();
            court.setCourtId(currentCourt.getCourtId());
            court.setAreaId(areaId);
            court.setStatus(status);
            result[0] = court;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 560), 430);
        dialog.setMinimumSize(new Dimension(560, 430));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    private static String resolveStatus(String status) {
        if ("BẢO TRÌ".equalsIgnoreCase(status)) {
            return "BẢO TRÌ";
        }
        return "ĐANG HOẠT ĐỘNG";
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
        JTextField field = new JTextField(value);
        field.setFont(AppFonts.lexendRegular(14f));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setEditable(false);
        field.setFocusable(false);
        field.setRequestFocusEnabled(false);
        field.setCursor(Cursor.getDefaultCursor());
        field.setBackground(new Color(241, 245, 249));
        return field;
    }

    private static void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(AppFonts.lexendRegular(14f));
        comboBox.setFocusable(false);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        comboBox.setBackground(Color.WHITE);
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
    }

    private static String normalizeCode(String code) {
        if (code == null) {
            return "";
        }
        return code.trim().toUpperCase();
    }

    private static boolean isValidCode(String code) {
        return code != null && CODE_PATTERN.matcher(code).matches();
    }
}