package com.sportcourt.modules.customer_history.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;
import com.sportcourt.modules.customer_history.controller.BookingHistoryController;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;
import com.sportcourt.modules.customer_history.dto.ServiceDetailDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

public class BookingDetailPanel extends JPanel {

    private static final Color PAGE_BG = new Color(248, 249, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color SUMMARY_CARD_BG = new Color(238, 238, 238);
    private static final Color TEXT_DARK = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color LIME_DARK = new Color(34, 139, 34);
    private static final Color SEPARATOR_COLOR = new Color(220, 220, 220);
    private static final Color STATUS_CANCEL_FG = CrudViewStyle.DANGER_TEXT;

    private final BookingHistoryController controller;
    private final Runnable onBack;

    private final JPanel mainArea = new JPanel(new GridBagLayout());
    private final JLabel statusLabel = new JLabel("Đang tải...");
    private Image bannerImage;

    public BookingDetailPanel(BookingHistoryController controller, Runnable onBack) {
        this.controller = controller;
        this.onBack = onBack;

        AppFonts.register();
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(
                UIScale.scale(20), UIScale.scale(24),
                UIScale.scale(24), UIScale.scale(24)));

        loadBannerImage();
        add(buildTopBar(), BorderLayout.NORTH);

        mainArea.setOpaque(false);
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setOpaque(false);
        scrollWrapper.add(mainArea, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(scrollWrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private static Image loadSportImage(String sportTypeName) {
        if (sportTypeName == null) return null;
        String lower = sportTypeName.toLowerCase().trim();
        String file;
        if (lower.contains("pickleball")) file = "/image/pickleball.png";
        else if (lower.contains("bóng bàn") || lower.contains("bong ban")) file = "/image/bongban.png";
        else if (lower.contains("cầu lông") || lower.contains("cau long")) file = "/image/caulong.png";
        else if (lower.contains("tennis")) file = "/image/tennis.jpg";
        else if (lower.contains("bóng đá") || lower.contains("bong da")) file = "/image/court.png";
        else file = "/image/court.png";
        URL url = BookingDetailPanel.class.getResource(file);
        return url != null ? new ImageIcon(url).getImage() : null;
    }

    private static JPanel makeSeparator() {
        JPanel line = new JPanel();
        line.setBackground(SEPARATOR_COLOR);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(100, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private static JPanel makeDot(Color color) {
        int sz = 10;
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, (getHeight() - sz) / 2, sz, sz);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(sz + 2, sz + 2));
        dot.setMinimumSize(new Dimension(sz + 2, sz + 2));
        dot.setMaximumSize(new Dimension(sz + 2, sz + 2));
        return dot;
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

    private static String safeStr(String s) {
        return (s == null || s.isBlank()) ? "--" : s;
    }

    public void loadDetail(String invoiceId) {
        renderLoading();

        SwingWorker<BookingDetailDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingDetailDTO doInBackground() {
                return controller.loadDetail(invoiceId);
            }

            @Override
            protected void done() {
                try {
                    renderDetail(get());
                } catch (Exception ex) {
                    renderError(ex);
                }
            }
        };
        worker.execute();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, UIScale.scale(12), 0));

        JLabel backLbl = new JLabel("< QUAY LẠI");
        backLbl.setFont(AppFonts.lexendBold(14f));
        backLbl.setForeground(LIME_DARK);
        backLbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLbl.setToolTipText("Quay lại danh sách");
        backLbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onBack.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                backLbl.setForeground(new Color(25, 110, 25));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backLbl.setForeground(LIME_DARK);
            }
        });

        bar.add(backLbl, BorderLayout.WEST);
        bar.add(statusLabel, BorderLayout.EAST);
        statusLabel.setFont(AppFonts.lexendRegular(13f));
        statusLabel.setForeground(TEXT_MUTED);

        return bar;
    }

    private void renderLoading() {
        mainArea.removeAll();
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.weighty = 1;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;

        JLabel loading = new JLabel("Đang tải chi tiết hóa đơn...");
        loading.setFont(AppFonts.lexendRegular(15f));
        loading.setForeground(TEXT_MUTED);
        mainArea.add(loading, g);
        mainArea.revalidate();
        mainArea.repaint();
        statusLabel.setText("");
    }

    private void renderError(Exception ex) {
        mainArea.removeAll();
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = 1;
        g.weighty = 1;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;

        JLabel err = new JLabel("Lỗi: " + ex.getMessage());
        err.setFont(AppFonts.lexendRegular(13f));
        err.setForeground(STATUS_CANCEL_FG);
        mainArea.add(err, g);
        mainArea.revalidate();
        mainArea.repaint();
        statusLabel.setText("Lỗi tải dữ liệu");
    }

    private void renderDetail(BookingDetailDTO detail) {
        mainArea.removeAll();

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; // Đã sửa lại thành HORIZONTAL để không dãn vỡ layout
        g.anchor = GridBagConstraints.NORTH;

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.weightx = 1.0;
        g.weighty = 0;
        g.insets = new Insets(0, 0, UIScale.scale(40), 0);
        mainArea.add(buildBanner(detail), g);

        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 1;
        g.weightx = 0.65; // Đã tinh chỉnh lại tỷ lệ cho khung bên trái to hơn chút
        g.insets = new Insets(0, 0, 0, UIScale.scale(16));
        mainArea.add(buildBookingInfoCard(detail), g);

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);
        rightCol.add(buildSummaryCard(detail));

        g.gridx = 1;
        g.weightx = 0.35; // Tỷ lệ khung bên phải
        g.insets = new Insets(0, 0, 0, 0);
        mainArea.add(rightCol, g);

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        g.weighty = 1.0;
        mainArea.add(new JLabel(""), g);

        mainArea.revalidate();
        mainArea.repaint();

        statusLabel.setText("Mã đơn: " + detail.getInvoiceId());
    }

    private void loadBannerImage() {
        String[] names = {"/image/court2.png", "/image/court2.jpg",
                "/image/court.png", "/image/court.jpg"};
        for (String name : names) {
            URL url = BookingDetailPanel.class.getResource(name);
            if (url != null) {
                bannerImage = new ImageIcon(url).getImage();
                break;
            }
        }
    }

    private JPanel buildBanner(BookingDetailDTO detail) {
        String sportName = null;
        if (detail != null && detail.getCourtItems() != null && !detail.getCourtItems().isEmpty()) {
            sportName = detail.getCourtItems().get(0).getSportTypeName();
        }
        Image sportImg = loadSportImage(sportName);
        final Image displayImg = (sportImg != null) ? sportImg : bannerImage;

        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int arc = UIScale.scale(80);
                int h = getHeight();
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), h, arc, arc));
                if (displayImg != null) {
                    g2.drawImage(displayImg, 0, 0, getWidth(), h, null);
                } else {
                    g2.setColor(new Color(65, 82, 60));
                    g2.fillRect(0, 0, getWidth(), h);
                }
                int overlay = UIScale.scale(70);
                g2.setColor(new Color(15, 25, 15, 220));
                g2.fillRect(0, h - overlay, getWidth(), overlay);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        int bh = UIScale.scale(250);
        banner.setPreferredSize(new Dimension(100, bh));
        banner.setMinimumSize(new Dimension(100, bh));

        JLabel title = new JLabel("CHI TIẾT LỊCH SỬ ĐẶT CHỖ", SwingConstants.CENTER);
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(Color.WHITE);
        title.setPreferredSize(new Dimension(0, UIScale.scale(60)));
        banner.add(title, BorderLayout.SOUTH);
        return banner;
    }

    private JPanel buildBookingInfoCard(BookingDetailDTO detail) {
        RoundedPanel card = new RoundedPanel(UIScale.scale(40), CARD_BG, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(UIScale.scale(24), UIScale.scale(24),
                UIScale.scale(24), UIScale.scale(24)));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(30)));
        JPanel sectionTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(6), 0));
        sectionTitleRow.setOpaque(false);
        sectionTitleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel greenDot1 = makeDot(LIME_DARK);
        JLabel sectionTitle = new JLabel("THÔNG TIN ĐẶT SÂN");
        sectionTitle.setFont(AppFonts.lexendBold(13f));
        sectionTitle.setForeground(TEXT_DARK);
        sectionTitleRow.add(greenDot1);
        sectionTitleRow.add(sectionTitle);
        headerRow.add(sectionTitleRow, BorderLayout.WEST);

        card.add(headerRow);
        card.add(Box.createVerticalStrut(UIScale.scale(12)));

        card.add(makeFieldRow("Khách hàng", safeStr(detail.getCustomerName())));
        card.add(Box.createVerticalStrut(UIScale.scale(6)));
        card.add(makeFieldRow("Số điện thoại", safeStr(detail.getCustomerPhone())));
        card.add(Box.createVerticalStrut(UIScale.scale(6)));
        card.add(makeFieldRow("Mã đơn", safeStr(detail.getInvoiceId())));
        card.add(Box.createVerticalStrut(UIScale.scale(10)));
        card.add(makeSeparator());
        card.add(Box.createVerticalStrut(UIScale.scale(10)));

        card.add(makeFieldRow("Chi nhánh", safeStr(detail.getBranchName())));
        card.add(Box.createVerticalStrut(UIScale.scale(6)));
        card.add(makeFieldRow("Địa chỉ", safeStr(detail.getBranchAddress())));
        card.add(Box.createVerticalStrut(UIScale.scale(10)));

        List<BookingDetailDTO.CourtLineItem> items = detail.getCourtItems();
        if (items != null && !items.isEmpty()) {
            for (BookingDetailDTO.CourtLineItem item : items) {
                card.add(makeCourtItem(item));
                card.add(Box.createVerticalStrut(UIScale.scale(6)));
            }
        } else {
            JLabel noItems = new JLabel("Không có chi tiết sân.");
            noItems.setFont(AppFonts.lexendRegular(13f));
            noItems.setForeground(TEXT_MUTED);
            noItems.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(noItems);
        }

        card.add(Box.createVerticalStrut(UIScale.scale(10)));
        card.add(makeSeparator());
        card.add(Box.createVerticalStrut(UIScale.scale(10)));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(30)));
        JLabel lblTotal = new JLabel("Tổng tiền thuê sân");
        lblTotal.setFont(AppFonts.lexendRegular(13f));
        lblTotal.setForeground(TEXT_MUTED);
        JLabel valTotal = new JLabel(detail.getFormattedTotalValue());
        valTotal.setFont(AppFonts.lexendBold(17f));
        valTotal.setForeground(TEXT_DARK);
        totalRow.add(lblTotal, BorderLayout.WEST);
        totalRow.add(valTotal, BorderLayout.EAST);
        card.add(totalRow);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel makeCourtItem(BookingDetailDTO.CourtLineItem item) {
        RoundedPanel p = new RoundedPanel(UIScale.scale(30), new Color(245, 247, 245), false);
        p.setLayout(new BorderLayout(UIScale.scale(12), 0));
        p.setBorder(new EmptyBorder(UIScale.scale(8), UIScale.scale(12),
                UIScale.scale(8), UIScale.scale(12)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(40)));
        p.setMinimumSize(new Dimension(0, UIScale.scale(40)));

        String rawStatus = item.getStatus();
        String st = (rawStatus != null && !rawStatus.isBlank()) ? rawStatus : "—";

        Color stBg = new Color(225, 245, 228);
        Color stFg = new Color(34, 139, 34);
        if (st.toUpperCase().contains("HỦY") || st.toUpperCase().contains("HUỶ")) {
            stBg = new Color(253, 236, 234);
            stFg = new Color(211, 47, 47);
        } else if (st.toUpperCase().contains("CHỜ") || st.toUpperCase().contains("CHƯA")) {
            stBg = new Color(255, 244, 229);
            stFg = new Color(230, 81, 0);
        }

        JPanel leftRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(8), 0));
        leftRow.setOpaque(false);

        // THÊM CHỮ "Mã sân: " Ở ĐÂY
        JLabel idLbl = new JLabel("Mã sân: " + (item.getCourtId() != null ? item.getCourtId() : "--"));
        idLbl.setFont(AppFonts.lexendBold(13f));
        idLbl.setForeground(TEXT_DARK);

        RoundedPanel statusPill = new RoundedPanel(UIScale.scale(50), stBg, false);
        statusPill.setLayout(new BorderLayout());
        statusPill.setBorder(new EmptyBorder(UIScale.scale(4), UIScale.scale(8),
                UIScale.scale(4), UIScale.scale(8)));
        JLabel stLbl = new JLabel(st);
        stLbl.setFont(AppFonts.lexendBold(10f));
        stLbl.setForeground(stFg);
        statusPill.add(stLbl, BorderLayout.CENTER);

        leftRow.add(idLbl);
        leftRow.add(statusPill);
        p.add(leftRow, BorderLayout.WEST);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, UIScale.scale(12), 0));
        rightSide.setOpaque(false);

        String timeLabel = item.getTimeSlot() + "  |  " + item.getFormattedCourtDate();
        JLabel timeLbl = new JLabel(timeLabel);
        timeLbl.setFont(AppFonts.lexendBold(12f));
        timeLbl.setForeground(LIME_DARK);

        String formattedPrice = String.format("%,.0f\u0111/gi\u1EDD", item.getUnitPrice()).replace(",", ".");
        JLabel priceLbl = new JLabel(formattedPrice);
        priceLbl.setFont(AppFonts.lexendBold(12f));
        priceLbl.setForeground(LIME_DARK);

        rightSide.add(timeLbl);
        rightSide.add(priceLbl);
        p.add(rightSide, BorderLayout.CENTER);
        return p;
    }

    private JPanel makeServiceItem(ServiceDetailDTO item) {
        RoundedPanel p = new RoundedPanel(UIScale.scale(30), new Color(248, 249, 250), false);
        p.setLayout(new BorderLayout(UIScale.scale(12), 0));
        p.setBorder(new EmptyBorder(UIScale.scale(8), UIScale.scale(12),
                UIScale.scale(8), UIScale.scale(12)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(36)));

        JLabel nameLbl = new JLabel(item.getServiceName() + " (x" + item.getQuantity() + ")");
        nameLbl.setFont(AppFonts.lexendRegular(13f));
        nameLbl.setForeground(TEXT_DARK);
        p.add(nameLbl, BorderLayout.WEST);

        String formattedPrice = String.format("%,.0f\u0111", item.getPrice()).replace(",", ".");
        JLabel priceLbl = new JLabel(formattedPrice);
        priceLbl.setFont(AppFonts.lexendBold(13f));
        priceLbl.setForeground(TEXT_DARK);
        p.add(priceLbl, BorderLayout.EAST);

        return p;
    }

    private JPanel buildSummaryCard(BookingDetailDTO detail) {
        RoundedPanel card = new RoundedPanel(UIScale.scale(40), SUMMARY_CARD_BG, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(UIScale.scale(20), UIScale.scale(24),
                UIScale.scale(20), UIScale.scale(24)));

        JLabel title = new JLabel("CHI TIẾT THANH TOÁN");
        title.setFont(AppFonts.lexendBold(14f));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(UIScale.scale(8)));

        // --- BẮT ĐẦU LOGIC GỘP TRẠNG THÁI CHUẨN ---
        String overallStatus = detail.getOverallStatus();
        // --- KẾT THÚC LOGIC ---

        card.add(createStatusBadge(overallStatus));
        card.add(Box.createVerticalStrut(UIScale.scale(16)));

        String rentStr = detail.getTotalValue() != null ? String.format("%,.0f\u0111", detail.getTotalValue()).replace(",", ".") : "0\u0111";
        card.add(makeSummaryRow("Tiền thuê sân", rentStr, false));

        if (detail.getDeposit() != null && detail.getDeposit().compareTo(java.math.BigDecimal.ZERO) > 0) {
            card.add(Box.createVerticalStrut(UIScale.scale(8)));
            String depositStr = String.format("%,.0f\u0111", detail.getDeposit()).replace(",", ".");
            card.add(makeSummaryRow("Tiền cọc", depositStr, false));
        }

        if (detail.getDiscount() != null && detail.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            card.add(Box.createVerticalStrut(UIScale.scale(8)));
            card.add(makeSummaryRow("Giảm giá", detail.getDiscount().toPlainString() + "%", false));
        }

        card.add(Box.createVerticalStrut(UIScale.scale(12)));
        card.add(makeSeparator());
        card.add(Box.createVerticalStrut(UIScale.scale(12)));

        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setAlignmentX(Component.LEFT_ALIGNMENT);
        bot.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(32)));

        JLabel botLbl = new JLabel("TỔNG CỘNG");
        botLbl.setFont(AppFonts.lexendBold(14f));
        botLbl.setForeground(TEXT_DARK);

        String totalStr = detail.getTotalAmount() != null ? String.format("%,.0f\u0111", detail.getTotalAmount()).replace(",", ".") : "0\u0111";
        JLabel botVal = new JLabel(totalStr);
        botVal.setFont(AppFonts.lexendBold(18f));
        botVal.setForeground(new Color(34, 139, 34));

        bot.add(botLbl, BorderLayout.WEST);
        bot.add(botVal, BorderLayout.EAST);
        card.add(bot);

        card.add(Box.createVerticalStrut(UIScale.scale(24)));

        // --- HIỂN THỊ NÚT BẤM ---
        String upperStatus = overallStatus != null ? overallStatus.toUpperCase() : "";
        if (upperStatus.contains("CHỜ CỌC") || upperStatus.contains("CHƯA")) {
            JButton btnPay = createActionButton("THANH TOÁN CỌC", new Color(57, 255, 20), new Color(0, 100, 0));
            btnPay.addActionListener(e -> handlePayDeposit(detail.getInvoiceId()));
            card.add(btnPay);
        } else if (upperStatus.contains("XÁC NHẬN") || upperStatus.contains("ĐÃ CỌC")) {
            JButton btnCancel = createActionButton("HỦY ĐẶT SÂN", new Color(255, 77, 77), Color.WHITE);
            btnCancel.addActionListener(e -> handleCancelBooking(detail)); // TRUYỀN NGUYÊN ĐỐI TƯỢNG DETAIL
            card.add(btnCancel);
        }

        return card;
    }

    private JPanel createStatusBadge(String status) {
        String st = status != null ? status : "";
        String upperSt = st.toUpperCase();

        Color bg = new Color(220, 220, 220);
        Color fg = Color.DARK_GRAY;
        String displayStatus = st;

        if (upperSt.contains("CHỜ CỌC") || upperSt.contains("CHƯA")) {
            bg = new Color(255, 77, 77);
            fg = Color.WHITE;
            displayStatus = "Đã đặt chờ cọc";
        } else if (upperSt.contains("XÁC NHẬN") && upperSt.contains("CHỜ")) {
            bg = new Color(204, 255, 204);
            fg = new Color(0, 100, 0);
            displayStatus = "Chờ xác nhận";
        } else if (upperSt.contains("XÁC NHẬN") || upperSt.contains("ĐÃ CỌC")) {
            bg = new Color(102, 255, 102);
            fg = new Color(0, 100, 0);
            displayStatus = "Đã xác nhận";
        } else if (upperSt.contains("HUỶ") || upperSt.contains("HỦY")) {
            bg = new Color(230, 230, 230);
            fg = Color.DARK_GRAY;
            displayStatus = "Đã hủy";
        }

        RoundedPanel badge = new RoundedPanel(UIScale.scale(50), bg, false);
        badge.setLayout(new BorderLayout());
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(45)));
        badge.setPreferredSize(new Dimension(10, UIScale.scale(45)));

        JLabel lbl = new JLabel(displayStatus, SwingConstants.CENTER);
        lbl.setFont(AppFonts.lexendBold(14f));
        lbl.setForeground(fg);
        badge.add(lbl, BorderLayout.CENTER);

        return badge;
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(AppFonts.lexendBold(14f));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(45)));
        btn.setPreferredSize(new Dimension(10, UIScale.scale(45)));
        return btn;
    }

    private JPanel makeSummaryRow(String label, String value, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(24)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppFonts.lexendRegular(12f));
        lbl.setForeground(TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(bold ? AppFonts.lexendBold(12f) : AppFonts.lexendRegular(12f));
        val.setForeground(TEXT_DARK);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    private void handleCancelBooking(BookingDetailDTO detail) {
        if (detail == null || detail.getCourtItems() == null || detail.getCourtItems().isEmpty()) return;

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        int confirm = JOptionPane.showConfirmDialog(parentWindow,
                "Bạn có chắc chắn muốn hủy TOÀN BỘ sân trong hóa đơn này?\n(Quy định hoàn cọc sẽ được áp dụng nếu hủy trước 2 ngày)",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Lặp qua tất cả các sân trong hóa đơn và gọi lệnh hủy
                    for (BookingDetailDTO.CourtLineItem item : detail.getCourtItems()) {
                        if (item.canBeCancelled()) {
                            controller.cancelCourtBooking(item.getBookingDetailId());
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        JOptionPane.showMessageDialog(parentWindow,
                                "Hủy đặt sân thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        onBack.run();
                    } catch (Exception ex) {
                        String errorMsg = ex.getMessage();
                        if (errorMsg.contains("ORA-")) {
                            errorMsg = errorMsg.split("ORA-\\d+:")[1].split("\n")[0].trim();
                        }
                        JOptionPane.showMessageDialog(parentWindow,
                                errorMsg, "Không thể hủy sân", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void handlePayDeposit(String invoiceId) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JOptionPane.showMessageDialog(parentWindow,
                "Chức năng thanh toán cọc đang được kết nối với cổng thanh toán cho mã đơn: " + invoiceId,
                "Đang phát triển", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color background;
        private final boolean shadow;

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
            int sh = UIScale.scale(6);
            int h = shadow ? getHeight() - sh : getHeight();
            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(0, sh / 2, getWidth(), h, radius, radius);
            }
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), h, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}