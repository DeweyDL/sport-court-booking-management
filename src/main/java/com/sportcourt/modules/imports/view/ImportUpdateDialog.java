package com.sportcourt.modules.imports.view;

import com.sportcourt.modules.imports.view.ImportMockData.ImportEquipmentDetail;
import com.sportcourt.modules.imports.view.ImportMockData.ImportItem;
import com.sportcourt.modules.imports.view.ImportMockData.ImportProductDetail;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dialog xem chi tiết phiếu nhập hàng.
 * Hiển thị header readonly + 2 nút toggle (Dụng cụ / Sản phẩm).
 */
final class ImportUpdateDialog {

    private static final Color DIALOG_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BRAND_GREEN = new Color(22, 101, 52);
    private static final Color BRAND_GREEN_BG = new Color(220, 252, 231);
    private static final Color BRAND_BLUE = new Color(29, 78, 216);
    private static final Color BRAND_BLUE_BG = new Color(239, 246, 255);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color READONLY_BG = new Color(241, 245, 249);
    private static final Color HEADER_BG = new Color(248, 249, 250);
    private static final Color ALTERNATE_ROW_BG = new Color(248, 250, 252);

    private static final Color TOGGLE_ACTIVE_BG = new Color(239, 246, 255);
    private static final Color TOGGLE_ACTIVE_FG = new Color(29, 78, 216);
    private static final Color TOGGLE_INACTIVE_BG = new Color(243, 244, 246);
    private static final Color TOGGLE_INACTIVE_FG = new Color(107, 114, 128);
    private static final int DETAIL_ROW_HEIGHT = 46;
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

    private ImportUpdateDialog() {
    }

    private static void applyResponsiveWindowSize(JDialog dialog, int baseWidth, int baseHeight) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double widthRatio = screenSize.getWidth() / 1920.0;
        double heightRatio = screenSize.getHeight() / 1080.0;
        double ratio = Math.min(widthRatio, heightRatio);
        if (ratio < 0.8) ratio = 0.8;

        int width = (int) (baseWidth * ratio);
        int height = (int) (baseHeight * ratio);
        dialog.setSize(width, height);
    }

    static void show(Component parent, ImportItem item) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, "Chỉnh sửa phiếu nhập", Dialog.ModalityType.APPLICATION_MODAL);
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

        JLabel title = new JLabel("Chỉnh sửa phiếu nhập");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Cập nhật thông tin phiếu nhập " + item.manh() + ".");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        // Center
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JTextField txtNcc = createSearchableField(java.util.Arrays.asList(MOCK_SUPPLIERS), null);
        JTextField txtNv = createSearchableField(java.util.Arrays.asList(MOCK_EMPLOYEES), null);
        JTextField txtChungTu = new JTextField(item.maChungTu());
        txtNcc.setText(item.tenNcc());
        txtNv.setText(item.tenNv());

        // --- Header fields ---
        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(createReadonlyField("Mã nhập hàng", item.manh()));
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(createField("Nhà cung cấp", txtNcc));
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(createField("Nhân viên", txtNv));
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(createField("Mã chứng từ", txtChungTu));
        formCard.add(Box.createVerticalStrut(14));
        formCard.add(createReadonlyField("Trị giá", formatCurrency(item.triGia())));

        centerPanel.add(formCard);
        centerPanel.add(Box.createVerticalStrut(12));

        // --- Detail section with toggle ---
        JPanel detailCard = new JPanel(new BorderLayout(0, 10));
        detailCard.setBackground(CARD_BG);
        detailCard.setBorder(new EmptyBorder(18, 18, 18, 18));
        detailCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnEquipment = createToggleButton("Dụng cụ", true);
        JButton btnProduct = createToggleButton("Sản phẩm", false);
        String[] activeDetail = {"EQUIP"};

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        togglePanel.setOpaque(false);
        togglePanel.add(btnEquipment);
        togglePanel.add(btnProduct);

        CardLayout detailCardLayout = new CardLayout();
        JPanel detailContainer = new JPanel(detailCardLayout);
        detailContainer.setOpaque(false);

        // Load mock details for this import
        List<ImportEquipmentDetail> equipDetails = ImportMockData.createSampleEquipmentDetails().stream()
                .filter(d -> d.manh().equals(item.manh())).toList();
        List<ImportProductDetail> prodDetails = ImportMockData.createSampleProductDetails().stream()
                .filter(d -> d.manh().equals(item.manh())).toList();

        DetailTableView equipTable = buildEquipmentTable(equipDetails);
        DetailTableView prodTable = buildProductTable(prodDetails);
        equipTable.editable = true;
        prodTable.editable = true;
        renderDetailTable(equipTable);
        renderDetailTable(prodTable);

        JButton addRowButton = createPillButton("+ Thêm dòng", BRAND_GREEN_BG, BRAND_GREEN);
        addRowButton.addActionListener(e -> {
            if ("PROD".equals(activeDetail[0])) {
                addBlankDetailRow(prodTable);
                return;
            }
            addBlankDetailRow(equipTable);
        });

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);
        rightActions.add(addRowButton);

        JPanel detailHeaderPanel = new JPanel(new BorderLayout());
        detailHeaderPanel.setOpaque(false);
        detailHeaderPanel.add(togglePanel, BorderLayout.WEST);
        detailHeaderPanel.add(rightActions, BorderLayout.EAST);
        detailCard.add(detailHeaderPanel, BorderLayout.NORTH);

        detailContainer.add(equipTable.panel, "EQUIP");
        detailContainer.add(prodTable.panel, "PROD");
        detailCardLayout.show(detailContainer, "EQUIP");
        detailCard.add(detailContainer, BorderLayout.CENTER);

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

        // Footer
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(8, 0, 0, 0));
        JButton confirmBtn = createPillButton("Xác nhận", BRAND_GREEN_BG, BRAND_GREEN);
        JButton cancelBtn = createPillButton("Hủy", new Color(229, 231, 235), new Color(31, 41, 55));
        confirmBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(dialog, "Đã ghi nhận cập nhật (mock).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        actions.add(confirmBtn);
        actions.add(cancelBtn);
        root.add(actions, BorderLayout.SOUTH);

        applyResponsiveWindowSize(dialog, 900, 800);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    // --------- Build detail tables ---------

    private static DetailTableView buildEquipmentTable(List<ImportEquipmentDetail> details) {
        List<String[]> rows = new ArrayList<>();
        for (ImportEquipmentDetail d : details) {
            BigDecimal thanhTien = d.donGia().multiply(BigDecimal.valueOf(d.slThucNhap()))
                    .multiply(BigDecimal.ONE.subtract(d.cktm().divide(BigDecimal.valueOf(100))));
            rows.add(new String[]{
                    d.maDc(), d.tenDc(),
                    String.valueOf(d.slChungTu()), String.valueOf(d.slThucNhap()),
                    formatCurrency(d.donGia()), d.cktm() + "%",
                    formatCurrency(thanhTien)
            });
        }
        return buildDetailTable(
                new String[]{"Mã DC", "Tên DC", "SL C.Từ", "SL T.Nhập", "Đơn giá", "CKTM(%)", "Thành tiền", ""},
                rows,
                "Không có chi tiết dụng cụ.",
                true
        );
    }

    private static DetailTableView buildProductTable(List<ImportProductDetail> details) {
        List<String[]> rows = new ArrayList<>();
        for (ImportProductDetail d : details) {
            BigDecimal thanhTien = d.donGia().multiply(BigDecimal.valueOf(d.slThucNhap()))
                    .multiply(BigDecimal.ONE.add(d.vat().divide(BigDecimal.valueOf(100))));
            rows.add(new String[]{
                    d.maSp(), d.tenSp(),
                    String.valueOf(d.slChungTu()), String.valueOf(d.slThucNhap()),
                    formatCurrency(d.donGia()), d.vat() + "%",
                    formatCurrency(thanhTien)
            });
        }
        return buildDetailTable(
                new String[]{"Mã SP", "Tên SP", "SL C.Từ", "SL T.Nhập", "Đơn giá", "VAT(%)", "Thành tiền", ""},
                rows,
                "Không có chi tiết sản phẩm.",
                false
        );
    }

    private static DetailTableView buildDetailTable(String[] columns, List<String[]> rows, String emptyMessage, boolean equipmentTable) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(CARD_BG);

        JScrollPane scroll = new JScrollPane(tablePanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(0, 200));
        panel.add(scroll, BorderLayout.CENTER);
        DetailTableView view = new DetailTableView(panel, tablePanel, columns, new ArrayList<>(rows), emptyMessage, equipmentTable);
        renderDetailTable(view);
        return view;
    }

    private static void addBlankDetailRow(DetailTableView view) {
        view.editable = true;
        view.rows.add(new String[]{"", "", "", "", "", "", ""});
        renderDetailTable(view);
    }

    private static void renderDetailTable(DetailTableView view) {
        view.tablePanel.removeAll();
        view.tablePanel.add(createDetailHeader(view.columns));

        if (view.rows.isEmpty()) {
            view.tablePanel.add(createEmptyDetailRow(view.emptyMessage));
        } else {
            for (int i = 0; i < view.rows.size(); i++) {
                view.tablePanel.add(view.editable
                        ? createEditableDetailRow(view, i, view.rows.get(i))
                        : createReadonlyDetailRow(view, i, view.rows.get(i)));
            }
        }

        view.tablePanel.revalidate();
        view.tablePanel.repaint();
    }

    private static final class DetailTableView {
        private final JPanel panel;
        private final JPanel tablePanel;
        private final String[] columns;
        private final List<String[]> rows;
        private final String emptyMessage;
        private final boolean equipmentTable;
        private boolean editable;

        private DetailTableView(JPanel panel, JPanel tablePanel, String[] columns, List<String[]> rows, String emptyMessage, boolean equipmentTable) {
            this.panel = panel;
            this.tablePanel = tablePanel;
            this.columns = columns;
            this.rows = rows;
            this.emptyMessage = emptyMessage;
            this.equipmentTable = equipmentTable;
            this.editable = false;
        }
    }

    // --------- Detail table UI ---------

    private static JPanel createDetailHeader(String[] columns) {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(0, 8, 0, 8)
        ));
        header.setPreferredSize(new Dimension(0, 36));
        header.setMinimumSize(new Dimension(0, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 4);

        double[] weights = {0.10, 0.22, 0.09, 0.09, 0.14, 0.09, 0.16, 0.11};
        for (int i = 0; i < columns.length; i++) {
            JLabel lbl = new JLabel(columns[i]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(new Color(107, 114, 128));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            
            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(false);
            cell.add(lbl, BorderLayout.CENTER);
            
            gbc.weightx = weights[i];
            if (i == columns.length - 1) gbc.insets = new Insets(0, 0, 0, 0);
            header.add(cell, gbc);
        }
        return header;
    }

    private static JPanel createReadonlyDetailRow(DetailTableView view, int idx, String[] values) {
        JPanel row = new JPanel(new GridBagLayout());
        Color bg = idx % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;
        row.setBackground(bg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 8, 0, 8)
        ));
        row.setPreferredSize(new Dimension(0, DETAIL_ROW_HEIGHT));
        row.setMinimumSize(new Dimension(0, DETAIL_ROW_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 4);

        double[] weights = {0.10, 0.22, 0.09, 0.09, 0.14, 0.09, 0.16, 0.11};
        for (int i = 0; i < values.length && i < weights.length - 1; i++) {
            JLabel lbl = new JLabel(values[i] == null || values[i].isBlank() ? "--" : values[i]);
            lbl.setFont(new Font("Segoe UI", i == 0 ? Font.BOLD : Font.PLAIN, 13));
            lbl.setForeground(i == 0 ? new Color(22, 163, 74) : new Color(31, 41, 55));
            lbl.setHorizontalAlignment(i <= 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
            
            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(false);
            cell.add(lbl, BorderLayout.CENTER);
            
            gbc.weightx = weights[i];
            row.add(cell, gbc);
        }

        gbc.weightx = weights[weights.length - 1];
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton delBtn = createMiniDeleteButton();
        delBtn.addActionListener(e -> {
            view.rows.remove(values);
            renderDetailTable(view);
        });
        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setOpaque(false);
        actionCell.add(delBtn);
        row.add(actionCell, gbc);
        return row;
    }

    private static JPanel createEditableDetailRow(DetailTableView view, int idx, String[] values) {
        JPanel row = new JPanel(new GridBagLayout());
        Color bg = idx % 2 == 0 ? Color.WHITE : ALTERNATE_ROW_BG;
        row.setBackground(bg);
        row.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(0, 8, 0, 8)
        ));
        row.setPreferredSize(new Dimension(0, DETAIL_ROW_HEIGHT));
        row.setMinimumSize(new Dimension(0, DETAIL_ROW_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 4);

        double[] weights = {0.10, 0.22, 0.09, 0.09, 0.14, 0.09, 0.16, 0.11};
        JTextField[] rowFields = new JTextField[values.length];
        for (int i = 0; i < values.length && i < weights.length - 1; i++) {
            JTextField field;
            if (i == 1) {
                java.util.Map<String, String> source = view.equipmentTable ? MOCK_EQUIPMENTS : MOCK_PRODUCTS;
                field = createSearchableField(source.keySet(), selectedName -> {
                    values[1] = selectedName;
                    values[0] = source.getOrDefault(selectedName, "");
                    if (rowFields[0] != null) {
                        rowFields[0].setText(values[0]);
                    }
                });
                field.setText(values[i] == null ? "" : values[i]);
            } else {
                field = new JTextField(values[i] == null ? "" : values[i]);
            }
            rowFields[i] = field;
            if (i == 0) {
                field.setEditable(false);
                field.setFocusable(false);
            }
            styleEditableField(field);
            final int valueIndex = i;
            field.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    values[valueIndex] = field.getText();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    values[valueIndex] = field.getText();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    values[valueIndex] = field.getText();
                }
            });

            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(false);
            cell.add(field, BorderLayout.CENTER);
            
            gbc.weightx = weights[i];
            row.add(cell, gbc);
        }

        gbc.weightx = weights[weights.length - 1];
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton delBtn = createMiniDeleteButton();
        delBtn.addActionListener(e -> {
            view.rows.remove(values);
            renderDetailTable(view);
        });
        JPanel actionCell = new JPanel(new GridBagLayout());
        actionCell.setOpaque(false);
        actionCell.add(delBtn);
        row.add(actionCell, gbc);
        return row;
    }

    private static JPanel createEmptyDetailRow(String message) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(16, 10, 16, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel lbl = new JLabel(message);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(107, 114, 128));
        row.add(lbl, BorderLayout.CENTER);
        return row;
    }

    // --------- Shared helpers ---------

    private static JPanel createReadonlyField(String labelText, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setFocusable(false);
        field.setFont(new Font("Segoe UI", Font.BOLD, 15));
        field.setForeground(new Color(31, 41, 55));
        field.setBackground(READONLY_BG);
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 18),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

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
        field.setBackground(Color.WHITE);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
        panel.add(field);
        return panel;
    }

    private static void styleEditableField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(new Color(31, 41, 55));
        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 10),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        field.setBackground(Color.WHITE);
    }

    private static javax.swing.border.Border createUpdateFieldBorder(boolean focused) {
        Color line = focused ? new Color(59, 130, 246) : new Color(191, 207, 226);
        return BorderFactory.createCompoundBorder(
                new RoundedLineBorder(line, 18),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)
        );
    }

    private static javax.swing.border.Border createTableFieldBorder(boolean focused) {
        Color line = focused ? new Color(59, 130, 246) : new Color(191, 207, 226);
        return BorderFactory.createCompoundBorder(
                new RoundedLineBorder(line, 12),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        );
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

    private static String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VNĐ";
        return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(value) + " VNĐ";
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
