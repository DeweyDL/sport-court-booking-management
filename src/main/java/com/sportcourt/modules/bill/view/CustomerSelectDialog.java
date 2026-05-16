package com.sportcourt.modules.bill.view;

import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.branch.controller.BranchController;
import com.sportcourt.modules.branch.entity.Branch;
import com.sportcourt.modules.customer.controller.ManageCustomerController;
import com.sportcourt.modules.customer.dto.CreateCustomerRequest;
import com.sportcourt.modules.customer.dto.CustomerProfile;
import com.sportcourt.modules.customer.dto.CustomerResult;
import com.sportcourt.modules.customer.dto.CustomerSummary;
import com.sportcourt.modules.customer.view.CustomerCreateDialog;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CustomerSelectDialog {

    public record SelectResult(CustomerSummary customer, Branch branch) {}

    private static final Color DIALOG_BG    = new Color(248, 249, 252);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color GREEN        = new Color(16, 110, 0);
    private static final Color GREEN_SOFT   = new Color(240, 253, 244);
    private static final Color GREEN_BORDER = new Color(74, 190, 110);
    private static final Color TEXT_DARK    = new Color(30, 41, 59);
    private static final Color TEXT_MUTED   = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color ROW_SELECTED = new Color(220, 252, 231);

    private CustomerSelectDialog() {}

    public static SelectResult show(Component parent, ManageCustomerController controller, BranchController branchController) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Chọn khách hàng", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(true);

        final SelectResult[]    result   = {null};
        final CustomerSummary[] selected = {null};

        UserSession session   = SessionManager.requireSession();
        boolean     isOwner   = session.isOwner();
        java.util.List<Branch> branches = branchController.getBranchList("");
        final Branch[] selectedBranch = {null};
        final List<CustomerSummary> rowData = new ArrayList<>();

        // ── TABLE MODEL ──────────────────────────────────────────────────────
        String[] cols = {"Mã khách hàng", "Họ tên", "Số điện thoại"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // ── ROOT ─────────────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        dialog.setContentPane(root);

        // ── HEADER ───────────────────────────────────────────────────────────
        JLabel title = new JLabel("Tìm kiếm khách hàng");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JLabel sub = new JLabel("Chọn khách hàng để lập hóa đơn đặt sân.");
        sub.setFont(new Font("Lexend", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        // ── CARD ─────────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        root.add(card, BorderLayout.CENTER);

        // ── BRANCH SELECTOR ──────────────────────────────────────────────────
        JPanel branchRow = new JPanel(new BorderLayout(12, 0));
        branchRow.setOpaque(false);
        branchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        branchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel branchLbl = new JLabel("Chi nhánh:");
        branchLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        branchLbl.setForeground(TEXT_DARK);
        branchLbl.setPreferredSize(new Dimension(90, 0));

        if (isOwner) {
            JComboBox<Branch> branchCombo = new JComboBox<>();
            for (Branch b : branches) branchCombo.addItem(b);
            branchCombo.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
                JLabel lbl = new JLabel(value == null ? "--" : value.tenChiNhanh());
                lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                lbl.setOpaque(isSelected);
                if (isSelected) lbl.setBackground(ROW_SELECTED);
                return lbl;
            });
            if (!branches.isEmpty()) selectedBranch[0] = branches.get(0);
            branchCombo.addActionListener(e -> selectedBranch[0] = (Branch) branchCombo.getSelectedItem());
            branchRow.add(branchLbl, BorderLayout.WEST);
            branchRow.add(branchCombo, BorderLayout.CENTER);
        } else {
            String sessionBranchId = session.getBranchId();
            selectedBranch[0] = branches.stream()
                    .filter(b -> b.maCn().equals(sessionBranchId))
                    .findFirst().orElse(null);
            String branchName = selectedBranch[0] != null ? selectedBranch[0].tenChiNhanh() : sessionBranchId;
            JLabel fixedLbl = new JLabel(branchName + "  🔒");
            fixedLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fixedLbl.setForeground(TEXT_MUTED);
            branchRow.add(branchLbl, BorderLayout.WEST);
            branchRow.add(fixedLbl, BorderLayout.CENTER);
        }

        // ── NÚT THÊM KHÁCH HÀNG (trên thanh tìm kiếm) ───────────────────────
        JButton addCustomerBtn = pillBtn("+ Thêm khách hàng mới", new Color(228, 250, 226), GREEN);
        addCustomerBtn.setFont(new Font("Lexend", Font.BOLD, 13));
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        topRow.add(addCustomerBtn);

        // ── SEARCH FIELD ─────────────────────────────────────────────────────
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc số điện thoại...");
        searchField.setBorder(new EmptyBorder(8, 14, 8, 14));
        searchField.setOpaque(false);

        JPanel searchWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new java.awt.BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        searchWrapper.setOpaque(false);
        searchWrapper.add(searchField, BorderLayout.CENTER);
        searchWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // ── TABLE ────────────────────────────────────────────────────────────
        JTable table = new JTable(model);
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_DARK);
        table.setFocusable(true);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(248, 250, 252));
        th.setForeground(TEXT_MUTED);
        th.setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
        th.setReorderingAllowed(false);

        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(110);
        tcm.getColumn(1).setPreferredWidth(200);
        tcm.getColumn(2).setPreferredWidth(130);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? CARD_BG : new Color(250, 251, 253));
                }
                return this;
            }
        };
        for (int i = 0; i < cols.length; i++) tcm.getColumn(i).setCellRenderer(cellRenderer);

        // Rounded table wrapper
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(CARD_BG);

        JPanel tableWrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
            @Override protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        tableWrapper.setOpaque(false);
        tableWrapper.add(tableScroll, BorderLayout.CENTER);
        tableWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        tableWrapper.setPreferredSize(new Dimension(0, 220));

        // ── INFO PANEL ───────────────────────────────────────────────────────
        JLabel lblName  = new JLabel("--");
        JLabel lblPhone = new JLabel("--");
        JPanel infoPanel = buildInfoPanel(lblName, lblPhone);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.setVisible(false);

        // Assemble card
        card.add(branchRow);
        card.add(Box.createVerticalStrut(12));
        card.add(topRow);
        card.add(Box.createVerticalStrut(10));
        card.add(searchWrapper);
        card.add(Box.createVerticalStrut(12));
        card.add(tableWrapper);
        card.add(Box.createVerticalStrut(12));
        card.add(infoPanel);

        // ── ACTION BUTTONS ───────────────────────────────────────────────────
        JPanel actions = new JPanel(new GridLayout(1, 2, 14, 0));
        actions.setOpaque(false);
        JButton cancelBtn  = pillBtn("Hủy", new Color(226, 232, 240), TEXT_DARK);
        JButton confirmBtn = pillBtn("Tạo hóa đơn", GREEN, Color.WHITE);
        confirmBtn.setEnabled(false);
        actions.add(cancelBtn);
        actions.add(confirmBtn);
        root.add(actions, BorderLayout.SOUTH);

        // ── SEARCH LISTENER ──────────────────────────────────────────────────
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { search(); }
            @Override public void removeUpdate(DocumentEvent e) { search(); }
            @Override public void changedUpdate(DocumentEvent e) { search(); }

            private void search() {
                String kw = searchField.getText().trim();
                model.setRowCount(0);
                rowData.clear();
                selected[0] = null;
                confirmBtn.setEnabled(false);
                infoPanel.setVisible(false);
                card.revalidate();

                if (kw.isEmpty()) return;
                CustomerResult<List<CustomerSummary>> r = controller.searchByName(kw);
                if (r.success() && r.data() != null) {
                    for (CustomerSummary c : r.data()) {
                        model.addRow(new Object[]{c.maKhachHang(), c.hoTen(), c.sdt()});
                        rowData.add(c);
                    }
                }
            }
        });

        // ── TABLE SELECTION ──────────────────────────────────────────────────
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row >= 0 && row < rowData.size()) {
                selected[0] = rowData.get(row);
                lblName.setText(nullDash(selected[0].hoTen()));
                lblPhone.setText(nullDash(selected[0].sdt()));
                infoPanel.setVisible(true);
                confirmBtn.setEnabled(true);
            } else {
                selected[0] = null;
                infoPanel.setVisible(false);
                confirmBtn.setEnabled(false);
            }
            card.revalidate();
            card.repaint();
        });

        // ── THÊM KHÁCH HÀNG ─────────────────────────────────────────────────
        addCustomerBtn.addActionListener(e -> {
            CustomerResult<String> genId = controller.generateNextMaKhachHang();
            if (!genId.success() || genId.data() == null || genId.data().isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Không thể sinh mã khách hàng.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CreateCustomerRequest req = CustomerCreateDialog.show(dialog, genId.data());
            if (req == null) return;

            CustomerResult<CustomerProfile> cr = controller.create(req);
            if (!cr.success() || cr.data() == null) {
                JOptionPane.showMessageDialog(dialog,
                        cr.message() == null ? "Tạo khách hàng thất bại." : cr.message(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            CustomerProfile p = cr.data();
            CustomerSummary ns = new CustomerSummary(
                    p.maKhachHang(), p.userId(), p.hoTen(), p.sdt(),
                    p.diaChi(), p.ngaySinh(), p.maHang(), p.trangThai(), p.doanhThu()
            );
            rowData.add(ns);
            model.addRow(new Object[]{ns.maKhachHang(), ns.hoTen(), ns.sdt()});
            int newRow = model.getRowCount() - 1;
            table.setRowSelectionInterval(newRow, newRow);
            table.scrollRectToVisible(table.getCellRect(newRow, 0, true));
            card.revalidate();
            card.repaint();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        confirmBtn.addActionListener(e -> {
            result[0] = new SelectResult(selected[0], selectedBranch[0]);
            dialog.dispose();
        });

        // ── SIZE & SHOW ──────────────────────────────────────────────────────
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = Math.max(600, (int) (screen.width * 0.42));
        int h = Math.max(620, (int) (screen.height * 0.72));
        dialog.setSize(Math.min(w, screen.width), Math.min(h, screen.height));
        dialog.setMinimumSize(new Dimension(560, 560));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    // ── INFO PANEL ───────────────────────────────────────────────────────────

    private static JPanel buildInfoPanel(JLabel lblName, JLabel lblPhone) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GREEN_SOFT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(GREEN_BORDER);
                g2.setStroke(new java.awt.BasicStroke(1.4f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel titleLbl = new JLabel("Thông tin khách hàng đã chọn");
        titleLbl.setFont(new Font("Lexend", Font.BOLD, 14));
        titleLbl.setForeground(new Color(22, 101, 52));
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(infoRow("Khách hàng", lblName));
        panel.add(Box.createVerticalStrut(6));
        panel.add(infoRow("Số điện thoại", lblPhone));
        return panel;
    }

    private static JPanel infoRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(74, 124, 89));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valueLabel.setForeground(new Color(30, 41, 59));
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lbl, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    // ── UTILITIES ────────────────────────────────────────────────────────────

    private static JButton pillBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? bg : new Color(220, 220, 220));
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
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private static String nullDash(String s) {
        return s == null || s.isBlank() ? "--" : s.trim();
    }
}
