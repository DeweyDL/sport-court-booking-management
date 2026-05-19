package com.sportcourt.modules.customer_history.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;
import com.sportcourt.modules.auth.dto.UserSession;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.customer_history.controller.BookingHistoryController;
import com.sportcourt.modules.customer_history.dto.BookingHistoryItemDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BookingHistoryPanel extends JPanel {
    private static final String LIST_VIEW = "LIST_VIEW";
    private static final String DETAIL_VIEW = "DETAIL_VIEW";

    private static final Color PAGE_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color LIME_DARK = new Color(34, 139, 34);
    private static final Color BANNER_BOTTOM_BG = new Color(65, 82, 60);
    private static final Color DARK_BUTTON_BG = new Color(50, 50, 50);
    private static final Color SEPARATOR_COLOR = new Color(220, 220, 220);
    private static final Color TEXT_LIGHT_BUTTON = Color.WHITE;
    private static final int PILL_RADIUS = 25;

    private static final Color EDIT_BG = CrudViewStyle.EDIT_BG;
    private static final Color EDIT_TEXT = CrudViewStyle.EDIT_TEXT;
    private static final Color CREATE_BG = CrudViewStyle.SUCCESS_BG;
    private static final Color CREATE_TEXT = CrudViewStyle.SUCCESS_TEXT;
    private static final Color SOFT_RED_BG = CrudViewStyle.DANGER_BG;
    private static final Color SOFT_RED_TEXT = CrudViewStyle.DANGER_TEXT;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final BookingHistoryController controller = new BookingHistoryController();
    private final HistoryListPanel listPanel;
    private final BookingDetailPanel detailPanel;

    private Image bannerImage;

    public BookingHistoryPanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);
        loadBannerImage();

        listPanel = new HistoryListPanel(this::showDetailView);
        detailPanel = new BookingDetailPanel(controller, this::showListView);

        contentPanel.setOpaque(false);
        contentPanel.add(listPanel, LIST_VIEW);
        contentPanel.add(detailPanel, DETAIL_VIEW);

        add(contentPanel, BorderLayout.CENTER);
        showListView();
        CrudViewStyle.installResponsiveTypography(this);
    }

    private static JPanel makeSeparator() {
        JPanel line = new JPanel();
        line.setBackground(SEPARATOR_COLOR);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(1)));
        line.setPreferredSize(new Dimension(100, UIScale.scale(1)));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private static String sportTypeToImageFile(String sportTypeName) {
        if (sportTypeName == null) return "court.png";
        String lower = sportTypeName.toLowerCase().trim();
        if (lower.contains("pickleball")) return "pickleball.png";
        if (lower.contains("bóng bàn") || lower.contains("bong ban")) return "bongban.png";
        if (lower.contains("cầu lông") || lower.contains("cau long")) return "caulong.png";
        if (lower.contains("tennis")) return "tennis.jpg";
        if (lower.contains("bóng đá") || lower.contains("bong da")) return "court.png";
        return "court.png";
    }

    private static JLabel createCourtImage(String sportTypeName, int width, int height) {
        String fileName = sportTypeToImageFile(sportTypeName);
        String[] candidates = {"/image/" + fileName, "/image/court.png", "/image/court.jpg"};
        Image finalImage = null;
        for (String path : candidates) {
            URL url = BookingHistoryPanel.class.getResource(path);
            if (url != null) {
                finalImage = new ImageIcon(url).getImage();
                break;
            }
        }
        final Image img = finalImage;
        int arc = UIScale.scale(60);

        return new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, arc, arc));
                if (img != null) {
                    g2.drawImage(img, 0, 0, w, h, null);
                } else {
                    g2.setColor(new Color(180, 195, 210));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                }
                g2.dispose();
            }
        };
    }

    private static JLabel makeIconLabel(String iconPath, String text, float fontSize, Color textColor) {
        JLabel label = new JLabel(text == null ? "" : text);
        label.setFont(AppFonts.lexendRegular(fontSize));
        label.setForeground(textColor);
        URL iconUrl = BookingHistoryPanel.class.getResource(iconPath);
        if (iconUrl != null) {
            int sz = Math.round(fontSize * 1.1f);
            Image img = new ImageIcon(iconUrl).getImage().getScaledInstance(sz, sz, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setIconTextGap(UIScale.scale(5));
        }
        return label;
    }

    private void loadBannerImage() {
        String[] names = {
                "/image/court2.png", "/image/court2.jpg", "/image/court2.jpeg",
                "/image/court.png", "/image/court.jpg"
        };
        for (String name : names) {
            URL url = BookingHistoryPanel.class.getResource(name);
            if (url != null) {
                bannerImage = new ImageIcon(url).getImage();
                break;
            }
        }
    }

    private void showListView() {
        listPanel.loadData();
        cardLayout.show(contentPanel, LIST_VIEW);
    }

    private void showDetailView(String invoiceId) {
        detailPanel.loadDetail(invoiceId);
        cardLayout.show(contentPanel, DETAIL_VIEW);
    }

    private JButton createPillButton(String text, Color bg, Color fg, boolean bold) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                if (bg == Color.WHITE) {
                    g2.setColor(new Color(203, 213, 225));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(bold ? AppFonts.lexendBold(13f) : AppFonts.lexendRegular(13f));
        btn.setForeground(fg);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static final class RoundedLineBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int arc;

        RoundedLineBorder(Color color, int arc) {
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

    private static class RoundedPanel extends JPanel {
        protected final boolean shadow;
        private final int radius;
        private final Color background;

        RoundedPanel(int radius, Color background) {
            this(radius, background, false);
        }

        RoundedPanel(int radius, Color background, boolean shadow) {
            this.radius = radius;
            this.background = background;
            this.shadow = shadow;
            setOpaque(false);
            if (shadow) setBorder(new EmptyBorder(0, 0, UIScale.scale(6), 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int shadowOffset = UIScale.scale(6);
            int h = shadow ? getHeight() - shadowOffset : getHeight();

            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(0, shadowOffset / 2, getWidth(), h, radius, radius);
            }

            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), h, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class HistoryListPanel extends JPanel {
        private final Consumer<String> onDetailRequested;
        private final JPanel listContainer;
        private final JTextField txtSearch = new JTextField();
        private final JPanel searchWrapper = new JPanel(new BorderLayout());
        private final Timer searchDebounceTimer;
        private List<BookingHistoryItemDTO> allItems = new ArrayList<>();

        HistoryListPanel(Consumer<String> onDetailRequested) {
            this.onDetailRequested = onDetailRequested;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(UIScale.scale(30), UIScale.scale(50), UIScale.scale(30), UIScale.scale(50)));

            searchDebounceTimer = new Timer(300, e -> applyFilterAndRender());
            searchDebounceTimer.setRepeats(false);

            add(createHeader(), BorderLayout.NORTH);

            listContainer = new JPanel();
            listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
            listContainer.setOpaque(false);
            listContainer.setBorder(new EmptyBorder(UIScale.scale(20), 0, UIScale.scale(30), 0));

            JPanel scrollWrapper = new JPanel(new BorderLayout());
            scrollWrapper.setOpaque(false);
            scrollWrapper.add(listContainer, BorderLayout.NORTH);

            JScrollPane sp = new JScrollPane(scrollWrapper);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.setBorder(null);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            sp.getViewport().addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    listContainer.revalidate();
                    listContainer.repaint();
                }
            });

            add(sp, BorderLayout.CENTER);
        }

        public void loadData() {
            String customerId = SessionManager.getCurrentSession()
                    .map(UserSession::getCustomerId)
                    .orElse(null);
            if (customerId == null || customerId.isBlank()) {
                allItems = new ArrayList<>();
                applyFilterAndRender();
                return;
            }
            final String cid = customerId;
            SwingWorker<List<BookingHistoryItemDTO>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<BookingHistoryItemDTO> doInBackground() {
                    return controller.loadHistory(cid, null);
                }

                @Override
                protected void done() {
                    try {
                        allItems = get() == null ? new ArrayList<>() : get();
                    } catch (Exception ex) {
                        allItems = new ArrayList<>();
                    }
                    applyFilterAndRender();
                }
            };
            worker.execute();
        }

        private void applyFilterAndRender() {
            String kw = txtSearch.getText();
            List<BookingHistoryItemDTO> filtered = new ArrayList<>();
            for (BookingHistoryItemDTO i : allItems) {
                if (i.matchSearchKeyword(kw)) {
                    filtered.add(i);
                }
            }
            renderList(filtered);
        }

        private void renderList(List<BookingHistoryItemDTO> items) {
            listContainer.removeAll();
            if (items == null || items.isEmpty()) {
                JLabel emptyLbl = new JLabel("Không tìm thấy dữ liệu lịch sử đặt sân.");
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
            toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(42)));

            txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo mã đơn, loại thể thao, chi nhánh...");
            txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    searchDebounceTimer.restart();
                }

                public void removeUpdate(DocumentEvent e) {
                    searchDebounceTimer.restart();
                }

                public void changedUpdate(DocumentEvent e) {
                    searchDebounceTimer.restart();
                }
            });
            JPanel searchField = CrudViewStyle.createSearchFieldWithIcon(searchWrapper, txtSearch, loadSearchIcon());

            JPanel rightBar = new JPanel();
            rightBar.setLayout(new BoxLayout(rightBar, BoxLayout.X_AXIS));
            rightBar.setOpaque(false);
            rightBar.add(searchField);

            toolbar.add(rightBar, BorderLayout.EAST);
            header.add(toolbar);
            return header;
        }

        private Icon loadSearchIcon() {
            URL iconUrl = getClass().getResource("/icon/search.png");
            if (iconUrl == null) return UIManager.getIcon("FileView.fileIcon");
            Image img = new ImageIcon(iconUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }

        private JPanel card(BookingHistoryItemDTO item) {
            String sportName = item.getSportTypeName();
            String title = (sportName != null && !sportName.isBlank()) ? sportName : "Hóa đơn " + item.getInvoiceId();

            String locText = item.getBranchAddress() != null ? item.getBranchAddress() : "Chưa cập nhật địa chỉ";
            String dateText = item.getFormattedBookingDate();
            String price = item.getFormattedTotalAmount();

            RoundedPanel card = new RoundedPanel(UIScale.scale(80), CARD_BG, true);
            card.setLayout(new BorderLayout(UIScale.scale(22), 0));
            card.setBorder(new EmptyBorder(UIScale.scale(20), UIScale.scale(20), UIScale.scale(20), UIScale.scale(28)));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(140)));

            int imgW = UIScale.scale(140);
            int imgH = UIScale.scale(90);
            JLabel imgComp = createCourtImage(item.getSportTypeName(), imgW, imgH);
            imgComp.setPreferredSize(new Dimension(imgW, imgH));
            imgComp.setMinimumSize(new Dimension(imgW, imgH));
            imgComp.setMaximumSize(new Dimension(imgW, imgH));
            imgComp.setSize(imgW, imgH);
            card.add(imgComp, BorderLayout.WEST);

            JPanel mid = new JPanel();
            mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
            mid.setOpaque(false);
            mid.setBorder(new EmptyBorder(UIScale.scale(8), 0, UIScale.scale(8), 0));

            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(10), 0));
            tagRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(26)));
            tagRow.setOpaque(false);
            tagRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            String rawStatus = item.getStatus();
            String st = (rawStatus != null && !rawStatus.isBlank()) ? rawStatus.toUpperCase() : "TRỐNG";

            Color statusBg = new Color(225, 245, 228);
            Color statusFg = new Color(34, 139, 34);
            if (st.contains("HỦY") || st.contains("HUỶ")) {
                statusBg = new Color(253, 236, 234);
                statusFg = new Color(211, 47, 47);
            } else if (st.contains("CHỜ") || st.contains("CHƯA") || st.equals("TRỐNG")) {
                statusBg = new Color(255, 244, 229);
                statusFg = new Color(230, 81, 0);
            } else if (st.equals("ĐÃ CỌC")) {
                statusBg = new Color(254, 243, 199);
                statusFg = new Color(146, 64, 14);
            }

            String displayStatus = resolveDisplayStatus(rawStatus);

            RoundedPanel statusPill = new RoundedPanel(UIScale.scale(12), statusBg, false);
            statusPill.setLayout(new BorderLayout());
            statusPill.setBorder(new EmptyBorder(UIScale.scale(4), UIScale.scale(8), UIScale.scale(4), UIScale.scale(8)));
            JLabel stLbl = new JLabel(displayStatus);
            stLbl.setFont(AppFonts.lexendBold(10f));
            stLbl.setForeground(statusFg);
            statusPill.add(stLbl, BorderLayout.CENTER);
            tagRow.add(statusPill);

            JLabel dateLbl = makeIconLabel("/icon/calendar.png", dateText, 14f, TEXT_MUTED);
            tagRow.add(dateLbl);

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(AppFonts.lexendBold(18f));
            titleLbl.setForeground(TEXT_DARK);
            titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel locLbl = makeIconLabel("/icon/address.png", locText, 13f, TEXT_MUTED);
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

            JButton btn = createPillButton("Xem chi tiết", new Color(226, 232, 240), TEXT_DARK, true);
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

        private String resolveDisplayStatus(String rawStatus) {
            if (rawStatus == null) return "TRỐNG";
            return switch (rawStatus.toUpperCase()) {
                case "ĐÃ ĐẶT CHỜ CỌC" -> "Chờ cọc";
                case "ĐÃ CỌC" -> "Đã cọc, chờ xác nhận";
                case "ĐÃ XÁC NHẬN" -> "Đã xác nhận";
                case "ĐANG SỬ DỤNG" -> "Đang sử dụng";
                case "ĐÃ HOÀN THÀNH" -> "Đã hoàn thành";
                case "ĐÃ HUỶ", "ĐÃ HỦY" -> "Đã hủy";
                default -> rawStatus;
            };
        }
    }
}