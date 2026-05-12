package com.sportcourt.modules.cost.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.cost.view.CostMockData.AreaOption;
import com.sportcourt.modules.cost.view.CostMockData.CostItem;
import com.sportcourt.modules.cost.view.CostMockData.KhungGioOption;
import com.sportcourt.modules.cost.view.CostMockData.Store;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

final class CostChange {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_BLUE = new Color(37, 99, 235);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);

    private CostChange() {
    }

    static boolean show(Component parent, Store store, String maBg) {
        CostItem detail = store.getDetail(maBg);
        if (detail == null) {
            JOptionPane.showMessageDialog(parent, "Không tìm thấy bảng giá.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Cập nhật bảng giá", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Cập nhật bảng giá");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Chỉnh sửa thông tin khung giờ và giá.");
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField maBgField = readonly(detail.maBg());

        List<AreaOption> areaOptions = store.getAreaOptions();
        JComboBox<AreaOption> areaCombo = new JComboBox<>(areaOptions.toArray(new AreaOption[0]));
        for (int i = 0; i < areaCombo.getItemCount(); i++) {
            AreaOption opt = areaCombo.getItemAt(i);
            if (opt != null && opt.maKv() != null && opt.maKv().equals(detail.maKv())) {
                areaCombo.setSelectedIndex(i);
                break;
            }
        }

        List<KhungGioOption> khungGioList = store.getKhungGioOptions();
        if (khungGioList == null || khungGioList.isEmpty()) {
            khungGioList = new ArrayList<>();
            for (int h = 0; h <= 23; h++) {
                khungGioList.add(new KhungGioOption(null, h, h + 1));
            }
        }
        JComboBox<KhungGioOption> khungGioCombo = new JComboBox<>(khungGioList.toArray(new KhungGioOption[0]));
        for (int i = 0; i < khungGioCombo.getItemCount(); i++) {
            KhungGioOption opt = khungGioCombo.getItemAt(i);
            if (opt != null && opt.gioBatDau() == detail.gioBatDau() && opt.gioKetThuc() == detail.gioKetThuc()) {
                khungGioCombo.setSelectedIndex(i);
                break;
            }
        }

        JTextField startHourField = readonly("");
        JTextField endHourField = readonly("");
        JTextField giaField = new JTextField(detail.gia() == null ? "" : detail.gia().toPlainString());

        Runnable updateHours = () -> {
            KhungGioOption sel = (KhungGioOption) khungGioCombo.getSelectedItem();
            startHourField.setText(sel == null ? "" : "%02d:00".formatted(sel.gioBatDau()));
            endHourField.setText(sel == null ? "" : "%02d:00".formatted(sel.gioKetThuc()));
        };
        updateHours.run();
        khungGioCombo.addActionListener(e -> updateHours.run());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Mã bảng giá", maBgField);
        addField(form, g, 1, "Khu vực", areaCombo);
        addField(form, g, 2, "Khung giờ", khungGioCombo);
        addField(form, g, 3, "Giờ bắt đầu", startHourField);
        addField(form, g, 4, "Giờ kết thúc", endHourField);
        addField(form, g, 5, "Giá (VNĐ)", giaField);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnSave = button("Lưu thay đổi", BRAND_BLUE, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        final boolean[] saved = {false};
        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            AreaOption selectedArea = (AreaOption) areaCombo.getSelectedItem();
            if (selectedArea == null) {
                JOptionPane.showMessageDialog(dialog, "Hãy chọn khu vực.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            KhungGioOption selectedKhungGio = (KhungGioOption) khungGioCombo.getSelectedItem();
            BigDecimal gia;
            try {
                gia = new BigDecimal(giaField.getText().trim().replace(",", ""));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Giá không hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int gioBatDau = selectedKhungGio == null ? 0 : selectedKhungGio.gioBatDau();
            int gioKetThuc = selectedKhungGio == null ? 1 : selectedKhungGio.gioKetThuc();
            try {
                store.update(new CostItem(maBg, selectedArea.maKv(),
                        selectedKhungGio == null ? null : selectedKhungGio.maKg(),
                        gioBatDau, gioKetThuc, gia, false, LocalDateTime.now()));
                saved[0] = true;
                dialog.dispose();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage(),
                        "Lỗi cập nhật bảng giá", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 560), dialog.getHeight());
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        return saved[0];
    }

    private static JTextField readonly(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setBackground(new Color(241, 245, 249));
        styleTextField(field);
        return field;
    }

    private static void addField(JPanel panel, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row * 2;
        JLabel lb = new JLabel(label);
        lb.setFont(AppFonts.lexendBold(12f));
        lb.setForeground(TEXT_DARK);
        panel.add(lb, g);

        g.gridy = row * 2 + 1;
        if (field instanceof JTextField textField) {
            styleTextField(textField);
        } else {
            field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            field.setBackground(Color.WHITE);
            field.setFont(AppFonts.lexendRegular(14f));
        }
        panel.add(field, g);
    }

    private static void styleTextField(JTextField textField) {
        textField.setFont(AppFonts.lexendRegular(14f));
        textField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(new Color(203, 213, 225), INPUT_CORNER_RADIUS),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
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
}
