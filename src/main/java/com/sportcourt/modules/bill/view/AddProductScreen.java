package com.sportcourt.modules.bill.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.bill.dto.ServiceItem;
import com.sportcourt.modules.product.dto.ProductResponse;
import com.sportcourt.modules.product.dto.ProductSearchCriteria;
import com.sportcourt.modules.product.service.ProductService;
import com.sportcourt.modules.product.service.ProductServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Màn "Thêm sản phẩm" — chọn sản phẩm + số lượng để chuyển về hóa đơn.
 * Bấm "Chọn" hiện stepper (- N +); "Hoàn tất" trả danh sách đã chọn về BillEditScreen.
 * Chưa lưu DB — chỉ truyền dữ liệu trong bộ nhớ.
 */
public class AddProductScreen extends JPanel {

    private static final Color WHITE        = Color.WHITE;
    private static final Color SELECT_BLUE  = new Color(29, 78, 216);
    private static final Color ACTION_GREEN = new Color(46, 204, 88);
    private static final Color TITLE_DARK   = new Color(17, 24, 39);
    private static final Color MUTED        = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    private static final NumberFormat MONEY_FMT = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private final ProductService service = new ProductServiceImpl();
    private final Runnable onBack;
    private final Consumer<List<ServiceItem>> onComplete;

    private final JTextField searchField = new JTextField();
    private final JPanel gridPanel = new JPanel();
    private final JLabel resultLabel = new JLabel();

    private final Map<String, Integer> selectedQty = new LinkedHashMap<>();
    private final Map<String, ProductResponse> prodById = new LinkedHashMap<>();

    public AddProductScreen(Runnable onBack, Consumer<List<ServiceItem>> onComplete) {
        this.onBack = onBack;
        this.onComplete = onComplete;
        AppFonts.register();
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel card = mainContainer();
        card.add(buildTopBar(), BorderLayout.NORTH);
        card.add(buildBody(),   BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);

        reload();
    }

    // ── Containers ───────────────────────────────────────────────────────────

    private JPanel mainContainer() {
        JPanel container = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.dispose();
            }
            @Override protected void paintChildren(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(12, 0, 16, 0));
        return container;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(16, 24, 12, 24));

        JButton backBtn = new JButton("← Quay lại");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.setForeground(MUTED);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

        JButton doneBtn = new JButton("Hoàn tất") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACTION_GREEN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        doneBtn.setFont(new Font("Lexend", Font.BOLD, 13));
        doneBtn.setForeground(WHITE);
        doneBtn.setContentAreaFilled(false);
        doneBtn.setBorderPainted(false);
        doneBtn.setFocusPainted(false);
        doneBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        doneBtn.setBorder(new EmptyBorder(9, 22, 9, 22));
        doneBtn.addActionListener(e -> complete());

        bar.add(backBtn, BorderLayout.WEST);
        bar.add(doneBtn, BorderLayout.EAST);
        return bar;
    }

    private void complete() {
        List<ServiceItem> result = new ArrayList<>();
        for (Map.Entry<String, Integer> en : selectedQty.entrySet()) {
            ProductResponse p = prodById.get(en.getKey());
            if (p == null || en.getValue() <= 0) continue;
            result.add(new ServiceItem(
                    null, p.getMaSp(), null, p.getTenSp(),
                    en.getValue(), p.getGia(), "ĐANG SỬ DỤNG"));
        }
        if (onComplete != null) onComplete.accept(result);
    }

    // ── Body ─────────────────────────────────────────────────────────────────

    private JComponent buildBody() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(WHITE);
        content.setBorder(new EmptyBorder(4, 28, 28, 28));

        JLabel title = new JLabel("Thêm sản phẩm");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(TITLE_DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);

        // Header: "Danh mục sản phẩm (N)" + ô tìm kiếm
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        header.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel cat = new JLabel("Danh mục sản phẩm");
        cat.setFont(new Font("Lexend", Font.BOLD, 16));
        cat.setForeground(TITLE_DARK);
        resultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultLabel.setForeground(MUTED);
        JPanel hl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hl.setOpaque(false);
        hl.add(cat);
        hl.add(resultLabel);

        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm sản phẩm...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        searchField.setPreferredSize(new Dimension(340, 38));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { reload(); }
            @Override public void removeUpdate(DocumentEvent e) { reload(); }
            @Override public void changedUpdate(DocumentEvent e) { reload(); }
        });
        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(searchField);

        header.add(hl,        BorderLayout.WEST);
        header.add(searchWrap, BorderLayout.EAST);

        gridPanel.setLayout(new GridLayout(0, 3, 16, 16));
        gridPanel.setBackground(WHITE);
        gridPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setBackground(WHITE);
        gridWrap.setAlignmentX(LEFT_ALIGNMENT);
        gridWrap.add(gridPanel, BorderLayout.NORTH);

        content.add(title);
        content.add(Box.createVerticalStrut(12));
        content.add(header);
        content.add(Box.createVerticalStrut(6));
        content.add(gridWrap);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    private void reload() {
        gridPanel.removeAll();
        List<ProductResponse> list;
        try {
            ProductSearchCriteria c = new ProductSearchCriteria();
            c.setKeyword(searchField.getText().trim());
            c.setIncludeDeleted(false);
            list = service.searchProducts(c);
        } catch (Exception ex) {
            resultLabel.setText("(lỗi tải dữ liệu)");
            gridPanel.revalidate();
            gridPanel.repaint();
            return;
        }
        resultLabel.setText("(" + list.size() + " kết quả)");
        for (ProductResponse p : list) {
            gridPanel.add(productCard(p));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel productCard(ProductResponse p) {
        JPanel cardPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel name = new JLabel(p.getTenSp() == null ? "--" : p.getTenSp());
        name.setFont(new Font("Lexend", Font.BOLD, 15));
        name.setForeground(TITLE_DARK);
        name.setAlignmentX(LEFT_ALIGNMENT);

        int stockVal = p.getSlTon() == null ? 0 : p.getSlTon();
        JLabel stock = new JLabel("Số lượng còn lại: " + stockVal);
        stock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stock.setForeground(MUTED);
        stock.setAlignmentX(LEFT_ALIGNMENT);

        JLabel price = new JLabel(money(p.getGia()) + "VNĐ / 1 " + (p.getDvt() == null ? "" : p.getDvt()));
        price.setFont(new Font("Lexend", Font.BOLD, 15));
        price.setForeground(TITLE_DARK);
        price.setAlignmentX(LEFT_ALIGNMENT);

        JPanel control = new JPanel(new BorderLayout());
        control.setOpaque(false);
        control.setAlignmentX(LEFT_ALIGNMENT);
        control.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        if (selectedQty.containsKey(p.getMaSp())) {
            showStepper(control, p);
        } else {
            showChoose(control, p);
        }

        cardPanel.add(name);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(stock);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(price);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(control);
        return cardPanel;
    }

    private void showChoose(JPanel holder, ProductResponse p) {
        holder.removeAll();
        JButton choose = new JButton("Chọn") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SELECT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        choose.setFont(new Font("Lexend", Font.BOLD, 13));
        choose.setForeground(WHITE);
        choose.setContentAreaFilled(false);
        choose.setBorderPainted(false);
        choose.setFocusPainted(false);
        choose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        choose.setBorder(new EmptyBorder(8, 0, 8, 0));
        choose.addActionListener(ev -> {
            selectedQty.put(p.getMaSp(), 1);
            prodById.put(p.getMaSp(), p);
            showStepper(holder, p);
        });
        holder.add(choose, BorderLayout.CENTER);
        holder.revalidate();
        holder.repaint();
    }

    private void showStepper(JPanel holder, ProductResponse p) {
        holder.removeAll();
        prodById.put(p.getMaSp(), p);
        int q = selectedQty.getOrDefault(p.getMaSp(), 1);

        JLabel qty = new JLabel(String.valueOf(q), SwingConstants.CENTER);
        qty.setFont(new Font("Lexend", Font.BOLD, 15));
        qty.setForeground(TITLE_DARK);
        qty.setPreferredSize(new Dimension(40, 28));

        JButton minus = circleBtn("−");
        minus.addActionListener(ev -> {
            int cur = selectedQty.getOrDefault(p.getMaSp(), 1) - 1;
            if (cur <= 0) {
                selectedQty.remove(p.getMaSp());
                showChoose(holder, p);
            } else {
                selectedQty.put(p.getMaSp(), cur);
                qty.setText(String.valueOf(cur));
            }
        });
        JButton plus = circleBtn("+");
        plus.addActionListener(ev -> {
            int cur = selectedQty.getOrDefault(p.getMaSp(), 1) + 1;
            selectedQty.put(p.getMaSp(), cur);
            qty.setText(String.valueOf(cur));
        });

        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        stepper.setOpaque(false);
        stepper.add(minus);
        stepper.add(qty);
        stepper.add(plus);
        holder.add(stepper, BorderLayout.CENTER);
        holder.revalidate();
        holder.repaint();
    }

    private JButton circleBtn(String sign) {
        Color fill = "+".equals(sign) ? new Color(46, 204, 88) : new Color(220, 53, 69);
        JButton b = new JButton(sign) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(26, 26));
        b.setFont(new Font("Segoe UI", Font.BOLD, 15));
        b.setForeground(WHITE);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static String money(BigDecimal v) {
        return MONEY_FMT.format(v == null ? BigDecimal.ZERO : v);
    }
}
