package com.sportcourt.modules.customer_history.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;
import com.sportcourt.modules.customer_history.controller.BookingHistoryController;
import com.sportcourt.modules.customer_history.dto.BookingDetailDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.List;

/**
 * Panel chi tiết một hóa đơn đặt sân.
 * Được nhúng vào CardLayout của BookingHistoryPanel.
 */
public class BookingDetailPanel extends JPanel {

    // ── Màu ─────────────────────────────────────────────────────────────
    private static final Color PAGE_BG = new Color(248, 249, 252); // khớp Account DIALOG_BG
    private static final Color CARD_BG = Color.WHITE;
    private static final Color DARK_CARD_BG = new Color(50, 50, 50);
    private static final Color SUMMARY_CARD_BG = new Color(238, 238, 238);
    private static final Color BANNER_OVERLAY = new Color(65, 82, 60);
    private static final Color TEXT_DARK = new Color(30, 41, 59);   // khớp Account TEXT_DARK
    private static final Color TEXT_MUTED = new Color(100, 116, 139); // khớp Account TEXT_MUTED
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color LIME_DARK = new Color(34, 139, 34);
    private static final Color SEPARATOR_COLOR = new Color(220, 220, 220);
    private static final Color INPUT_BORDER = new Color(203, 213, 225); // khớp Account RoundedLineBorder

    private static final Color STATUS_DONE_BG = CrudViewStyle.SUCCESS_BG;
    private static final Color STATUS_DONE_FG = CrudViewStyle.SUCCESS_TEXT;
    private static final Color STATUS_PEND_BG = CrudViewStyle.EDIT_BG;
    private static final Color STATUS_PEND_FG = CrudViewStyle.EDIT_TEXT;
    private static final Color STATUS_CANCEL_BG = CrudViewStyle.DANGER_BG;
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

    /**
     * Load ảnh theo tên loại thể thao (giống mapping trong BookingHistoryPanel)
     */
    private static Image loadSportImage(String sportTypeName) {
        if (sportTypeName == null) return null;
        String lower = sportTypeName.toLowerCase().trim();
        String file;
        if (lower.contains("pickleball")) file = "/image/pickleball.png";
        else if (lower.contains("b\u00f3ng b\u00e0n") || lower.contains("bong ban")) file = "/image/bongban.png";
        else if (lower.contains("c\u1ea7u l\u00f4ng") || lower.contains("cau long")) file = "/image/caulong.png";
        else if (lower.contains("tennis")) file = "/image/tennis.jpg";
        else if (lower.contains("b\u00f3ng \u0111\u00e1") || lower.contains("bong da")) file = "/image/court.png";
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

    /**
     * Vẽ chấm tròn màu nhỏ dùng cho header section
     */
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

        JLabel backLbl = new JLabel("\u2190");
        backLbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
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
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTH;

        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.weightx = 1.0;
        g.weighty = 0;
        g.insets = new Insets(0, 0, UIScale.scale(16), 0);
        mainArea.add(buildBanner(detail), g);

        g.gridx = 0;
        g.gridy = 1;
        g.gridwidth = 1;
        g.weightx = 0.60;
        g.insets = new Insets(0, 0, 0, UIScale.scale(16));
        mainArea.add(buildBookingInfoCard(detail), g);

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);
        rightCol.add(buildPaymentCard());
        rightCol.add(Box.createVerticalStrut(UIScale.scale(12)));
        rightCol.add(buildSummaryCard(detail));

        g.gridx = 1;
        g.weightx = 0.40;
        g.insets = new Insets(0, 0, 0, 0);
        mainArea.add(rightCol, g);

        // ── Spacer cuối ──────────────────────────────────────────────────
        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 2;
        g.weighty = 1.0;
        mainArea.add(new JLabel(""), g);

        mainArea.revalidate();
        mainArea.repaint();

        // Cập nhật status label ở topbar
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
        // Chọn ảnh theo loại thể thao — lấy từ item đầu tiên trong danh sách sân
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
                int arc = UIScale.scale(28);
                int h = getHeight();
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), h, arc, arc));
                if (displayImg != null) {
                    g2.drawImage(displayImg, 0, 0, getWidth(), h, null);
                } else {
                    g2.setColor(new Color(65, 82, 60));
                    g2.fillRect(0, 0, getWidth(), h);
                }
                // Overlay gradient tối phía dưới
                int overlay = UIScale.scale(70);
                g2.setColor(new Color(15, 25, 15, 220));
                g2.fillRect(0, h - overlay, getWidth(), overlay);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        int bh = UIScale.scale(200);
        banner.setPreferredSize(new Dimension(0, bh));
        banner.setMinimumSize(new Dimension(0, bh));

        JLabel title = new JLabel("CHI TIẾT LỊCH SỬ ĐẶT CHỖ", SwingConstants.CENTER);
        title.setFont(AppFonts.lexendBold(24f));
        title.setForeground(Color.WHITE);
        title.setPreferredSize(new Dimension(0, UIScale.scale(60)));
        banner.add(title, BorderLayout.SOUTH);
        return banner;
    }

    // ====================================================================
    //  Booking info card (left column)
    // ====================================================================
    private JPanel buildBookingInfoCard(BookingDetailDTO detail) {
        RoundedPanel card = new RoundedPanel(UIScale.scale(28), CARD_BG, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(UIScale.scale(16), UIScale.scale(18),
                UIScale.scale(16), UIScale.scale(18)));

        // --- Header ---
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

        JButton btnAdd = new JButton("Thêm sân") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(LIME_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnAdd.setFont(AppFonts.lexendBold(13f)); // khớp Account button font
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBorder(new EmptyBorder(10, 18, 10, 18)); // khớp Account button padding
        btnAdd.setContentAreaFilled(false);
        btnAdd.setBorderPainted(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                List<String> courts;
                List<com.sportcourt.modules.customer_history.dto.PriceBoardOptionDTO> priceBoards;

                @Override
                protected Void doInBackground() {
                    courts = controller.loadAvailableCourts();
                    priceBoards = controller.loadAvailablePriceBoards();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        com.sportcourt.modules.customer_history.dto.BookingAddCourtRequest req =
                                BookingAddCourtDialog.show(BookingDetailPanel.this, detail.getInvoiceId(), courts, priceBoards);
                        if (req != null) {
                            controller.addCourtBooking(req);
                            JOptionPane.showMessageDialog(BookingDetailPanel.this, "Đã thêm sân thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            loadDetail(detail.getInvoiceId()); // refresh
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(BookingDetailPanel.this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });
        headerRow.add(btnAdd, BorderLayout.EAST);

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
        RoundedPanel p = new RoundedPanel(UIScale.scale(10), new Color(245, 247, 245), false);
        p.setLayout(new BorderLayout(UIScale.scale(12), 0));
        p.setBorder(new EmptyBorder(UIScale.scale(8), UIScale.scale(12),
                UIScale.scale(8), UIScale.scale(12)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(40)));
        p.setMinimumSize(new Dimension(0, UIScale.scale(40)));

        // ── Bên trái: [Mã sân]  [Status badge] cùng hàng ─────────────────
        // Trạng thái từ DB (CT.TRANGTHAI)
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

        JLabel idLbl = new JLabel(item.getCourtId() != null ? item.getCourtId() : "--");
        idLbl.setFont(AppFonts.lexendBold(13f));
        idLbl.setForeground(TEXT_DARK);

        RoundedPanel statusPill = new RoundedPanel(UIScale.scale(10), stBg, false);
        statusPill.setLayout(new BorderLayout());
        statusPill.setBorder(new EmptyBorder(UIScale.scale(4), UIScale.scale(8),
                UIScale.scale(4), UIScale.scale(8))); // khớp Account pill padding
        JLabel stLbl = new JLabel(st);
        stLbl.setFont(AppFonts.lexendBold(10f));
        stLbl.setForeground(stFg);
        statusPill.add(stLbl, BorderLayout.CENTER);

        leftRow.add(idLbl);
        leftRow.add(statusPill);
        p.add(leftRow, BorderLayout.WEST);

        // ── Bên phải: Khung giờ | Ngày | Giá ────────────────────────────
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

    // ====================================================================
    //  Shared helpers
    // ====================================================================

    private JPanel buildPaymentCard() {
        RoundedPanel card = new RoundedPanel(UIScale.scale(24), DARK_CARD_BG, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(UIScale.scale(14), UIScale.scale(16),
                UIScale.scale(14), UIScale.scale(16)));

        JPanel payTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(6), 0));
        payTitleRow.setOpaque(false);
        payTitleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel greenDot2 = makeDot(new Color(100, 220, 100));
        JLabel t = new JLabel("THANH TOÁN");
        t.setFont(AppFonts.lexendBold(12f));
        t.setForeground(TEXT_WHITE);
        payTitleRow.add(greenDot2);
        payTitleRow.add(t);
        card.add(payTitleRow);
        card.add(Box.createVerticalStrut(UIScale.scale(10)));

        card.add(makePayOption("card", "Thẻ tín dụng", true));
        card.add(Box.createVerticalStrut(UIScale.scale(6)));
        card.add(makePayOption("bank", "Chuyển khoản", false));
        card.add(Box.createVerticalStrut(UIScale.scale(6)));
        card.add(makePayOption("wallet", "Ví điện tử", false));
        return card;
    }

    private JPanel makePayOption(String iconKey, String label, boolean selected) {
        RoundedPanel p = new RoundedPanel(UIScale.scale(10), new Color(65, 67, 70), false);
        p.setLayout(new BorderLayout(UIScale.scale(8), 0));
        p.setBorder(new EmptyBorder(UIScale.scale(8), UIScale.scale(12),
                UIScale.scale(8), UIScale.scale(12)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(36)));

        // Map key → icon path
        String iconPath = switch (iconKey) {
            case "card" -> "/icon/user_2.png";   // thẻ → dùng user_2 tạm
            case "bank" -> "/icon/branch.png";   // ngân hàng
            case "wallet" -> "/icon/products.png"; // ví
            default -> null;
        };

        JLabel iconLbl = new JLabel();
        iconLbl.setFont(AppFonts.lexendRegular(12f));
        if (iconPath != null) {
            java.net.URL url = BookingDetailPanel.class.getResource(iconPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                iconLbl.setIcon(new ImageIcon(img));
            }
        }
        p.add(iconLbl, BorderLayout.WEST);

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppFonts.lexendRegular(12f));
        lbl.setForeground(TEXT_WHITE);
        p.add(lbl, BorderLayout.CENTER);

        JLabel dot = new JLabel(selected ? "●" : "○");
        dot.setForeground(selected ? LIME_DARK : Color.GRAY);
        p.add(dot, BorderLayout.EAST);
        return p;
    }

    // ====================================================================
    //  Summary card (right column, bottom)
    // ====================================================================
    private JPanel buildSummaryCard(BookingDetailDTO detail) {
        RoundedPanel card = new RoundedPanel(UIScale.scale(24), SUMMARY_CARD_BG, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(UIScale.scale(12), UIScale.scale(14),
                UIScale.scale(12), UIScale.scale(14)));

        JLabel title = new JLabel("CHI TIẾT THANH TOÁN");
        title.setFont(AppFonts.lexendBold(12f));
        title.setForeground(TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(UIScale.scale(10)));

        // Tiền thuê sân
        String rentStr = detail.getTotalValue() != null ? String.format("%,.0f\u0111", detail.getTotalValue()).replace(",", ".") : "0\u0111";
        card.add(makeSummaryRow("Tiền thuê sân", rentStr, false));

        // Tiền cọc (nếu có)
        if (detail.getDeposit() != null && detail.getDeposit().compareTo(java.math.BigDecimal.ZERO) > 0) {
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            String depositStr = String.format("%,.0f\u0111", detail.getDeposit()).replace(",", ".");
            card.add(makeSummaryRow("Tiền cọc", depositStr, false));
        }

        // Giảm giá (nếu có)
        if (detail.getDiscount() != null && detail.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            card.add(Box.createVerticalStrut(UIScale.scale(6)));
            card.add(makeSummaryRow("Giảm giá", detail.getDiscount().toPlainString() + "%", false));
        }

        card.add(Box.createVerticalStrut(UIScale.scale(10)));
        card.add(makeSeparator());
        card.add(Box.createVerticalStrut(UIScale.scale(10)));

        // Tổng cộng
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setAlignmentX(Component.LEFT_ALIGNMENT);
        bot.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(32)));
        JLabel botLbl = new JLabel("TỔNG CỘNG");
        botLbl.setFont(AppFonts.lexendBold(13f));
        botLbl.setForeground(TEXT_DARK);

        String totalStr = detail.getTotalAmount() != null ? String.format("%,.0f\u0111", detail.getTotalAmount()).replace(",", ".") : "0\u0111";
        JLabel botVal = new JLabel(totalStr);
        botVal.setFont(AppFonts.lexendBold(18f));
        botVal.setForeground(LIME_DARK);
        bot.add(botLbl, BorderLayout.WEST);
        bot.add(botVal, BorderLayout.EAST);
        card.add(bot);

        return card;
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

    // ====================================================================
    //  RoundedLineBorder — khớp Account
    // ====================================================================
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

    // ====================================================================
    //  RoundedPanel
    // ====================================================================
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