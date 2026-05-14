package com.sportcourt.modules.customer_history.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class BookingHistoryPanel extends JPanel {
    private static final String LIST_VIEW   = "LIST_VIEW";
    private static final String DETAIL_VIEW = "DETAIL_VIEW";

    private static final Color CARD_BG           = Color.WHITE;
    private static final Color TEXT_DARK         = new Color(30, 31, 36);
    private static final Color TEXT_MUTED        = new Color(103, 112, 133);
    private static final Color LIME_DARK         = new Color(34, 139, 34);
    private static final Color BANNER_BOTTOM_BG  = new Color(65, 82, 60);
    private static final Color DARK_BUTTON_BG    = new Color(50, 50, 50);
    private static final Color SEPARATOR_COLOR   = new Color(220, 220, 220);

    private static final Color EDIT_BG       = CrudViewStyle.EDIT_BG;
    private static final Color EDIT_TEXT     = CrudViewStyle.EDIT_TEXT;
    private static final Color SOFT_RED_BG   = CrudViewStyle.DANGER_BG;
    private static final Color SOFT_RED_TEXT = CrudViewStyle.DANGER_TEXT;

    private final CardLayout cardLayout   = new CardLayout();
    private final JPanel     contentPanel = new JPanel(cardLayout);

    private final HistoryListPanel   listPanel;
    private final HistoryDetailPanel detailPanel;

    private Image bannerImage;

    // Dummy data class
    static class MockBooking {
        String title, loc, date, price, img, status, orderId;
        MockBooking(String title, String loc, String date, String price, String img, String status, String orderId) {
            this.title = title; this.loc = loc; this.date = date; this.price = price; this.img = img; this.status = status; this.orderId = orderId;
        }
    }

    private final List<MockBooking> allBookings = List.of(
            new MockBooking("Sân Tennis - Quận 1", "12 Nguyễn Huệ, Quận 1, TP.HCM", "24/10/2026", "400.000 VNĐ", "tennis.jpg", "ĐÃ HOÀN THÀNH", "HD001"),
            new MockBooking("Sân Cầu Lông - Quận 3", "45 Võ Văn Tần, Quận 3, TP.HCM", "18/10/2026", "150.000 VNĐ", "badminton.png", "ĐÃ HỦY", "HD002"),
            new MockBooking("Sân Bóng Đá - Bình Thạnh", "88 Điện Biên Phủ, Bình Thạnh, TP.HCM", "12/10/2026", "1.200.000 VNĐ", "court.png", "CHỜ XÁC NHẬN", "HD003")
    );

    public BookingHistoryPanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        CrudViewStyle.applyPageDefaults(this);
        loadBannerImage();

        listPanel   = new HistoryListPanel(this::showDetailView);
        detailPanel = new HistoryDetailPanel(this::showListView);

        contentPanel.setOpaque(false);
        contentPanel.add(listPanel,   LIST_VIEW);
        contentPanel.add(detailPanel, DETAIL_VIEW);

        add(contentPanel, BorderLayout.CENTER);
        showListView();
        CrudViewStyle.installResponsiveTypography(this);
    }

    private void loadBannerImage() {
        String[] names = {"/image/court2.png", "/image/court2.jpg", "/image/court.png"};
        for (String name : names) {
            URL url = BookingHistoryPanel.class.getResource(name);
            if (url != null) {
                bannerImage = new ImageIcon(url).getImage();
                break;
            }
        }
    }

    private void showListView()   { cardLayout.show(contentPanel, LIST_VIEW);   }
    private void showDetailView(MockBooking b) { 
        detailPanel.loadData(b);
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

    private static JPanel makeFieldRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(22)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppFonts.lexendRegular(12f));
        lbl.setForeground(TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(AppFonts.lexendBold(12f));
        val.setForeground(TEXT_DARK);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private JComponent createImageLabel(String fileName, int width, int height) {
        URL imgUrl = BookingHistoryPanel.class.getResource("/image/" + fileName);
        if (imgUrl != null) {
            Image scaled = new ImageIcon(imgUrl).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new JPanel() {
                { setOpaque(false); }
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, UIScale.scale(14), UIScale.scale(14)));
                    g2.drawImage(scaled, 0, 0, width, height, null);
                    g2.dispose();
                }
            };
        }
        return new RoundedPanel(UIScale.scale(14), new Color(200, 210, 220));
    }

    private Icon loadSearchIcon() {
        URL iconUrl = getClass().getResource("/icon/search.png");
        if (iconUrl == null) return UIManager.getIcon("FileView.fileIcon");
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LIST PANEL
    // ════════════════════════════════════════════════════════════════════════
    private class HistoryListPanel extends JPanel {
        private final java.util.function.Consumer<MockBooking> onDetailRequested;
        private final JPanel listContainer = new JPanel();
        private final JTextField txtSearch = new JTextField();
        private final JPanel searchWrapper = new JPanel(new BorderLayout());
        private final Timer searchDebounceTimer;

        HistoryListPanel(java.util.function.Consumer<MockBooking> onDetailRequested) {
            this.onDetailRequested = onDetailRequested;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(UIScale.scale(30), UIScale.scale(50), UIScale.scale(30), UIScale.scale(50)));
            
            searchDebounceTimer = new Timer(300, e -> filterList());
            searchDebounceTimer.setRepeats(false);
            
            txtSearch.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
                public void removeUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
                public void changedUpdate(DocumentEvent e) { searchDebounceTimer.restart(); }
            });

            add(createHeader(), BorderLayout.NORTH);
            
            listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
            listContainer.setOpaque(false);
            listContainer.setBorder(new EmptyBorder(UIScale.scale(20), 0, 0, 0));
            
            JScrollPane sp = new JScrollPane(listContainer);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            sp.setBorder(null);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            add(sp, BorderLayout.CENTER);
            
            filterList();
        }

        private void filterList() {
            String query = txtSearch.getText().trim().toLowerCase();
            List<MockBooking> filtered = allBookings.stream()
                .filter(b -> b.title.toLowerCase().contains(query) || b.loc.toLowerCase().contains(query))
                .collect(Collectors.toList());
                
            listContainer.removeAll();
            if (filtered.isEmpty()) {
                JLabel empty = new JLabel("Không tìm thấy kết quả.");
                empty.setFont(AppFonts.lexendRegular(14f));
                empty.setForeground(TEXT_MUTED);
                listContainer.add(empty);
            } else {
                for (MockBooking b : filtered) {
                    listContainer.add(card(b));
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

            JLabel title = new JLabel("LỊCH SỬ ĐẶT CHỖ");
            title.setFont(new Font("Lexend", Font.BOLD, 30));
            title.setForeground(TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(title);

            JLabel sub = new JLabel("Xem lại lịch sử đặt sân và chi tiết các buổi tập tại các cơ sở thể thao hàng đầu.");
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
            
            txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm sân hoặc địa chỉ...");
            JPanel searchPanel = CrudViewStyle.createSearchFieldWithIcon(searchWrapper, txtSearch, loadSearchIcon());
            
            toolbar.add(filters, BorderLayout.WEST);
            toolbar.add(searchPanel, BorderLayout.EAST);
            header.add(toolbar);
            return header;
        }

        private JPanel card(MockBooking item) {
            RoundedPanel card = new RoundedPanel(UIScale.scale(28), CARD_BG, true);
            card.setLayout(new BorderLayout(UIScale.scale(22), 0));
            card.setBorder(new EmptyBorder(UIScale.scale(20), UIScale.scale(20), UIScale.scale(20), UIScale.scale(28)));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMinimumSize(new Dimension(UIScale.scale(400), UIScale.scale(120)));

            int imgW = UIScale.scale(140);
            int imgH = UIScale.scale(90);
            JComponent imgComp = createImageLabel(item.img, imgW, imgH);
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
            
            Color statusBg = new Color(230, 244, 234);
            Color statusFg = new Color(34, 139, 34);
            if (item.status.contains("HỦY")) { statusBg = SOFT_RED_BG; statusFg = SOFT_RED_TEXT; }
            else if (item.status.contains("CHỜ")) { statusBg = EDIT_BG; statusFg = EDIT_TEXT; }
            tagRow.add(CrudViewStyle.createStatusPill(item.status, statusBg, statusFg));
            
            JLabel dateLbl = new JLabel("\uD83D\uDDD3 " + item.date);
            dateLbl.setFont(AppFonts.lexendRegular(12f));
            dateLbl.setForeground(TEXT_MUTED);
            tagRow.add(dateLbl);

            JLabel titleLbl = new JLabel(item.title);
            titleLbl.setFont(AppFonts.lexendBold(18f));
            titleLbl.setForeground(TEXT_DARK);
            titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel locLbl = new JLabel("\uD83D\uDCCD " + item.loc);
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

            JLabel prc = new JLabel(item.price);
            prc.setFont(AppFonts.lexendBold(18f));
            prc.setForeground(TEXT_DARK);
            prc.setAlignmentX(Component.RIGHT_ALIGNMENT);

            JButton btn = createPillButton("Xem chi tiết", new Color(235, 235, 235), TEXT_DARK, true);
            btn.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btn.addActionListener(e -> onDetailRequested.accept(item));

            right.add(Box.createVerticalGlue());
            right.add(prc);
            right.add(Box.createVerticalStrut(UIScale.scale(10)));
            right.add(btn);
            right.add(Box.createVerticalGlue());
            card.add(right, BorderLayout.EAST);

            return card;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DETAIL PANEL
    // ════════════════════════════════════════════════════════════════════════
    private class HistoryDetailPanel extends JPanel {
        private final Runnable onBack;
        private final JPanel infoCardPanel = new JPanel();
        private MockBooking currentData;

        HistoryDetailPanel(Runnable onBack) {
            this.onBack = onBack;
            setLayout(new BorderLayout(0, UIScale.scale(15)));
            setOpaque(false);
            setBorder(new EmptyBorder(UIScale.scale(20), UIScale.scale(24), UIScale.scale(24), UIScale.scale(24)));
            
            infoCardPanel.setLayout(new BoxLayout(infoCardPanel, BoxLayout.Y_AXIS));
            infoCardPanel.setOpaque(false);
        }
        
        public void loadData(MockBooking data) {
            this.currentData = data;
            removeAll();
            add(buildHeader(), BorderLayout.NORTH);
            add(buildMain(), BorderLayout.CENTER);
            revalidate();
            repaint();
        }

        private JPanel buildHeader() {
            JPanel h = new JPanel(new BorderLayout());
            h.setOpaque(false);

            JLabel back = new JLabel("\u2190 Quay lại danh sách");
            back.setFont(AppFonts.lexendBold(24f));
            back.setForeground(TEXT_DARK);
            back.setCursor(new Cursor(Cursor.HAND_CURSOR));
            back.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onBack.run(); }
                @Override public void mouseEntered(MouseEvent e) { back.setForeground(LIME_DARK); }
                @Override public void mouseExited(MouseEvent e)  { back.setForeground(TEXT_DARK); }
            });
            h.add(back, BorderLayout.WEST);

            JPanel bottomWrap = new JPanel(new BorderLayout());
            bottomWrap.setOpaque(false);
            bottomWrap.add(h, BorderLayout.CENTER);
            bottomWrap.add(Box.createVerticalStrut(UIScale.scale(16)), BorderLayout.SOUTH);
            return bottomWrap;
        }

        private JPanel buildMain() {
            JPanel main = new JPanel(new GridBagLayout());
            main.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();

            g.fill = GridBagConstraints.HORIZONTAL;
            g.anchor = GridBagConstraints.NORTH;

            g.gridx = 0; g.gridy = 0;
            g.gridwidth = 2;
            g.weightx = 1.0;
            g.weighty = 0;
            g.insets = new Insets(0, 0, UIScale.scale(20), 0);
            main.add(buildBanner(), g);

            g.gridy = 1;
            g.gridwidth = 1;
            g.weightx = 0.6;
            g.insets = new Insets(0, 0, 0, UIScale.scale(16));
            main.add(buildBookingInfo(), g);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);
            right.add(buildPaymentCard());
            right.add(Box.createVerticalStrut(UIScale.scale(12)));
            right.add(buildTotalCard());

            g.gridx = 1;
            g.weightx = 0.4;
            g.insets = new Insets(0, 0, 0, 0);
            main.add(right, g);

            g.gridx = 0; g.gridy = 2;
            g.gridwidth = 2;
            g.weighty = 1.0;
            main.add(new JLabel(""), g);

            return main;
        }

        private JPanel buildBanner() {
            RoundedPanel banner = new RoundedPanel(UIScale.scale(40), BANNER_BOTTOM_BG, true) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int h = shadow ? getHeight() - UIScale.scale(8) : getHeight();
                    int arc = UIScale.scale(40);
                    int overlayH = UIScale.scale(68);

                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), h, arc, arc));

                    if (bannerImage != null) {
                        g2.drawImage(bannerImage, 0, 0, getWidth(), h, null);
                    } else {
                        g2.setColor(new Color(60, 120, 40));
                        g2.fillRect(0, 0, getWidth(), h);
                    }

                    g2.setColor(new Color(30, 40, 28, 220));
                    g2.fillRect(0, h - overlayH, getWidth(), overlayH);
                    g2.dispose();
                }
            };

            banner.setLayout(new BorderLayout());
            int bannerHeight = UIScale.scale(250);
            banner.setPreferredSize(new Dimension(0, bannerHeight));
            banner.setMinimumSize(new Dimension(0, bannerHeight));

            JLabel lbl = new JLabel("CHI TIẾT LỊCH SỬ ĐẶT CHỖ", SwingConstants.CENTER);
            lbl.setFont(AppFonts.lexendBold(30f));
            lbl.setForeground(Color.WHITE);
            lbl.setPreferredSize(new Dimension(0, UIScale.scale(68)));
            banner.add(lbl, BorderLayout.SOUTH);
            return banner;
        }

        private JPanel buildBookingInfo() {
            RoundedPanel card = new RoundedPanel(UIScale.scale(40), CARD_BG, true);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(UIScale.scale(14), UIScale.scale(16), UIScale.scale(14), UIScale.scale(16)));

            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            head.setAlignmentX(Component.LEFT_ALIGNMENT);
            head.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(32)));

            JLabel title = new JLabel("\uD83D\uDFE9 THÔNG TIN ĐẶT SÂN");
            title.setFont(AppFonts.lexendBold(13f));
            title.setForeground(TEXT_DARK);

            JButton addBtn = createPillButton("Thêm sân", LIME_DARK, Color.WHITE, true);
            head.add(title,  BorderLayout.WEST);
            head.add(addBtn, BorderLayout.EAST);
            card.add(head);
            card.add(Box.createVerticalStrut(UIScale.scale(10)));

            card.add(makeFieldRow("Khách hàng", "Nguyễn Văn A"));
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            card.add(makeFieldRow("Số điện thoại", "0123456789"));
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            card.add(makeFieldRow("Mã đơn", currentData.orderId));
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            card.add(makeFieldRow("Ngày đặt", currentData.date));
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            card.add(makeFieldRow("Trạng thái", currentData.status));
            card.add(Box.createVerticalStrut(UIScale.scale(10)));

            card.add(makeSeparator());
            card.add(Box.createVerticalStrut(UIScale.scale(10)));

            card.add(makeFieldRow("Chi nhánh", currentData.title));
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            card.add(makeFieldRow("Địa chỉ", currentData.loc));
            card.add(Box.createVerticalStrut(UIScale.scale(10)));

            card.add(makeCourtItem("Mã sân 1", "18:00 - 20:00 | 29/12/2026", currentData.price));
            card.add(Box.createVerticalStrut(UIScale.scale(10)));

            card.add(makeSeparator());
            card.add(Box.createVerticalStrut(UIScale.scale(10)));

            JPanel total = new JPanel(new BorderLayout());
            total.setOpaque(false);
            total.setAlignmentX(Component.LEFT_ALIGNMENT);
            total.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(30)));
            JLabel lblTotal = new JLabel("Tổng tiền thuê sân");
            lblTotal.setFont(AppFonts.lexendRegular(12f));
            lblTotal.setForeground(TEXT_MUTED);
            JLabel valTotal = new JLabel(currentData.price);
            valTotal.setFont(AppFonts.lexendBold(17f));
            valTotal.setForeground(TEXT_DARK);
            total.add(lblTotal, BorderLayout.WEST);
            total.add(valTotal, BorderLayout.EAST);
            card.add(total);
            card.add(Box.createVerticalGlue());

            return card;
        }

        private JPanel makeCourtItem(String id, String time, String price) {
            RoundedPanel p = new RoundedPanel(UIScale.scale(10), new Color(245, 247, 245), false);
            p.setLayout(new BorderLayout(UIScale.scale(8), 0));
            p.setBorder(new EmptyBorder(UIScale.scale(7), UIScale.scale(12), UIScale.scale(7), UIScale.scale(12)));
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(36)));

            JLabel idLbl = new JLabel(id);
            idLbl.setFont(AppFonts.lexendRegular(12f));
            idLbl.setForeground(TEXT_MUTED);
            p.add(idLbl, BorderLayout.WEST);

            JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIScale.scale(10), 0));
            rightSide.setOpaque(false);

            JLabel timeLbl = new JLabel(time);
            timeLbl.setFont(AppFonts.lexendBold(12f));
            timeLbl.setForeground(LIME_DARK);

            JLabel priceLbl = new JLabel(price);
            priceLbl.setFont(AppFonts.lexendBold(12f));
            priceLbl.setForeground(LIME_DARK);

            rightSide.add(timeLbl);
            rightSide.add(priceLbl);
            p.add(rightSide, BorderLayout.CENTER);
            return p;
        }

        private JPanel buildPaymentCard() {
            RoundedPanel card = new RoundedPanel(UIScale.scale(32), DARK_BUTTON_BG, true);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(UIScale.scale(12), UIScale.scale(14), UIScale.scale(12), UIScale.scale(14)));

            JLabel t = new JLabel("\uD83D\uDFE9 THANH TOÁN");
            t.setFont(AppFonts.lexendBold(12f));
            t.setForeground(Color.WHITE);
            t.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(t);
            card.add(Box.createVerticalStrut(UIScale.scale(10)));
            card.add(makePayOpt("\uD83D\uDCB3", "Thẻ tín dụng", true));
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            card.add(makePayOpt("\uD83C\uDFE6", "Chuyển khoản", false));
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            card.add(makePayOpt("\uD83D\uDCF1", "Ví điện tử",   false));
            return card;
        }

        private JPanel makePayOpt(String icon, String text, boolean selected) {
            RoundedPanel p = new RoundedPanel(UIScale.scale(10), new Color(65, 67, 70));
            p.setLayout(new BorderLayout());
            p.setBorder(new EmptyBorder(UIScale.scale(8), UIScale.scale(12), UIScale.scale(8), UIScale.scale(12)));
            p.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(36)));

            JLabel lbl = new JLabel(icon + "   " + text);
            lbl.setFont(AppFonts.lexendRegular(12f));
            lbl.setForeground(Color.WHITE);
            p.add(lbl, BorderLayout.WEST);

            JLabel dot = new JLabel(selected ? "●" : "○");
            dot.setForeground(selected ? LIME_DARK : Color.GRAY);
            p.add(dot, BorderLayout.EAST);
            return p;
        }

        private JPanel buildTotalCard() {
            RoundedPanel card = new RoundedPanel(UIScale.scale(32), new Color(238, 238, 238), true);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(UIScale.scale(12), UIScale.scale(14), UIScale.scale(12), UIScale.scale(14)));

            JLabel title = new JLabel("CHI TIẾT THANH TOÁN");
            title.setFont(AppFonts.lexendBold(12f));
            title.setForeground(TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(title);
            card.add(Box.createVerticalStrut(UIScale.scale(10)));

            JPanel rent = new JPanel(new BorderLayout());
            rent.setOpaque(false);
            rent.setAlignmentX(Component.LEFT_ALIGNMENT);
            rent.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(22)));
            JLabel rentLbl = new JLabel("Tiền thuê sân");
            rentLbl.setFont(AppFonts.lexendRegular(12f));
            rentLbl.setForeground(TEXT_MUTED);
            JLabel rentVal = new JLabel(currentData.price);
            rentVal.setFont(AppFonts.lexendBold(12f));
            rentVal.setForeground(TEXT_DARK);
            rent.add(rentLbl, BorderLayout.WEST);
            rent.add(rentVal, BorderLayout.EAST);
            card.add(rent);

            card.add(Box.createVerticalStrut(UIScale.scale(10)));
            card.add(makeSeparator());
            card.add(Box.createVerticalStrut(UIScale.scale(10)));

            JPanel bot = new JPanel(new BorderLayout());
            bot.setOpaque(false);
            bot.setAlignmentX(Component.LEFT_ALIGNMENT);
            bot.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(30)));
            JLabel botLbl = new JLabel("TỔNG CỘNG");
            botLbl.setFont(AppFonts.lexendBold(13f));
            botLbl.setForeground(TEXT_DARK);
            JLabel botVal = new JLabel(currentData.price);
            botVal.setFont(AppFonts.lexendBold(18f));
            botVal.setForeground(LIME_DARK);
            bot.add(botLbl, BorderLayout.WEST);
            bot.add(botVal, BorderLayout.EAST);
            card.add(bot);

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
        btn.setBorder(new EmptyBorder(UIScale.scale(8), UIScale.scale(20), UIScale.scale(8), UIScale.scale(20)));
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