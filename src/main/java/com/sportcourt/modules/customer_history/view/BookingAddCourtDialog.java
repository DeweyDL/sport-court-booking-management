package com.sportcourt.modules.customer_history.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer_history.dto.BookingAddCourtRequest;
import com.sportcourt.modules.customer_history.dto.PriceBoardOptionDTO;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class BookingAddCourtDialog {
    private static final int INPUT_CORNER_RADIUS = 25;
    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BUTTON_MUTED = new Color(226, 232, 240);
    private static final Color READONLY_BG = new Color(241, 245, 249);
    private static final Color INPUT_BORDER = new Color(203, 213, 225);

    private BookingAddCourtDialog() {
    }

    public static BookingAddCourtRequest show(Component parent, String invoiceId, List<String> courtIds, List<PriceBoardOptionDTO> priceBoards) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Thêm sân con", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.setContentPane(root);

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Thêm sân vào hóa đơn");
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Chọn sân, bảng giá và ngày để thêm vào hóa đơn " + invoiceId);
        subtitle.setFont(AppFonts.lexendRegular(13f));
        subtitle.setForeground(TEXT_MUTED);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JTextField txtInvoiceId = readonlyField(invoiceId);

        JComboBox<String> cbCourtId = new JComboBox<>(courtIds.toArray(new String[0]));
        styleComboBox(cbCourtId);
        cbCourtId.setEnabled(!courtIds.isEmpty());

        JComboBox<PriceBoardOptionDTO> cbPriceBoard = new JComboBox<>(priceBoards.toArray(new PriceBoardOptionDTO[0]));
        styleComboBox(cbPriceBoard);
        cbPriceBoard.setEnabled(!priceBoards.isEmpty());

        JTextField txtDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtDate.setFont(AppFonts.lexendRegular(14f));
        txtDate.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(INPUT_BORDER, INPUT_CORNER_RADIUS),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);

        addField(form, g, 0, "Mã hóa đơn", txtInvoiceId);
        addField(form, g, 1, "Chọn sân", cbCourtId);
        addField(form, g, 2, "Chọn khung giờ (Bảng giá)", cbPriceBoard);
        addField(form, g, 3, "Ngày thuê (YYYY-MM-DD)", txtDate);
        root.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        JButton btnCancel = button("Hủy", BUTTON_MUTED, TEXT_DARK);
        JButton btnConfirm = button("Thêm sân", BRAND_GREEN, Color.WHITE);
        actions.add(btnCancel);
        actions.add(btnConfirm);
        root.add(actions, BorderLayout.SOUTH);

        final BookingAddCourtRequest[] result = new BookingAddCourtRequest[1];
        btnCancel.addActionListener(e -> dialog.dispose());
        btnConfirm.addActionListener(e -> {
            String courtId = (String) cbCourtId.getSelectedItem();
            PriceBoardOptionDTO pb = (PriceBoardOptionDTO) cbPriceBoard.getSelectedItem();
            String dateStr = txtDate.getText().trim();

            if (courtId == null || pb == null || dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BookingAddCourtRequest req = new BookingAddCourtRequest();
            req.setInvoiceId(invoiceId);
            req.setCourtId(courtId);
            req.setPriceBoardId(pb.getPriceBoardId());
            req.setBookingDateStr(dateStr);
            result[0] = req;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setSize(Math.max(dialog.getWidth(), 500), dialog.getHeight());
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
        JTextField field = new JTextField(value == null ? "" : value) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, INPUT_CORNER_RADIUS, INPUT_CORNER_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(AppFonts.lexendBold(14f));
        field.setEditable(false);
        field.setFocusable(false);
        field.setOpaque(false);
        field.setBackground(READONLY_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(INPUT_BORDER, INPUT_CORNER_RADIUS),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return field;
    }

    private static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(AppFonts.lexendRegular(14f));
        comboBox.setFocusable(false);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(INPUT_BORDER, INPUT_CORNER_RADIUS),
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
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, arc, arc);
            g2.dispose();
        }
    }
}