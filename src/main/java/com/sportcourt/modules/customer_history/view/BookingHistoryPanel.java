package com.sportcourt.modules.customer_history.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;
import com.sportcourt.modules.customer_history.controller.BookingHistoryController;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.auth.dto.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class BookingHistoryPanel extends JPanel {
    private static final String LIST_VIEW   = "LIST_VIEW";
    private static final String DETAIL_VIEW = "DETAIL_VIEW";

    private static final Color PAGE_BG           = new Color(248, 249, 250);
    private static final Color CARD_BG           = Color.WHITE;
    private static final Color TEXT_DARK         = new Color(30, 31, 36);
    private static final Color TEXT_MUTED        = new Color(103, 112, 133);
    private static final Color LIME_DARK         = new Color(34, 139, 34);
    private static final Color BANNER_BOTTOM_BG  = new Color(65, 82, 60);
    private static final Color DARK_BUTTON_BG    = new Color(50, 50, 50);
    private static final Color SEPARATOR_COLOR   = new Color(220, 220, 220);
    private static final Color TEXT_LIGHT_BUTTON = Color.WHITE;

    private static final Color EDIT_BG       = CrudViewStyle.EDIT_BG;
    private static final Color EDIT_TEXT     = CrudViewStyle.EDIT_TEXT;
    private static final Color CREATE_BG     = CrudViewStyle.SUCCESS_BG;
    private static final Color CREATE_TEXT   = CrudViewStyle.SUCCESS_TEXT;
    private static final Color SOFT_RED_BG   = CrudViewStyle.DANGER_BG;
    private static final Color SOFT_RED_TEXT = CrudViewStyle.DANGER_TEXT;

    private final CardLayout cardLayout   = new CardLayout();
    private final JPanel     contentPanel = new JPanel(cardLayout);

    private final BookingHistoryController controller = new BookingHistoryController();
    private final HistoryListPanel   listPanel;
    private final BookingDetailPanel detailPanel;

    private Image bannerImage;

    public BookingHistoryPanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);
        loadBannerImage();

        listPanel   = new HistoryListPanel(this::showDetailView);
        detailPanel = new BookingDetailPanel(controller, this::showListView);

        contentPanel.setOpaque(false);
        contentPanel.add(listPanel,   LIST_VIEW);
        contentPanel.add(detailPanel, DETAIL_VIEW);

        add(contentPanel, BorderLayout.CENTER);
        showListView();
        CrudViewStyle.installResponsiveTypography(this);
    }

    private void loadBannerImage() {
        String[] names = {"/image/court2.png", "/image/court2.jpg", "/image/court2.jpeg",
                "/image/court.png", "/image/court.jpg"};
        for (String name : names) {
            URL url = BookingHistoryPanel.BookingHistoryPanel.class.getResource(name);
            if (url != null) {
                bannerImage = new ImageIcon(url).getImage();
                break;
            }
        }
    }

    private void showListView()   { 
        listPanel.loadData();
        cardLayout.show(contentPanel, LIST_VIEW);   
    }
    private void showDetailView(String invoiceId) { 
        detailPanel.loadDetail(invoiceId);
        cardLayout.show(contentPanel, DETAIL_VIEW); 
    }

    private static JPanel makeSeparator() {
        JPanel line = new JPanel();
        line.setBackground(SEPARATOR_COLOR);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(1)));
        line.setPreferredSize(new Dimension(100, UIScale.scale(1)));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private JComponent createImageLabel(String fileName, int width, int height) {
        URL imgUrl = BookingHistoryPanel.BookingHistoryPanel.class.getResource("/image/" + fileName);
        if (imgUrl != null) {
            Image scaled = new ImageIcon(imgUrl).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new JPanel() {
                { setOpaque(false); }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(
                            0, 0, width, height, UIScale.scale(14), UIScale.scale(14)));
                    g2.drawImage(scaled, 0, 0, width, height, null);
                    g2.dispose();
                }
            };
        }
        return new RoundedPanel(UIScale.scale(14), new Color(200, 210, 220));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LIST PANEL
    // ════════════════════════════════════════════════════════════════════════
    private class HistoryListPanel extends JPanel {
        private final java.util.function.Consumer<String> onDetailRequested;
        private final JPanel listContainer;

        HistoryListPanel(java.util.function.Consumer<String> onDetailRequested) {
            this.onDetailRequested = onDetailRequested;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(UIScale.scale(30), UIScale.scale(50),
                    UIScale.scale(30), UIScale.scale(50)));
            add(createHeader(),      BorderLayout.NORTH);
            
            listContainer = new JPanel();
            listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
            listContainer.setOpaque(false);
            listContainer.setBorder(new EmptyBorder(UIScale.scale(20), 0, 0, 0));

            JScrollPane sp = new JScrollPane(listContainer);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.setBorder(null);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            
            add(sp, BorderLayout.CENTER);
        }

        public void loadData() {
            listContainer.removeAll();
            
            String customerId = "KH-1";
            try {
                java.lang.reflect.Field field = SessionManager.class.getDeclaredField("session");
                field.setAccessible(true);
                UserSession session = (UserSession) field.get(null);
                if (session != null && session.getCustomerId() != null) {
                    customerId = session.getCustomerId();
                }
            } catch (Exception e) {
                // fallback
            }
            
            List<BookingHistoryItemDTO> items = controller.loadHistory(customerId, "");
            
            if (items == null || items.isEmpty()) {
                JLabel emptyLbl = new JLabel("Không có dữ liệu lịch sử đặt sân.");
                emptyLbl.setFont(AppFonts.lexendRegular(14f));
                emptyLbl.setForeground(TEXT_MUTED);
                listContainer.add(emptyLbl);
            } else {
                for (BookingHistoryItemDTO item : items) {
                    listContainer.add(card(item));
                    listContainer.add(Box.createVerticalStrut(UIScale.scale(16)));
                }
            }
            
            listContainer.revalidate();
            listContainer.repaint();
        }

        private JPanel createHeader() {
            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setOpaque(false);

            JLabel logo = new JLabel("RENTSTA");
            logo.setFont(AppFonts.lexendBold(30f));
            logo.setForeground(new Color(30, 31, 36));
            logo.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(logo);
            header.add(Box.createVerticalStrut(UIScale.scale(10)));
            header.add(makeSeparator());
            header.add(Box.createVerticalStrut(UIScale.scale(30)));

            JLabel title = new JLabel("Lịch sử đặt chỗ");
            title.setFont(AppFonts.lexendBold(30f));
            title.setForeground(TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(title);

            JLabel sub = new JLabel("Xem lại lịch sử đặt sân và chi tiết các buổi tập tại các cơ sở thể thao hàng đầu.");
            sub.setFont(AppFonts.lexendRegular(14f));
            sub.setForeground(TEXT_MUTED);
            sub.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(sub);
            header.add(Box.createVerticalStrut(UIScale.scale(30)));

            JPanel toolbar = new JPanel(new BorderLayout());
            toolbar.setOpaque(false);
            toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
            toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(45)));

            JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(12), 0));
            filters.setOpaque(false);
            filters.add(createPillButton("Lọc theo danh mục", Color.WHITE, TEXT_DARK, true));
            filters.add(createPillButton("3 tháng qua", Color.WHITE, TEXT_DARK, true));

            JLabel sortLbl = new JLabel("Sắp xếp: Mới nhất");
            sortLbl.setFont(AppFonts.lexendBold(14f));
            sortLbl.setForeground(LIME_DARK);

            toolbar.add(filters,  BorderLayout.WEST);
            toolbar.add(sortLbl,  BorderLayout.EAST);
            header.add(toolbar);
            return header;
        }

        private JPanel card(BookingHistoryItemDTO item) {
            String title = item.getBranchName();
            if (title == null) title = "Hóa đơn " + item.getInvoiceId();
            String loc = item.getBranchAddress() != null ? "📍 " + item.getBranchAddress() : "";
            String date = "🗓 " + item.getFormattedBookingDate();
            String price = item.getFormattedTotalAmount();
            
            String img = "court.png";
            if (item.getSportTypeName() != null) {
                String typeLower = item.getSportTypeName().toLowerCase();
                if (typeLower.contains("tennis")) img = "tennis.jpg";
                else if (typeLower.contains("cầu lông")) img = "badminton.png";
            }

            RoundedPanel card = new RoundedPanel(UIScale.scale(28), CARD_BG, true);
            card.setLayout(new BorderLayout(UIScale.scale(22), 0));
            card.setBorder(new EmptyBorder(UIScale.scale(20), UIScale.scale(20),
                    UIScale.scale(20), UIScale.scale(28)));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMinimumSize(new Dimension(UIScale.scale(400), UIScale.scale(120)));

            int imgW = UIScale.scale(140);
            int imgH = UIScale.scale(90);
            JComponent imgComp = createImageLabel(img, imgW, imgH);
            imgComp.setPreferredSize(new Dimension(imgW, imgH));
            imgComp.setMinimumSize(new Dimension(imgW, imgH));
            card.add(imgComp, BorderLayout.WEST);

            JPanel mid = new JPanel();
            mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
            mid.setOpaque(false);
            mid.setBorder(new EmptyBorder(UIScale.scale(8), 0, UIScale.scale(8), 0));

            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(10), 0));
            tagRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(26)));
            tagRow.setOpaque(false);
            tagRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            Color statusBg = new Color(230, 244, 234); // Light green background
            Color statusFg = new Color(34, 139, 34); // Dark green text
            String st = item.getStatus() != null ? item.getStatus().toUpperCase() : "ĐÃ HOÀN THÀNH";
            if (st.contains("HỦY")) {
                statusBg = SOFT_RED_BG;
                statusFg = SOFT_RED_TEXT;
            } else if (st.contains("CHỜ") || st.contains("CHƯA")) {
                statusBg = EDIT_BG;
                statusFg = EDIT_TEXT;
            }
            tagRow.add(CrudViewStyle.createStatusPill(st, statusBg, statusFg));
            
            JLabel dateLbl = new JLabel(date);
            dateLbl.setFont(AppFonts.lexendRegular(12f));
            dateLbl.setForeground(TEXT_MUTED);
            tagRow.add(dateLbl);

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(AppFonts.lexendBold(18f));
            titleLbl.setForeground(TEXT_DARK);
            titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel locLbl = new JLabel(loc);
            locLbl.setFont(AppFonts.lexendRegular(13f));
            locLbl.setForeground(TEXT_MUTED);
            locLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            mid.add(tagRow);
            mid.add(Box.createVerticalStrut(UIScale.scale(2)));
            mid.add(titleLbl);
            mid.add(Box.createVerticalStrut(UIScale.scale(3)));
            mid.add(locLbl);
            card.add(mid, BorderLayout.CENTER);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);

            JLabel prc = new JLabel(price);
            prc.setFont(AppFonts.lexendBold(18f));
            prc.setForeground(TEXT_DARK);
            prc.setAlignmentX(Component.RIGHT_ALIGNMENT);

            JButton btn = createPillButton("Xem chi tiết", new Color(235, 235, 235), TEXT_DARK, true);
            btn.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btn.addActionListener(e -> onDetailRequested.accept(item.getInvoiceId()));

            right.add(Box.createVerticalGlue());
            right.add(prc);
            right.add(Box.createVerticalStrut(UIScale.scale(10)));
            right.add(btn);
            right.add(Box.createVerticalGlue());
            card.add(right, BorderLayout.EAST);

            return card;
        }
    }

    private JButton createPillButton(String text, Color bg, Color fg, boolean bold) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                if (bg == Color.WHITE) {
                    g2.setColor(SEPARATOR_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(bold ? AppFonts.lexendBold(13f) : AppFonts.lexendRegular(13f));
        btn.setForeground(fg);
        btn.setBorder(new EmptyBorder(UIScale.scale(8), UIScale.scale(20),
                UIScale.scale(8), UIScale.scale(20)));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class RoundedPanel extends JPanel {
        private final int     radius;
        private final Color   background;
        protected final boolean shadow;

        RoundedPanel(int radius, Color background) {
            this(radius, background, false);
        }

        RoundedPanel(int radius, Color background, boolean shadow) {
            this.radius     = radius;
            this.background = background;
            this.shadow     = shadow;
            setOpaque(false);
            if (shadow) setBorder(new EmptyBorder(0, 0, UIScale.scale(8), 0));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowOffset = UIScale.scale(8);
            int h = shadow ? getHeight() - shadowOffset : getHeight();

            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(0, shadowOffset / 2, getWidth(), h, radius, radius);
                g2.setColor(new Color(0, 0, 0, 5));
                g2.fillRoundRect(0, shadowOffset, getWidth(), h, radius, radius);
            }

            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), h, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}