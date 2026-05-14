package com.sportcourt.modules.revenue.view;

import com.sportcourt.modules.branch.entity.Branch;
import com.sportcourt.modules.revenue.dto.RevenueCreateRequest;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

final class RevenueCreateDialog {

    private static final Color DIALOG_BG   = new Color(248, 249, 252);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color TEXT_DARK   = new Color(30, 41, 59);
    private static final Color TEXT_MUTED  = new Color(100, 116, 139);
    private static final Color BORDER_CLR  = new Color(203, 213, 225);
    private static final Color READONLY_BG = new Color(241, 245, 249);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private RevenueCreateDialog() {}

    /**
     * @param parent     parent component
     * @param nextId     mã báo cáo được sinh tự động
     * @param branches   danh sách chi nhánh (index 0 có thể là null – bỏ qua)
     * @return request nếu người dùng nhấn Lưu, null nếu Hủy
     */
    static RevenueCreateRequest show(Component parent, String nextId, List<Branch> branches) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm báo cáo doanh thu",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        // ── Header ───────────────────────────────────────────────────────────
        JLabel title = new JLabel("Thêm báo cáo doanh thu mới");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Nhập thông tin báo cáo doanh thu thủ công.");
        subtitle.setFont(new Font("Lexend", Font.PLAIN, 13));
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

        // ── Form ─────────────────────────────────────────────────────────────
        JTextField txtMaDt   = createReadOnlyField(nextId);
        JTextField txtNgay   = new JTextField(LocalDate.now().format(DATE_FMT));
        JTextField txtNoiDung = new JTextField();
        JTextField txtTongDt = new JTextField();

        // Chi nhánh dropdown
        JComboBox<String> cbBranch = new JComboBox<>();
        cbBranch.setFont(new Font("Lexend", Font.PLAIN, 14));
        cbBranch.setBackground(CARD_BG);
        for (Branch b : branches) {
            if (b != null) cbBranch.addItem(b.tenChiNhanh());
        }
        // Filter ra branches thực (bỏ null)
        List<Branch> realBranches = branches.stream().filter(b -> b != null).toList();

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(createField("Mã báo cáo", txtMaDt));
        form.add(Box.createVerticalStrut(12));
        form.add(createComboField("Chi nhánh", cbBranch));
        form.add(Box.createVerticalStrut(12));
        form.add(createField("Ngày (dd/MM/yyyy)", txtNgay));
        form.add(Box.createVerticalStrut(12));
        form.add(createField("Nội dung", txtNoiDung));
        form.add(Box.createVerticalStrut(12));
        form.add(createField("Tổng doanh thu (VNĐ)", txtTongDt));

        root.add(form, BorderLayout.CENTER);

        // ── Actions ──────────────────────────────────────────────────────────
        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton cancelBtn = createPillButton("Hủy",
                new Color(226, 232, 240), new Color(30, 41, 59));
        JButton saveBtn   = createPillButton("Lưu báo cáo", BRAND_GREEN, Color.WHITE);
        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        final RevenueCreateRequest[] result = new RevenueCreateRequest[1];

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            // Validate
            if (realBranches.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Không có chi nhánh nào trong hệ thống.",
                        "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String noiDung = txtNoiDung.getText().trim();
            String rawDt   = txtTongDt.getText().trim();
            String rawNgay = txtNgay.getText().trim();

            if (noiDung.isEmpty() || rawDt.isEmpty() || rawNgay.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Vui lòng điền đầy đủ thông tin.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate ngay;
            try {
                ngay = LocalDate.parse(rawNgay, DATE_FMT);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Ngày không hợp lệ. Định dạng: dd/MM/yyyy",
                        "Lỗi ngày", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal tongDt;
            try {
                tongDt = new BigDecimal(rawDt.replace(",", "").replace(".", ""));
                if (tongDt.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Tổng doanh thu phải là số không âm.",
                        "Lỗi giá trị", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int idx     = cbBranch.getSelectedIndex();
            String maCn = realBranches.get(idx).maCn();

            result[0] = new RevenueCreateRequest(
                    txtMaDt.getText().trim(), maCn, noiDung, ngay, tongDt);
            dialog.dispose();
        });

        dialog.pack();
        applySize(dialog, 0.38, 0.65, 500, 520);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static void applySize(JDialog d, double wr, double hr, int minW, int minH) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.max(minW, (int) (screen.width  * wr));
        int h = Math.max(minH, (int) (screen.height * hr));
        d.setSize(Math.min(w, screen.width), Math.min(h, screen.height));
        d.setMinimumSize(new Dimension(minW, minH));
    }

    private static JTextField createReadOnlyField(String value) {
        JTextField field = new JTextField(value == null ? "" : value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setOpaque(false);
        field.setFont(new Font("Lexend", Font.BOLD, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBackground(READONLY_BG);
        return field;
    }

    private static JPanel createField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean editable = field.isEditable();
        field.setFont(new Font("Lexend", editable ? Font.PLAIN : Font.BOLD, 14));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_CLR, 25),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        field.setBackground(editable ? Color.WHITE : READONLY_BG);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private static JPanel createComboField(String labelText, JComboBox<String> combo) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Lexend", Font.BOLD, 12));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        combo.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_CLR, 25),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(combo);
        return panel;
    }

    private static JButton createPillButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Lexend", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private static final class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int arc;
        RoundedLineBorder(Color color, int arc) { this.color = color; this.arc = arc; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, arc, arc);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
    }
}
