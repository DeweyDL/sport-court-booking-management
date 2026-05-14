package com.sportcourt.modules.customer_history.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.common.style.CrudViewStyle;
import com.sportcourt.common.style.UIScale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * View Lịch sử đặt chỗ - Đã tăng độ bo tròn tối đa theo ảnh image_1da6b7.jpg
 */
public class BookingHistoryPanel extends JPanel {
    private static final String LIST_VIEW = "LIST_VIEW";
    private static final String DETAIL_VIEW = "DETAIL_VIEW";

    private static final Color PAGE_BG = new Color(240, 240, 245);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(30, 30, 30);
    private static final Color TEXT_MUTED = new Color(100, 100, 100);
    private static final Color LIME_DARK = new Color(60, 160, 60);
    private static final Color BANNER_GREEN = new Color(74, 122, 28);
    private static final Color DARK_BUTTON_BG = new Color(50, 50, 50);
    private static final Color SEPARATOR_COLOR = new Color(230, 230, 230);
    private static final Color TEXT_LIGHT_BUTTON = Color.WHITE;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final HistoryListPanel listPanel;
    private final HistoryDetailPanel detailPanel;

    public BookingHistoryPanel() {
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);

        listPanel = new HistoryListPanel(this::showDetailView);
        detailPanel = new HistoryDetailPanel(this::showListView);

        contentPanel.setOpaque(false);
        contentPanel.add(listPanel, LIST_VIEW);
        contentPanel.add(detailPanel, DETAIL_VIEW);

        add(contentPanel, BorderLayout.CENTER);
        showListView();

        CrudViewStyle.installResponsiveTypography(this);
    }

    private void showListView() {
        cardLayout.show(contentPanel, LIST_VIEW);
    }

    private void showDetailView() {
        cardLayout.show(contentPanel, DETAIL_VIEW);
    }

    private JComponent createImageLabel(String fileName, int width, int height) {
        String path = "/image/" + fileName;
        URL imgUrl = getClass().getResource(path);

        if (imgUrl != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(imgUrl).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // TĂNG BO TRÒN ẢNH: Lên mức 50px để giống ảnh image_1da6b7.jpg
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, UIScale.scale(50), UIScale.scale(50)));
                    g2.drawImage(icon.getImage(), 0, 0, width, height, null);
                    g2.dispose();
                }
            };
        }
        return new RoundedPanel(UIScale.scale(50), new Color(230, 235, 240));
    }

    private class HistoryListPanel extends JPanel {
        private final Runnable onDetailRequested;

        public HistoryListPanel(Runnable onDetailRequested) {
            this.onDetailRequested = onDetailRequested;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(UIScale.scale(30), UIScale.scale(50), UIScale.scale(30), UIScale.scale(50)));

            add(createHeader(), BorderLayout.NORTH);
            add(createListContent(), BorderLayout.CENTER);
        }

        private JPanel createHeader() {
            JPanel header = new JPanel();
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
            header.setOpaque(false);

            JLabel logoLabel = new JLabel("RENTSTA");
            logoLabel.setFont(AppFonts.lexendBold(30f));
            logoLabel.setForeground(Color.BLACK);
            logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(logoLabel);

            header.add(Box.createVerticalStrut(UIScale.scale(10)));

            JPanel line = new JPanel();
            line.setBackground(SEPARATOR_COLOR);
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(1)));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(line);

            header.add(Box.createVerticalStrut(UIScale.scale(30)));

            JLabel title = new JLabel("Lịch sử đặt chỗ");
            title.setFont(AppFonts.lexendBold(36f));
            title.setForeground(TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(title);

            JLabel subtitle = new JLabel("Xem lại lịch sử đặt sân và chi tiết các buổi tập tại các cơ sở thể thao hàng đầu.");
            subtitle.setFont(AppFonts.lexendRegular(16f));
            subtitle.setForeground(TEXT_MUTED);
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(subtitle);

            header.add(Box.createVerticalStrut(UIScale.scale(30)));

            JPanel toolbar = new JPanel(new BorderLayout());
            toolbar.setOpaque(false);
            toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
            toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(45)));

            JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(15), 0));
            filters.setOpaque(false);
            filters.add(createPillButton("Lọc theo danh mục", Color.WHITE, TEXT_DARK, true));
            filters.add(createPillButton("3 tháng qua", Color.WHITE, TEXT_DARK, true));

            JLabel sortLabel = new JLabel("Sắp xếp: Mới nhất");
            sortLabel.setFont(AppFonts.lexendBold(14f));
            sortLabel.setForeground(LIME_DARK);

            toolbar.add(filters, BorderLayout.WEST);
            toolbar.add(sortLabel, BorderLayout.EAST);
            header.add(toolbar);

            return header;
        }

        private JScrollPane createListContent() {
            JPanel listWrapper = new JPanel();
            listWrapper.setLayout(new BoxLayout(listWrapper, BoxLayout.Y_AXIS));
            listWrapper.setOpaque(false);
            listWrapper.setBorder(new EmptyBorder(UIScale.scale(25), 0, 0, 0));

            listWrapper.add(createItemCard("Sân Tennis - Quận 1", "12 Nguyễn Huệ, Quận 1, TP.HCM", "24/10/2026", "400.000 VNĐ", "tennis.jpg"));
            listWrapper.add(Box.createVerticalStrut(UIScale.scale(20)));
            listWrapper.add(createItemCard("Sân Cầu Lông - Quận 3", "45 Võ Văn Tần, Quận 3, TP.HCM", "18/10/2026", "150.000 VNĐ", "badminton.png"));
            listWrapper.add(Box.createVerticalStrut(UIScale.scale(20)));
            listWrapper.add(createItemCard("Sân Bóng Đá - Bình Thạnh", "88 Điện Biên Phủ, Bình Thạnh, TP.HCM", "12/10/2026", "1.200.000 VNĐ", "court.png"));

            JScrollPane scrollPane = new JScrollPane(listWrapper);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            return scrollPane;
        }

        private JPanel createItemCard(String title, String loc, String date, String price, String imgFileName) {
            // TĂNG BO TRÒN CARD: Lên mức 60px để có độ cong mạnh như trong image_1da6b7.jpg
            RoundedPanel card = new RoundedPanel(UIScale.scale(60), CARD_BG, true);
            card.setLayout(new BorderLayout(UIScale.scale(30), 0));
            card.setBorder(new EmptyBorder(UIScale.scale(25), UIScale.scale(25), UIScale.scale(25), UIScale.scale(35)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIScale.scale(160)));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);

            JComponent img = createImageLabel(imgFileName, UIScale.scale(190), UIScale.scale(110));
            img.setPreferredSize(new Dimension(UIScale.scale(190), UIScale.scale(110)));

            JPanel mid = new JPanel();
            mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
            mid.setOpaque(false);
            mid.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UIScale.scale(12), 0));
            tagRow.setOpaque(false);
            tagRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            tagRow.add(CrudViewStyle.createStatusPill("ĐÃ HOÀN THÀNH", new Color(220, 252, 231), LIME_DARK));
            JLabel dateLbl = new JLabel(date);
            dateLbl.setFont(AppFonts.lexendRegular(14f));
            dateLbl.setForeground(TEXT_MUTED);
            tagRow.add(dateLbl);

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(AppFonts.lexendBold(20f));
            titleLbl.setForeground(TEXT_DARK);
            titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel locLbl = new JLabel(loc);
            locLbl.setFont(AppFonts.lexendRegular(15f));
            locLbl.setForeground(TEXT_MUTED);
            locLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            mid.add(tagRow);
            mid.add(Box.createVerticalStrut(UIScale.scale(15)));
            mid.add(titleLbl);
            mid.add(Box.createVerticalStrut(UIScale.scale(8)));
            mid.add(locLbl);

            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);
            right.setAlignmentX(Component.RIGHT_ALIGNMENT);

            JLabel prcLbl = new JLabel(price);
            prcLbl.setFont(AppFonts.lexendBold(22f));
            prcLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

            JButton btn = createPillButton("Xem chi tiết", DARK_BUTTON_BG, TEXT_LIGHT_BUTTON, false);
            btn.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btn.addActionListener(e -> onDetailRequested.run());

            right.add(Box.createVerticalGlue());
            right.add(prcLbl);
            right.add(Box.createVerticalStrut(UIScale.scale(15)));
            right.add(btn);
            right.add(Box.createVerticalGlue());

            card.add(img, BorderLayout.WEST);
            card.add(mid, BorderLayout.CENTER);
            card.add(right, BorderLayout.EAST);
            return card;
        }
    }

    private class HistoryDetailPanel extends JPanel {
        private final Runnable onBack;

        public HistoryDetailPanel(Runnable onBack) {
            this.onBack = onBack;
            setLayout(new BorderLayout(0, UIScale.scale(30)));
            setOpaque(false);
            setBorder(new EmptyBorder(UIScale.scale(30), UIScale.scale(50), UIScale.scale(30), UIScale.scale(50)));

            JLabel backBtn = new JLabel("< RENTSTA");
            backBtn.setFont(AppFonts.lexendBold(26f));
            backBtn.setForeground(Color.BLACK);
            backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            backBtn.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onBack.run(); }
            });
            add(backBtn, BorderLayout.NORTH);

            JPanel main = new JPanel(new GridBagLayout());
            main.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.BOTH;
            g.weightx = 1.0;

            // TĂNG BO TRÒN BANNER: Lên mức 45px
            RoundedPanel banner = new RoundedPanel(UIScale.scale(45), BANNER_GREEN) {
                @Override
                protected void paintComponent(Graphics g) {
                    URL bg = getClass().getResource("/image/court.png");
                    if (bg != null) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), UIScale.scale(45), UIScale.scale(45)));
                        g2.drawImage(new ImageIcon(bg).getImage(), 0, -150, getWidth(), getWidth(), null);
                        g2.setColor(new Color(0, 0, 0, 60));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            banner.setPreferredSize(new Dimension(0, UIScale.scale(160)));
            banner.setLayout(new BorderLayout());
            JLabel bt = new JLabel("CHI TIẾT LỊCH SỬ ĐẶT CHỖ", SwingConstants.CENTER);
            bt.setFont(AppFonts.lexendBold(36f));
            bt.setForeground(Color.WHITE);
            banner.add(bt, BorderLayout.CENTER);

            g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
            g.insets = new Insets(0, 0, UIScale.scale(30), 0);
            main.add(banner, g);

            g.gridy = 1; g.gridwidth = 1; g.weightx = 0.65;
            g.insets = new Insets(0, 0, 0, UIScale.scale(30));
            main.add(createBookingInfo(), g);

            JPanel right = new JPanel(new BorderLayout(0, UIScale.scale(30)));
            right.setOpaque(false);
            right.add(createPaymentMethodCard(), BorderLayout.NORTH);
            right.add(createTotalCard(), BorderLayout.CENTER);

            g.gridx = 1; g.weightx = 0.35; g.insets = new Insets(0, 0, 0, 0);
            main.add(right, g);

            add(main, BorderLayout.CENTER);
        }

        private JPanel createBookingInfo() {
            // TĂNG BO TRÒN PANEL THÔNG TIN: Lên mức 45px
            RoundedPanel card = new RoundedPanel(UIScale.scale(45), new Color(241, 242, 246));
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(UIScale.scale(30), UIScale.scale(30), UIScale.scale(30), UIScale.scale(30)));

            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            JLabel title = new JLabel("THÔNG TIN ĐẶT SÂN");
            title.setFont(AppFonts.lexendBold(18f));
            JButton addBtn = createPillButton("Thêm sân", LIME_DARK, Color.WHITE, true);
            head.add(title, BorderLayout.WEST);
            head.add(addBtn, BorderLayout.EAST);

            card.add(head);
            card.add(Box.createVerticalStrut(UIScale.scale(20)));
            card.add(createField("Khách hàng", "Nguyễn Văn A"));
            card.add(createField("Số điện thoại", "0123456789"));
            card.add(createField("Mã đơn", "HD001"));
            card.add(Box.createVerticalStrut(UIScale.scale(15)));
            card.add(new JSeparator());
            card.add(Box.createVerticalStrut(UIScale.scale(15)));
            card.add(createField("Chi nhánh", "Chi nhánh Quận 1"));
            card.add(createField("Địa chỉ", "12 Nguyễn Huệ, Quận 1, TP.HCM"));
            card.add(Box.createVerticalStrut(UIScale.scale(20)));

            card.add(createCourtItem("Mã sân 1", "18:00 - 20:00 | 29/12/2026", "250.000đ/giờ"));
            card.add(Box.createVerticalStrut(UIScale.scale(10)));
            card.add(createCourtItem("Mã sân 2", "18:00 - 20:00 | 29/12/2026", "250.000đ/giờ"));

            card.add(Box.createVerticalStrut(UIScale.scale(25)));
            JPanel total = new JPanel(new BorderLayout());
            total.setOpaque(false);
            JLabel lblTotal = new JLabel("Tổng tiền thuê sân");
            lblTotal.setFont(AppFonts.lexendRegular(14f));
            total.add(lblTotal, BorderLayout.WEST);
            JLabel val = new JLabel("500.000đ");
            val.setFont(AppFonts.lexendBold(22f));
            total.add(val, BorderLayout.EAST);
            card.add(total);

            return card;
        }

        private JPanel createPaymentMethodCard() {
            // TĂNG BO TRÒN PANEL THANH TOÁN: Lên mức 45px
            RoundedPanel card = new RoundedPanel(UIScale.scale(45), DARK_BUTTON_BG);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(UIScale.scale(25), UIScale.scale(25), UIScale.scale(25), UIScale.scale(25)));
            JLabel t = new JLabel("THANH TOÁN");
            t.setForeground(Color.WHITE); t.setFont(AppFonts.lexendBold(16f));
            card.add(t); card.add(Box.createVerticalStrut(UIScale.scale(20)));
            card.add(createPayOpt("Thẻ tín dụng", true));
            card.add(Box.createVerticalStrut(UIScale.scale(10)));
            card.add(createPayOpt("Chuyển khoản", false));
            return card;
        }

        private JPanel createTotalCard() {
            // TĂNG BO TRÒN PANEL TỔNG CỘNG: Lên mức 45px
            RoundedPanel card = new RoundedPanel(UIScale.scale(45), new Color(230, 232, 235));
            card.setLayout(new BorderLayout());
            card.setBorder(new EmptyBorder(UIScale.scale(30), UIScale.scale(30), UIScale.scale(30), UIScale.scale(30)));

            JPanel p = new JPanel(new GridLayout(2, 1, 0, UIScale.scale(10)));
            p.setOpaque(false);
            JLabel title = new JLabel("CHI TIẾT THANH TOÁN");
            title.setFont(AppFonts.lexendBold(14f));
            p.add(title);

            JPanel r = new JPanel(new BorderLayout()); r.setOpaque(false);
            JLabel lblRent = new JLabel("Tiền thuê sân");
            lblRent.setFont(AppFonts.lexendRegular(14f));
            r.add(lblRent, BorderLayout.WEST);
            JLabel valRent = new JLabel("500.000đ");
            valRent.setFont(AppFonts.lexendBold(14f));
            r.add(valRent, BorderLayout.EAST);
            p.add(r);
            card.add(p, BorderLayout.NORTH);

            JPanel bot = new JPanel(new BorderLayout()); bot.setOpaque(false);
            JLabel lblTotal = new JLabel("TỔNG CỘNG");
            lblTotal.setFont(AppFonts.lexendBold(16f));
            bot.add(lblTotal, BorderLayout.WEST);
            JLabel v = new JLabel("500.000đ");
            v.setFont(AppFonts.lexendBold(24f));
            v.setForeground(LIME_DARK);
            bot.add(v, BorderLayout.EAST);
            card.add(bot, BorderLayout.SOUTH);
            return card;
        }

        private JPanel createField(String l, String v) {
            JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
            JLabel jl = new JLabel(l); jl.setForeground(TEXT_MUTED);
            jl.setFont(AppFonts.lexendRegular(13f));
            JLabel jv = new JLabel(v); jv.setFont(AppFonts.lexendBold(14f));
            p.add(jl, BorderLayout.WEST); p.add(jv, BorderLayout.EAST);
            return p;
        }

        private JPanel createCourtItem(String id, String time, String prc) {
            // TĂNG BO TRÒN ITEM SÂN: Lên mức 20px
            RoundedPanel p = new RoundedPanel(UIScale.scale(20), Color.WHITE);
            p.setLayout(new BorderLayout(UIScale.scale(15), 0));
            p.setBorder(new EmptyBorder(UIScale.scale(12), UIScale.scale(15), UIScale.scale(12), UIScale.scale(15)));
            JLabel jid = new JLabel(id);
            jid.setFont(AppFonts.lexendRegular(13f));
            p.add(jid, BorderLayout.WEST);
            JLabel info = new JLabel(time + "   " + prc);
            info.setForeground(LIME_DARK); info.setFont(AppFonts.lexendBold(13f));
            p.add(info, BorderLayout.EAST);
            return p;
        }

        private JPanel createPayOpt(String text, boolean sel) {
            // TĂNG BO TRÒN OPTION THANH TOÁN: Lên mức 15px
            RoundedPanel p = new RoundedPanel(UIScale.scale(15), new Color(65, 67, 70));
            p.setLayout(new BorderLayout()); p.setBorder(new EmptyBorder(UIScale.scale(12), UIScale.scale(15), UIScale.scale(12), UIScale.scale(15)));
            JLabel l = new JLabel(text); l.setForeground(Color.WHITE);
            l.setFont(AppFonts.lexendRegular(13f));
            p.add(l, BorderLayout.WEST);
            if (sel) {
                JLabel dot = new JLabel("●"); dot.setForeground(new Color(163, 230, 53));
                p.add(dot, BorderLayout.EAST);
            }
            return p;
        }
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
                    g2.setColor(SEPARATOR_COLOR);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(bold ? AppFonts.lexendBold(14f) : AppFonts.lexendRegular(14f));
        btn.setForeground(fg);
        btn.setBorder(new EmptyBorder(UIScale.scale(10), UIScale.scale(25), UIScale.scale(10), UIScale.scale(25)));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color background;
        private final boolean shadow;

        public RoundedPanel(int radius, Color background) { this(radius, background, false); }
        public RoundedPanel(int radius, Color background, boolean shadow) {
            this.radius = radius;
            this.background = background;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int contentHeight = shadow ? getHeight() - UIScale.scale(8) : getHeight();
            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 8));
                g2.fillRoundRect(0, UIScale.scale(8), getWidth(), getHeight() - UIScale.scale(8), radius, radius);
            }
            g2.setColor(background);
            g2.fillRoundRect(0, 0, getWidth(), contentHeight, radius, radius);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("RENTSTA - History Demo");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1200, 900);
            f.add(new BookingHistoryPanel());
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}