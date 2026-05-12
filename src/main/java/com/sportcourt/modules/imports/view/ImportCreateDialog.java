package com.sportcourt.modules.imports.view;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog tạo phiếu nhập hàng mới.
 * Bao gồm thông tin header + chi tiết sản phẩm/dụng cụ.
 */
final class ImportCreateDialog {

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(16, 110, 0);
    private static final Color BRAND_GREEN_BG = new Color(228, 250, 226);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color HEADER_BG = new Color(248, 249, 250);
    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);
    private static final int DETAIL_ROW_HEIGHT = 46;

    // Toggle button colors
    private static final Color TOGGLE_ACTIVE_BG = new Color(219, 234, 254);
    private static final Color TOGGLE_ACTIVE_FG = new Color(29, 78, 216);
    private static final Color TOGGLE_INACTIVE_BG = new Color(239, 246, 255);
    private static final Color TOGGLE_INACTIVE_FG = new Color(37, 99, 235);

    private static final String[] MOCK_EMPLOYEES = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Minh Châu", "Phạm Đức Duy", "Hoàng Thị Eo", "Vũ Quang Phúc", "Đỗ Thanh Giang", "Bùi Thị Hạnh"
    };
    private static final String[] MOCK_SUPPLIERS = {
            "Công ty TNHH Thể thao Viễn Đông", 
            "Cửa hàng Dụng cụ TDTT Tiến Đạt", 
            "Đại lý phân phối Yonex VN", 
            "Nhà cung cấp Nước giải khát CocaCola", 
            "Tập đoàn Động Lực"
    };
    private static final java.util.Map<String, String> MOCK_PRODUCTS = new java.util.HashMap<>();
    private static final java.util.Map<String, String> MOCK_EQUIPMENTS = new java.util.HashMap<>();
    static {
        MOCK_PRODUCTS.put("Nước uống ion Pocari", "SP001");
        MOCK_PRODUCTS.put("Khăn lạnh thể thao", "SP002");
        MOCK_PRODUCTS.put("Băng dán thể thao", "SP003");
        MOCK_PRODUCTS.put("Bình nước thể thao 750ml", "SP004");
        MOCK_PRODUCTS.put("Grip vợt tennis", "SP005");

        MOCK_EQUIPMENTS.put("Vợt cầu lông Yonex", "DC001");
        MOCK_EQUIPMENTS.put("Quả bóng đá Mikasa", "DC002");
        MOCK_EQUIPMENTS.put("Giày thể thao Adidas", "DC004");
        MOCK_EQUIPMENTS.put("Bóng rổ Spalding", "DC005");
        MOCK_EQUIPMENTS.put("Vợt tennis Wilson", "DC007");
        MOCK_EQUIPMENTS.put("Bàn bóng bàn Butterfly", "DC009");
    }

    private ImportCreateDialog() {
    }

    // --------- Inner row models ---------
    private static class ProductRow {
        JTextField txtMaSp = new JTextField();
        JTextField txtTenSp = new JTextField();
        JTextField txtSlChungTu = new JTextField();
        JTextField txtSlThucNhap = new JTextField();
        JTextField txtDonGia = new JTextField();
        JTextField txtVat = new JTextField();
    }

    private static class EquipmentRow {
        JTextField txtMaDc = new JTextField();
        JTextField txtTenDc = new JTextField();
        JTextField txtSlChungTu = new JTextField();
        JTextField txtSlThucNhap = new JTextField();
        JTextField txtDonGia = new JTextField();
        JTextField txtCktm = new JTextField();
    }

    static void show(Component parent, com.sportcourt.modules.imports.view.ImportMockData.ImportItem itemToEdit) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        String dialogTitle = itemToEdit == null ? "Tạo phiếu nhập hàng" : "Chỉnh sửa phiếu nhập";
        JDialog dialog = new JDialog(owner, dialogTitle, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(DIALOG_BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        dialog.setContentPane(root);

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(itemToEdit == null ? "Tạo phiếu nhập hàng" : "Chỉnh sửa phiếu nhập");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Nhập thông tin phiếu nhập và chi tiết hàng hóa.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        // Center: header fields + detail section
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // --- Header fields ---
        JTextField txtNcc = createSearchableField(java.util.Arrays.asList(MOCK_SUPPLIERS), null);
        JTextField txtNv = createSearchableField(java.util.Arrays.asList(MOCK_EMPLOYEES), null);
        JTextField txtChungTu = new JTextField();
        
        if (itemToEdit != null) {
            txtNcc.setText(itemToEdit.tenNcc());
            txtNv.setText(itemToEdit.tenNv());
            txtChungTu.setText(itemToEdit.maChungTu());
        }

        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(createField("Nhà cung cấp", txtNcc));
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(createField("Nhân viên", txtNv));
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(createField("Mã chứng từ", txtChungTu));

        centerPanel.add(formCard);
        centerPanel.add(Box.createVerticalStrut(12));

        // --- Detail section with toggle buttons ---
        List<EquipmentRow> equipmentRows = new ArrayList<>();
        List<ProductRow> productRows = new ArrayList<>();
        
        if (itemToEdit == null) {
            EquipmentRow firstRow = new EquipmentRow();
            equipmentRows.add(firstRow);
        }

        JPanel detailCard = new JPanel(new BorderLayout(0, 10));
        detailCard.setBackground(CARD_BG);
        detailCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        detailCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Toggle buttons
        JButton btnEquipment = createToggleButton("Dụng cụ", true);
        JButton btnProduct = createToggleButton("Sản phẩm", false);
        String[] activeDetail = {"EQUIP"};

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        togglePanel.setOpaque(false);
        togglePanel.add(btnEquipment);
        togglePanel.add(btnProduct);

        // Detail tables container (CardLayout)
        CardLayout detailCardLayout = new CardLayout();
        JPanel detailContainer = new JPanel(detailCardLayout);
        detailContainer.setOpaque(false);

        // Equipment detail panel
        JPanel equipPanel = new JPanel(new BorderLayout(0, 8));
        equipPanel.setOpaque(false);
        JPanel equipTablePanel = new JPanel();
        equipTablePanel.setLayout(new BoxLayout(equipTablePanel, BoxLayout.Y_AXIS));
        equipTablePanel.setBackground(CARD_BG);
        equipTablePanel.add(createEquipmentDetailHeader());

        JScrollPane equipScroll = new JScrollPane(equipTablePanel);
        equipScroll.setBorder(BorderFactory.createEmptyBorder());
        equipScroll.setPreferredSize(new Dimension(0, 200));
        equipPanel.add(equipScroll, BorderLayout.CENTER);

        // Product detail panel
        JPanel prodPanel = new JPanel(new BorderLayout(0, 8));
        prodPanel.setOpaque(false);
        JPanel prodTablePanel = new JPanel();
        prodTablePanel.setLayout(new BoxLayout(prodTablePanel, BoxLayout.Y_AXIS));
        prodTablePanel.setBackground(CARD_BG);
        prodTablePanel.add(createProductDetailHeader());

        JScrollPane prodScroll = new JScrollPane(prodTablePanel);
        prodScroll.setBorder(BorderFactory.createEmptyBorder());
        prodScroll.setPreferredSize(new Dimension(0, 200));
        prodPanel.add(prodScroll, BorderLayout.CENTER);

        if (itemToEdit != null) {
            for (com.sportcourt.modules.imports.view.ImportMockData.ImportEquipmentDetail d : ImportMockData.createSampleEquipmentDetails()) {
                if (d.manh().equals(itemToEdit.manh())) {
                    EquipmentRow row = new EquipmentRow();
                    row.txtMaDc.setText(d.maDc());
                    row.txtTenDc.setText(d.tenDc());
                    row.txtSlChungTu.setText(String.valueOf(d.slChungTu()));
                    row.txtSlThucNhap.setText(String.valueOf(d.slThucNhap()));
                    row.txtDonGia.setText(String.valueOf(d.donGia()));
                    row.txtCktm.setText(String.valueOf(d.cktm()));
                    equipmentRows.add(row);
                }
            }
            for (com.sportcourt.modules.imports.view.ImportMockData.ImportProductDetail d : ImportMockData.createSampleProductDetails()) {
                if (d.manh().equals(itemToEdit.manh())) {
                    ProductRow row = new ProductRow();
                    row.txtMaSp.setText(d.maSp());
                    row.txtTenSp.setText(d.tenSp());
                    row.txtSlChungTu.setText(String.valueOf(d.slChungTu()));
                    row.txtSlThucNhap.setText(String.valueOf(d.slThucNhap()));
                    row.txtDonGia.setText(String.valueOf(d.donGia()));
                    row.txtVat.setText(String.valueOf(d.vat()));
                    productRows.add(row);
                }
            }
        }

        for (int i = 0; i < equipmentRows.size(); i++) {
            equipTablePanel.add(createEquipmentDataRow(equipmentRows.get(i), equipmentRows, equipTablePanel, i));
        }
        for (int i = 0; i < productRows.size(); i++) {
            prodTablePanel.add(createProductDataRow(productRows.get(i), productRows, prodTablePanel, i));
        }

        JButton addRowButton = createPillButton("+ Thêm dòng", BRAND_GREEN_BG, BRAND_GREEN);
        addRowButton.addActionListener(e -> {
            if ("PROD".equals(activeDetail[0])) {
                ProductRow row = new ProductRow();
                productRows.add(row);
                prodTablePanel.add(createProductDataRow(row, productRows, prodTablePanel, productRows.size() - 1));
                prodTablePanel.revalidate();
                prodTablePanel.repaint();
                return;
            }

            EquipmentRow row = new EquipmentRow();
            equipmentRows.add(row);
            equipTablePanel.add(createEquipmentDataRow(row, equipmentRows, equipTablePanel, equipmentRows.size() - 1));
            equipTablePanel.revalidate();
            equipTablePanel.repaint();
        });

        JPanel detailHeaderPanel = new JPanel(new BorderLayout());
        detailHeaderPanel.setOpaque(false);
        detailHeaderPanel.add(togglePanel, BorderLayout.WEST);
        detailHeaderPanel.add(addRowButton, BorderLayout.EAST);
        detailCard.add(detailHeaderPanel, BorderLayout.NORTH);

        detailContainer.add(equipPanel, "EQUIP");
        detailContainer.add(prodPanel, "PROD");
        detailCardLayout.show(detailContainer, "EQUIP");
        detailCard.add(detailContainer, BorderLayout.CENTER);

        // Toggle button actions
        btnEquipment.addActionListener(e -> {
            activeDetail[0] = "EQUIP";
            detailCardLayout.show(detailContainer, "EQUIP");
            setToggleActive(btnEquipment, true);
            setToggleActive(btnProduct, false);
        });
        btnProduct.addActionListener(e -> {
            activeDetail[0] = "PROD";
            detailCardLayout.show(detailContainer, "PROD");
            setToggleActive(btnProduct, true);
            setToggleActive(btnEquipment, false);
        });

        centerPanel.add(detailCard);

        JScrollPane mainScroll = new JScrollPane(centerPanel);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(mainScroll, BorderLayout.CENTER);

        // Actions
        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton cancelBtn = createPillButton("Hủy", new Color(229, 231, 235), new Color(31, 41, 55));
        JButton saveBtn = createPillButton(itemToEdit == null ? "Tạo phiếu nhập" : "Lưu thay đổi", BRAND_GREEN_BG, BRAND_GREEN);

        cancelBtn.addActionListener(event -> dialog.dispose());
        saveBtn.addActionListener(event -> {
            if (txtNcc.getText().trim().isEmpty() || txtNv.getText().trim().isEmpty() || txtChungTu.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin phiếu nhập.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            System.out.println("[Import Create] NCC: " + txtNcc.getText() + ", NV: " + txtNv.getText()
                    + ", CT: " + txtChungTu.getText()
                    + ", SP rows: " + productRows.size() + ", DC rows: " + equipmentRows.size());
            JOptionPane.showMessageDialog(dialog, "Đã ghi nhận (mock). Phiếu nhập sẽ được lưu khi có BE.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        actions.add(cancelBtn);
        actions.add(saveBtn);
        root.add(actions, BorderLayout.SOUTH);

        dialog.setSize(850, 700);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // --------- Detail table headers ---------

    private static JPanel createEquipmentDetailHeader() {
        return createDetailHeader(new String[]{"Mã DC", "Tên DC", "SL C.Từ", "SL T.Nhập", "Đơn giá", "CKTM(%)", ""});
    }

    private static JPanel createProductDetailHeader() {
        return createDetailHeader(new String[]{"Mã SP", "Tên SP", "SL C.Từ", "SL T.Nhập", "Đơn giá", "VAT(%)", ""});
    }

    private static JPanel createDetailHeader(String[] columns) {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 8, 0, 8)
        ));
        header.setPreferredSize(new Dimension(0, 36));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        int[] widths = {80, 160, 70, 70, 100, 70, 76};
        for (int i = 0; i < columns.length; i++) {
            JLabel lbl = new JLabel(columns[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(new Color(107, 114, 128));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            Dimension d = new Dimension(widths[i], 36);
            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(false);
            cell.add(lbl, BorderLayout.CENTER);
            cell.setPreferredSize(d);
            cell.setMinimumSize(d);
            cell.setMaximumSize(d);
            header.add(cell);
            if (i < columns.length - 1) header.add(Box.createHorizontalStrut(4));
        }
        return header;
    }

    // --------- Detail data rows ---------

    private static JPanel createEquipmentDataRow(EquipmentRow row, List<EquipmentRow> rows, JPanel tablePanel, int idx) {
        String existingTenDc = row.txtTenDc.getText();
        row.txtTenDc = createSearchableField(MOCK_EQUIPMENTS.keySet(), name -> {
            row.txtMaDc.setText(MOCK_EQUIPMENTS.get(name));
        });
        if (existingTenDc != null && !existingTenDc.isEmpty()) {
            row.txtTenDc.setText(existingTenDc);
        }
        row.txtMaDc.setEditable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        Color bg = idx % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 8, 0, 8)
        ));
        panel.setPreferredSize(new Dimension(0, DETAIL_ROW_HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, DETAIL_ROW_HEIGHT));

        int[] widths = {80, 160, 70, 70, 100, 70, 76};
        JTextField[] fields = {row.txtMaDc, row.txtTenDc, row.txtSlChungTu, row.txtSlThucNhap, row.txtDonGia, row.txtCktm};
        for (int i = 0; i < fields.length; i++) {
            styleSmallField(fields[i], widths[i]);
            panel.add(wrapFieldCell(fields[i], widths[i]));
            panel.add(Box.createHorizontalStrut(4));
        }

        // Delete button
        JButton delBtn = createMiniDeleteButton();
        delBtn.addActionListener(e -> {
            rows.remove(row);
            tablePanel.remove(panel);
            tablePanel.revalidate();
            tablePanel.repaint();
        });
        JPanel delCell = new JPanel(new GridBagLayout());
        delCell.setBackground(bg);
        delCell.setOpaque(true);
        delCell.setBorder(new EmptyBorder(0, 0, 0, 0));
        delCell.add(delBtn);
        Dimension dd = new Dimension(76, DETAIL_ROW_HEIGHT);
        delCell.setPreferredSize(dd);
        delCell.setMinimumSize(dd);
        delCell.setMaximumSize(dd);
        panel.add(delCell);

        return panel;
    }

    private static JPanel createProductDataRow(ProductRow row, List<ProductRow> rows, JPanel tablePanel, int idx) {
        String existingTenSp = row.txtTenSp.getText();
        row.txtTenSp = createSearchableField(MOCK_PRODUCTS.keySet(), name -> {
            row.txtMaSp.setText(MOCK_PRODUCTS.get(name));
        });
        if (existingTenSp != null && !existingTenSp.isEmpty()) {
            row.txtTenSp.setText(existingTenSp);
        }
        row.txtMaSp.setEditable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        Color bg = idx % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 8, 0, 8)
        ));
        panel.setPreferredSize(new Dimension(0, DETAIL_ROW_HEIGHT));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, DETAIL_ROW_HEIGHT));

        int[] widths = {80, 160, 70, 70, 100, 70, 76};
        JTextField[] fields = {row.txtMaSp, row.txtTenSp, row.txtSlChungTu, row.txtSlThucNhap, row.txtDonGia, row.txtVat};
        for (int i = 0; i < fields.length; i++) {
            styleSmallField(fields[i], widths[i]);
            panel.add(wrapFieldCell(fields[i], widths[i]));
            panel.add(Box.createHorizontalStrut(4));
        }

        JButton delBtn = createMiniDeleteButton();
        delBtn.addActionListener(e -> {
            rows.remove(row);
            tablePanel.remove(panel);
            tablePanel.revalidate();
            tablePanel.repaint();
        });
        JPanel delCell = new JPanel(new GridBagLayout());
        delCell.setBackground(bg);
        delCell.setOpaque(true);
        delCell.setBorder(new EmptyBorder(0, 0, 0, 0));
        delCell.add(delBtn);
        Dimension dd = new Dimension(76, DETAIL_ROW_HEIGHT);
        delCell.setPreferredSize(dd);
        delCell.setMinimumSize(dd);
        delCell.setMaximumSize(dd);
        panel.add(delCell);

        return panel;
    }

    // --------- Shared UI helpers ---------

    private static JTextField createSearchableField(java.util.Collection<String> items, java.util.function.Consumer<String> onSelect) {
        JTextField field = new JTextField();
        JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(false); // Ngăn popup chiếm focus của textfield
        
        Runnable filterTask = () -> {
            SwingUtilities.invokeLater(() -> {
                field.putClientProperty("internalClose", true);
                if (popup.isVisible()) popup.setVisible(false);
                field.putClientProperty("internalClose", false);
                
                popup.removeAll();
                
                String text = field.getText().toLowerCase();
                int count = 0;
                for (String name : items) {
                    if (name.toLowerCase().contains(text)) {
                        JMenuItem item = new JMenuItem(name);
                        item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        item.addActionListener(ev -> {
                            field.putClientProperty("updating", true);
                            field.putClientProperty("internalClose", true);
                            field.setText(name);
                            popup.setVisible(false);
                            if (onSelect != null) onSelect.accept(name);
                            field.putClientProperty("internalClose", false);
                            field.putClientProperty("updating", false);
                        });
                        popup.add(item);
                        count++;
                        if (count >= 15) break;
                    }
                }
                
                if (count > 0 && field.isShowing()) {
                    popup.pack();
                    popup.show(field, 0, field.getHeight());
                }
            });
        };

        Runnable validateTask = () -> {
            SwingUtilities.invokeLater(() -> {
                if (Boolean.TRUE.equals(field.getClientProperty("updating"))) return;

                String text = field.getText().trim();
                if (text.isEmpty()) {
                    field.putClientProperty("updating", true);
                    field.setText("");
                    if (onSelect != null) onSelect.accept("");
                    field.putClientProperty("updating", false);
                    return;
                }

                String match = null;
                for (String name : items) {
                    if (name.equalsIgnoreCase(text)) {
                        match = name;
                        break;
                    }
                }

                if (match == null) {
                    for (String name : items) {
                        if (name.toLowerCase().contains(text.toLowerCase())) {
                            match = name;
                            break;
                        }
                    }
                }

                field.putClientProperty("updating", true);
                if (match == null) {
                    // Nếu không khớp hoàn toàn -> xóa trắng luôn
                    field.setText("");
                    if (onSelect != null) onSelect.accept("");
                } else {
                    field.setText(match);
                    if (onSelect != null) onSelect.accept(match);
                }
                field.putClientProperty("updating", false);
            });
        };

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { if (!Boolean.TRUE.equals(field.getClientProperty("updating"))) filterTask.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { if (!Boolean.TRUE.equals(field.getClientProperty("updating"))) filterTask.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { if (!Boolean.TRUE.equals(field.getClientProperty("updating"))) filterTask.run(); }
        });

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Do nothing on focus gained to prevent popup loops
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.putClientProperty("internalClose", true);
                if (popup.isVisible()) popup.setVisible(false);
                field.putClientProperty("internalClose", false);
                validateTask.run();
            }
        });
        
        field.addActionListener(e -> {
            field.putClientProperty("internalClose", true);
            if (popup.isVisible()) popup.setVisible(false);
            field.putClientProperty("internalClose", false);
            validateTask.run();
        });

        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                if (Boolean.TRUE.equals(field.getClientProperty("internalClose"))) return;
                validateTask.run();
            }
            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        field.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!popup.isVisible()) filterTask.run();
            }
        });

        return field;
    }

    private static JPanel createField(String labelText, JTextField field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 18),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        field.setBackground(new Color(249, 250, 251));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private static void styleSmallField(JTextField field, int width) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(createSmallFieldBorder(false));
        field.setBackground(Color.WHITE);
        field.setOpaque(true);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(createSmallFieldBorder(true));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(createSmallFieldBorder(false));
            }
        });
    }

    private static javax.swing.border.Border createSmallFieldBorder(boolean focused) {
        Color line = focused ? new Color(59, 130, 246) : new Color(191, 207, 226);
        return BorderFactory.createCompoundBorder(
                new RoundedLineBorder(line, 12),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        );
    }

    private static JPanel wrapFieldCell(JTextField field, int width) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setOpaque(false);
        cell.add(field, BorderLayout.CENTER);
        Dimension d = new Dimension(width, 32);
        cell.setPreferredSize(d);
        cell.setMinimumSize(d);
        cell.setMaximumSize(d);
        return cell;
    }

    private static JButton createToggleButton(String text, boolean active) {
        Color bg = active ? TOGGLE_ACTIVE_BG : TOGGLE_INACTIVE_BG;
        Color fg = active ? TOGGLE_ACTIVE_FG : TOGGLE_INACTIVE_FG;
        JButton btn = createPillButton(text, bg, fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.putClientProperty("isActive", active);
        return btn;
    }

    private static void setToggleActive(JButton btn, boolean active) {
        btn.putClientProperty("isActive", active);
        // Force repaint with new colors by recreating the button visuals
        btn.repaint();
    }

    private static JButton createMiniDeleteButton() {
        JButton btn = createPillButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBorder(new EmptyBorder(4, 8, 4, 8));
        Dimension d = new Dimension(64, 26);
        btn.setPreferredSize(d);
        btn.setMinimumSize(d);
        btn.setMaximumSize(d);
        return btn;
    }

    private static JButton createPillButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color paintBg = bg;
                if (getClientProperty("isActive") != null) {
                    boolean isActive = Boolean.TRUE.equals(getClientProperty("isActive"));
                    paintBg = isActive ? TOGGLE_ACTIVE_BG : TOGGLE_INACTIVE_BG;
                    setForeground(isActive ? TOGGLE_ACTIVE_FG : TOGGLE_INACTIVE_FG);
                }
                g2.setColor(paintBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g2);
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

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }
}
