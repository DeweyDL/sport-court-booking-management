package com.sportcourt.modules.dashboard.view;

import com.sportcourt.modules.dashboard.controller.DashboardController;
import com.sportcourt.modules.dashboard.dto.DashboardCourtCard;
import com.sportcourt.modules.dashboard.dto.DashboardSportTypeOption;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TestDashboard extends JPanel {

    private static final String HERO_IMAGE_PATH = "src/main/resources/image/BgDashboard.png";
    private static final String CARD_IMAGE_PATH = "src/main/resources/image/BgcardDashboard.png";

    private final Color bgColor = new Color(249, 249, 252);
    private final Color greenPrimary = new Color(57, 255, 20);
    private final Color darkText = new Color(15, 23, 42);
    private final Color grayUI = new Color(232, 232, 234);
    private final Color greenTextDark = new Color(16, 113, 0);

    private static final String SEARCH_PLACEHOLDER = "Tìm kiếm sân hoặc địa chỉ...";

    private final DashboardController controller = new DashboardController();
    private ResponsiveCardsGrid cardsGrid;
    private JTextField searchInput;
    private JPanel filterSection;
    private java.util.List<DashboardSportTypeOption> availableSportTypes = java.util.Collections.emptyList();

    public TestDashboard() {
        // Cố định: Thiết lập layout cho chính JPanel này để có thể chứa ScrollPane
        setLayout(new BorderLayout());

        ViewportWidthPanel mainContent = new ViewportWidthPanel(new GridBagLayout());
        mainContent.setBackground(bgColor);
        // Reduce padding to avoid overflow and keep hero banner wide.
        mainContent.setBorder(new EmptyBorder(25, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // 1. Logo
//        JLabel logo = new JLabel("RENTSTA");
//        logo.setFont(new Font("Lexend", Font.BOLD, 48));
//        logo.setForeground(darkText);
//        gbc.gridy = 0;
//        gbc.insets = new Insets(0, 0, 30, 0);
//        mainContent.add(logo, gbc);

        // 2. Hero Section
        gbc.gridy = 0;
        mainContent.add(createHeroSection(), gbc);

        // 3. Filter Buttons
        gbc.gridy = 1;
        gbc.insets = new Insets(30, 0, 30, 0);
        filterSection = createFilterSection();
        mainContent.add(filterSection, gbc);

        // 4. Discovery Header
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 20, 0);
        mainContent.add(createDiscoveryHeader(), gbc);

        // 5. Card Grid
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.cardsGrid = createCardsGrid(6);
        mainContent.add(this.cardsGrid, gbc);

        // Thanh cuộn tối giản
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        scrollPane.setBackground(bgColor);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setWheelScrollingEnabled(true);
        JScrollBar vbar = scrollPane.getVerticalScrollBar();
        vbar.setUI(new ModernScrollBarUI());
        vbar.setPreferredSize(new Dimension(8, 0));
        vbar.setUnitIncrement(20);
        vbar.setOpaque(false);
        vbar.setBackground(new Color(0, 0, 0, 0));
        vbar.setBorder(null);
        scrollPane.getViewport().setBackground(bgColor);

        // Ensure mouse wheel scroll works even when cursor is over inner components.
        installMouseWheelScroll(mainContent, vbar);

        scrollPane.getViewport().addChangeListener(e -> {
            Insets in = mainContent.getInsets();
            int w = scrollPane.getViewport().getWidth() - in.left - in.right;
            if (w > 0) {
                this.cardsGrid.updateColumns(w);
            }
        });

        // QUAN TRỌNG: Thêm scrollPane vào chính JPanel TestDashboard
        add(scrollPane, BorderLayout.CENTER);

        // Initial load: load sport types for filter bar, then show all courts/cards
        loadSportTypesAsync();
        loadCardsAsync(() -> controller.search(null));
    }

    private JPanel createHeroSection() {
        RoundedPanel hero = new RoundedPanel(40, HERO_IMAGE_PATH);
        hero.setPreferredSize(new Dimension(0, 380));
        hero.setLayout(new GridBagLayout());

        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        JLabel title = new JLabel("LÊN KÈO NGAY!!!");
        title.setFont(new Font("Lexend", Font.BOLD, 64));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getPreferredSize().height));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel searchBar = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        searchBar.setOpaque(false);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        searchBar.setBorder(new EmptyBorder(5, 25, 5, 5));

        searchInput = new JTextField(SEARCH_PLACEHOLDER);
        searchInput.setOpaque(false);
        searchInput.setBorder(null);
        searchInput.setForeground(Color.GRAY);
        searchInput.setFont(new Font("Lexend", Font.PLAIN, 18));
        searchInput.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(searchInput.getText())) {
                    searchInput.setText("");
                    searchInput.setForeground(darkText);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchInput.getText() == null || searchInput.getText().trim().isEmpty()) {
                    searchInput.setText(SEARCH_PLACEHOLDER);
                    searchInput.setForeground(Color.GRAY);
                }
            }
        });
        searchInput.addActionListener(e -> triggerSearch());

        RoundedButton btnSearch = new RoundedButton("TÌM NGAY", greenPrimary, new Color(16, 113, 0), 50);
        btnSearch.setFont(new Font("Lexend", Font.BOLD, 17));
        btnSearch.setPreferredSize(new Dimension(150, 50));
        btnSearch.addActionListener(e -> triggerSearch());

        searchBar.add(searchInput, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        centerContent.add(title);
        centerContent.add(Box.createRigidArea(new Dimension(0, 30)));
        centerContent.add(searchBar);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        // Keep banner content wide; large insets make the hero feel "shrunk".
        gbc.insets = new Insets(0, 28, 0, 28);

        hero.add(centerContent, gbc);
        return hero;
    }

    private JPanel createFilterSection() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        p.setBackground(bgColor);
        // Start with just "Tất cả". Actual sport type buttons are loaded from DB.
        rebuildFilterButtons(p);
        return p;
    }

    private void loadSportTypesAsync() {
        SwingWorker<List<DashboardSportTypeOption>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DashboardSportTypeOption> doInBackground() {
                return controller.loadAvailableSportTypes();
            }

            @Override
            protected void done() {
                try {
                    availableSportTypes = get();
                } catch (Exception e) {
                    availableSportTypes = java.util.Collections.emptyList();
                }

                if (filterSection != null) {
                    filterSection.removeAll();
                    rebuildFilterButtons(filterSection);
                    filterSection.revalidate();
                    filterSection.repaint();
                }
            }
        };
        worker.execute();
    }

    private void rebuildFilterButtons(JPanel container) {
        java.util.List<RoundedButton> buttons = new java.util.ArrayList<>();

        RoundedButton all = new RoundedButton("Tất cả", greenPrimary, new Color(16, 113, 0), 40);
        all.setFont(new Font("Lexend", Font.BOLD, 20));
        Dimension prefAll = all.getPreferredSize();
        all.setPreferredSize(new Dimension(Math.max(prefAll.width + 26, 120), 50));
        buttons.add(all);
        container.add(all);

        for (DashboardSportTypeOption opt : availableSportTypes) {
            RoundedButton b = new RoundedButton(opt.sportTypeName(), grayUI, darkText, 40);
            b.putClientProperty("sportTypeId", opt.sportTypeId());
            b.setFont(new Font("Lexend", Font.BOLD, 20));
            Dimension pref = b.getPreferredSize();
            b.setPreferredSize(new Dimension(Math.max(pref.width + 26, 120), 50));
            buttons.add(b);
            container.add(b);
        }

        for (int i = 0; i < buttons.size(); i++) {
            int idx = i;
            buttons.get(i).addActionListener(e -> {
                for (int j = 0; j < buttons.size(); j++) {
                    boolean selected = (j == idx);
                    RoundedButton btn = buttons.get(j);
                    btn.setBg(selected ? greenPrimary : grayUI);
                    btn.setForeground(selected ? greenTextDark : darkText);
                }

                if (idx == 0) {
                    loadCardsAsync(() -> controller.search(null));
                } else {
                    Object sportTypeId = buttons.get(idx).getClientProperty("sportTypeId");
                    loadCardsAsync(() -> controller.filterBySportType(String.valueOf(sportTypeId)));
                }
            });
        }
    }

    private JPanel createDiscoveryHeader() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
        p.setBackground(bgColor);
        JLabel t = new JLabel("Khám phá");
        t.setFont(new Font("Lexend", Font.BOLD, 32));
        JLabel st = new JLabel("Các sân thể thao theo ý muốn của bạn!");
        st.setFont(new Font("Lexend", Font.PLAIN, 16));
        st.setForeground(new Color(100, 100, 100));
        p.add(t);
        p.add(st);
        return p;
    }

    private ResponsiveCardsGrid createCardsGrid(int initialCols) {
        ResponsiveCardsGrid p = new ResponsiveCardsGrid(initialCols, 25, 25, bgColor);
        return p;
    }

    private void triggerSearch() {
        String keyword = searchInput == null ? null : searchInput.getText();
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty() || SEARCH_PLACEHOLDER.equals(keyword)) {
                keyword = null;
            }
        }
        String finalKeyword = keyword;
        loadCardsAsync(() -> controller.search(finalKeyword));
    }

    private void loadCardsAsync(java.util.concurrent.Callable<List<DashboardCourtCard>> task) {
        SwingWorker<List<DashboardCourtCard>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DashboardCourtCard> doInBackground() {
                try {
                    return task.call();
                } catch (Exception e) {
                    return java.util.Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    renderCards(get());
                } catch (Exception e) {
                    renderCards(java.util.Collections.emptyList());
                }
            }
        };
        worker.execute();
    }

    private void renderCards(List<DashboardCourtCard> cards) {
        if (cardsGrid == null) {
            return;
        }
        cardsGrid.removeAll();
        for (DashboardCourtCard card : cards) {
            cardsGrid.add(new StadiumCard(card));
        }
        cardsGrid.revalidate();
        cardsGrid.repaint();
    }

    private static void installMouseWheelScroll(Component root, JScrollBar vbar) {
        MouseWheelListener l = e -> {
            int rotation = e.getWheelRotation();
            if (rotation == 0) return;

            int delta;
            if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
                delta = rotation * Math.max(vbar.getBlockIncrement(rotation), vbar.getVisibleAmount());
            } else {
                delta = e.getUnitsToScroll() * vbar.getUnitIncrement(rotation);
            }

            int min = vbar.getMinimum();
            int max = vbar.getMaximum() - vbar.getVisibleAmount();
            vbar.setValue(Math.max(min, Math.min(max, vbar.getValue() + delta)));
            e.consume();
        };
        installMouseWheelScrollRecursive(root, l);
    }

    private static void installMouseWheelScrollRecursive(Component c, MouseWheelListener l) {
        c.addMouseWheelListener(l);
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                installMouseWheelScrollRecursive(child, l);
            }
        }
    }

    static class ViewportWidthPanel extends JPanel implements Scrollable {
        ViewportWidthPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - 20, 20);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // Grid that adapts column count based on viewport width (avoid fixed 6 columns overflow).
    static class ResponsiveCardsGrid extends JPanel {
        private final int hgap;
        private final int vgap;
        private final int minCardWidth;
        private int cols;

        ResponsiveCardsGrid(int initialCols, int hgap, int vgap, Color bg) {
            this.hgap = hgap;
            this.vgap = vgap;
            this.cols = Math.max(1, initialCols);
            this.minCardWidth = 260;
            setBackground(bg);
            setLayout(new GridLayout(0, this.cols, this.hgap, this.vgap));
        }

        void updateColumns(int viewportWidth) {
            int targetCols = Math.max(1, viewportWidth / (minCardWidth + hgap));
            if (targetCols != cols) {
                cols = targetCols;
                setLayout(new GridLayout(0, cols, hgap, vgap));
                revalidate();
                repaint();
            }
        }
    }

    class StadiumCard extends JPanel {
        public StadiumCard(DashboardCourtCard data) {
            setLayout(new BorderLayout());
            setOpaque(false);
            JPanel cardInner = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                    g2.dispose();
                }
            };
            cardInner.setOpaque(false);

            RoundedPanel imgArea = new RoundedPanel(40, CARD_IMAGE_PATH);
            imgArea.setPreferredSize(new Dimension(0, 200));
            cardInner.add(imgArea, BorderLayout.NORTH);

            JPanel info = new JPanel(new GridBagLayout());
            info.setPreferredSize(new Dimension(0, 160));
            info.setBackground(Color.WHITE);
            info.setOpaque(false);

            // Compact card info area (avoid excessive height).
            info.setBorder(new EmptyBorder(7, 15, 7, 15));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;

// --- PHẦN NAME ---
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 5, 0); // Khoảng cách nhỏ bên dưới
            JLabel name = new JLabel(safeText(data == null ? null : data.courtName()));
            name.setFont(new Font("Lexend", Font.BOLD, 19));
            info.add(name, gbc);

// --- PHẦN LOCATION ---
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 10, 0); // Khoảng cách dưới địa chỉ
            JLabel loc = new JLabel(safeText(data == null ? null : data.branchAddress()));
            loc.setForeground(Color.GRAY);
            loc.setFont(new Font("Lexend", Font.PLAIN, 14));
            info.add(loc, gbc);

// --- PHẦN PRICE ---
            gbc.gridy = 2;
            gbc.insets = new Insets(5, 0, 15, 0); // Reset và set lề mới cho price
            JLabel price = new JLabel(formatPrice(data == null ? null : data.price()));
            price.setFont(new Font("Lexend", Font.BOLD, 16));
            info.add(price, gbc);

// --- PHẦN BUTTON ---
            gbc.gridy = 3;
            gbc.anchor = GridBagConstraints.CENTER; // Reset về giữa
            gbc.insets = new Insets(0, 0, 5, 0);
            RoundedButton btnDetail = new RoundedButton("Chi tiết", grayUI, darkText, 30);
            btnDetail.setFont(new Font("Lexend", Font.BOLD, 14));
// Thay vì (0, 45), hãy dùng chiều rộng tối thiểu hoặc để Layout tự tính
            btnDetail.setPreferredSize(new Dimension(150, 45));
            info.add(btnDetail, gbc);

            cardInner.add(info, BorderLayout.CENTER);
            add(cardInner);
        }
    }

    private static String safeText(String v) {
        return v == null ? "" : v;
    }

    private static String formatPrice(BigDecimal price) {
        if (price == null) {
            return "--";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf.format(price) + "VNĐ/1 giờ";
    }

    class RoundedButton extends JButton {
        private Color bg;
        private int radius;
        public RoundedButton(String text, Color bg, Color fg, int radius) {
            super(text);
            this.bg = bg;
            this.radius = radius;
            setForeground(fg);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }

        public void setBg(Color bg) {
            this.bg = bg;
            repaint();
        }
    }

    class RoundedPanel extends JPanel {
        private int radius;
        private Image img;
        public RoundedPanel(int radius, String imgPath) {
            this.radius = radius;
            setOpaque(false);
            try {
                img = ImageIO.read(new File(imgPath));
            } catch (Exception e) {}
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setClip(clip);
            if (img != null) {
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            } else {
                g2.setColor(new Color(200, 200, 200));
                g2.fill(clip);
            }
            g2.dispose();
        }
    }

    class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(200, 200, 200);
            this.trackColor = new Color(0, 0, 0, 0);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        private JButton createZeroButton() {
            JButton jb = new JButton();
            jb.setPreferredSize(new Dimension(0, 0));
            return jb;
        }
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Hide track background/edge (keep UI clean; only show the thumb).
        }
    }

    // Main được sửa đổi để hiển thị JPanel TestDashboard
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Rentsta Dashboard Container");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//            // Cách gọi TestDashboard::new
//            TestDashboard dashboard = new TestDashboard();
//
//            frame.getContentPane().add(dashboard);
//            frame.setSize(1200, 800);
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//        });
//    }
}
