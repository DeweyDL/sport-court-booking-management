package com.sportcourt.modules.booking_management.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.sportcourt.modules.booking_management.controller.BookingRequestController;
import com.sportcourt.modules.booking_management.dto.BookingInvoiceDTO;
import com.sportcourt.modules.booking_management.dto.BookingPitchDetailDTO;
import com.sportcourt.modules.booking_management.dto.BookingSlotDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingDetailPanel extends JFrame {

    private final BookingRequestController controller;
    private String invoiceId;
    private String bookingDetailId;

    // Các thành phần UI hiển thị dữ liệu động
    private JLabel customerNameLabel;
    private JLabel customerPhoneLabel;
    private JLabel bookingIdLabel;
    private JLabel branchNameLabel;
    private JLabel branchAddressLabel;
    private JPanel pitchesPanel;
    private JLabel totalAmountLabel;
    private JLabel paymentStatusLabel;
    private JLabel totalPaymentLabel;
    private JLabel subTotalLabel;
    private JButton btnCancel; // Nút hủy đặt sân toàn cục để điều khiển trạng thái ẩn/hiện
    private JButton btnConfirm;
    private final boolean allowConfirmAction;
    private final Runnable onConfirmed;

    // Thành phần chứa Icon/Ảnh động
    private JLabel bannerImageLabel;
    private JLabel infoIconLabel;

    public BookingDetailPanel(BookingSlotDTO clickedSlot) {
        this(clickedSlot, false, null);
    }

    public BookingDetailPanel(BookingSlotDTO clickedSlot, boolean allowConfirmAction, Runnable onConfirmed) {
        this.bookingDetailId = clickedSlot.bookingDetailId();
        this.invoiceId = clickedSlot.invoiceId();
        this.controller = new BookingRequestController();
        this.allowConfirmAction = allowConfirmAction;
        this.onConfirmed = onConfirmed;

        setTitle("Chi tiết đặt sân - RENTSTA");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(250, 250, 250));
        setLayout(new BorderLayout());

        // --- MAIN CONTENT ---
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(new Color(250, 250, 250));
        mainContent.setBorder(new EmptyBorder(20, 40, 40, 40));

        // 1. Banner quảng cáo/tiêu đề hình ảnh
        mainContent.add(createBannerPanel());
        mainContent.add(Box.createVerticalStrut(30));

        // 2. Khu vực chia 2 cột chi tiết
        JPanel detailsContainer = new JPanel(new GridBagLayout());
        detailsContainer.setBackground(new Color(250, 250, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Cột trái: Thông tin đặt sân
        gbc.gridx = 0;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 0, 15);
        detailsContainer.add(createLeftPanel(), gbc);

        // Cột phải: Chi tiết thanh toán & Tác vụ
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.insets = new Insets(0, 15, 0, 0);
        detailsContainer.add(createRightPanel(), gbc);

        mainContent.add(detailsContainer);
        add(mainContent, BorderLayout.CENTER);

        // Thử tải ảnh/icon mặc định
        loadTitleIcon("/icon/sta.png");
        loadBannerImage("/image/court2.png");

        // Đổ dữ liệu thực tế từ Database
        renderRealBookingDetails(clickedSlot);
    }

    public void loadBannerImage(String pathOrResource) {
        try {
            ImageIcon icon;
            if (new File(pathOrResource).exists()) {
                icon = new ImageIcon(pathOrResource);
            } else {
                java.net.URL imgURL = getClass().getResource(pathOrResource);
                if (imgURL != null) icon = new ImageIcon(imgURL);
                else return;
            }
            Image img = icon.getImage().getScaledInstance(820, 120, Image.SCALE_SMOOTH);
            bannerImageLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Không thể tải ảnh banner: " + e.getMessage());
        }
    }

    public void loadTitleIcon(String pathOrResource) {
        try {
            ImageIcon icon;
            if (new File(pathOrResource).exists()) {
                icon = new ImageIcon(pathOrResource);
            } else {
                java.net.URL imgURL = getClass().getResource(pathOrResource);
                if (imgURL != null) icon = new ImageIcon(imgURL);
                else return;
            }
            Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            infoIconLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            System.err.println("Không thể tải icon tiêu đề: " + e.getMessage());
        }
    }

    public void setBookingData(
            String customerName, String phone, String invoiceId,
            String branchName, String address,
            double totalAmount, String status,
            List<BookingPitchDetailDTO> pitches) {

        this.invoiceId = invoiceId;
        customerNameLabel.setText(customerName != null ? customerName : "");
        customerPhoneLabel.setText(phone != null ? phone : "");
        bookingIdLabel.setText(invoiceId != null ? invoiceId : "");
        branchNameLabel.setText(branchName != null ? branchName : "");
        branchAddressLabel.setText(address != null ? address : "Chưa cập nhật địa chỉ");

        String formattedTotal = String.format("%,.0fđ", totalAmount).replace(",", ".");
        totalAmountLabel.setText(formattedTotal);
        subTotalLabel.setText(formattedTotal);
        totalPaymentLabel.setText(formattedTotal);

        paymentStatusLabel.setText(status != null ? status : "Chờ xác nhận");

        // Thiết kế nhãn và ẩn nút dựa vào trạng thái đơn đặt
        String normalizedStatus = status == null ? "" : status.trim();
        if (normalizedStatus.equalsIgnoreCase("ĐÃ XÁC NHẬN") || normalizedStatus.equalsIgnoreCase("Đã xác nhận")) {
            paymentStatusLabel.setForeground(new Color(22, 101, 52));
            paymentStatusLabel.getParent().setBackground(new Color(220, 252, 231));
            btnCancel.setVisible(true);
        } else if (normalizedStatus.equalsIgnoreCase("ĐÃ HUỶ")
                || normalizedStatus.equalsIgnoreCase("Đã hủy")
                || normalizedStatus.equalsIgnoreCase("ĐÃ HỦY")
                || normalizedStatus.equalsIgnoreCase("ĐÃ HUỶ")) {
            // Đã thêm "ĐÃ HUỶ" (chuẩn Oracle của bạn) và bọc .trim() để chống khoảng trắng thừa
            paymentStatusLabel.setText("Đã hủy"); // Ép chữ hiển thị đẹp mắt
            paymentStatusLabel.setForeground(new Color(153, 27, 27));
            paymentStatusLabel.getParent().setBackground(new Color(254, 226, 226));
            btnCancel.setVisible(false); // Ẩn hoàn toàn nút hủy khi đơn đã ở trạng thái hủy
        } else if (normalizedStatus.equalsIgnoreCase("ĐÃ CỌC")) {
            paymentStatusLabel.setForeground(new Color(180, 83, 9));
            paymentStatusLabel.getParent().setBackground(new Color(254, 243, 199));
            btnCancel.setVisible(true);
        } else {
            // Các trạng thái còn lại như 'ĐÃ ĐẶT CHỜ CỌC', 'ĐÃ CỌC', 'ĐANG SỬ DỤNG'...
            paymentStatusLabel.setForeground(new Color(180, 83, 9));
            paymentStatusLabel.getParent().setBackground(new Color(254, 243, 199));
            btnCancel.setVisible(true);
        }
        btnConfirm.setVisible(allowConfirmAction && normalizedStatus.equalsIgnoreCase("ĐÃ CỌC"));

        pitchesPanel.removeAll();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (pitches != null) {
            for (BookingPitchDetailDTO pitch : pitches) {
                String timeFormat = String.format("%02d:00 - %02d:00", pitch.startHour(), pitch.endHour());
                String dateStr = pitch.bookingDate() != null ? pitch.bookingDate().format(dtf) : "N/A";
                String timeAndDate = timeFormat + " | " + dateStr;

                JPanel row = createPitchRow("Sân " + pitch.courtId(), timeAndDate, formattedTotal);
                pitchesPanel.add(row);
                pitchesPanel.add(Box.createVerticalStrut(10));
            }
        }

        pitchesPanel.revalidate();
        pitchesPanel.repaint();
    }

    public BookingDetailPanel() {
        this(new BookingSlotDTO(
                "CT001", "HD_RENTSTA_999", "1", "KV_01",
                "Bóng Đá", "Nguyễn Văn A", "0123456789", 18, 20, LocalDate.now()
        ));
    }

    private void renderRealBookingDetails(BookingSlotDTO slot) {
        if (slot.bookingDetailId() == null || slot.bookingDetailId().isBlank()) {
            showFallbackData(slot);
            return;
        }

        new Thread(() -> {
            try {
                // THAY ĐỔI: Truyền slot.bookingDetailId() thay vì slot.invoiceId() sang Controller
                BookingInvoiceDTO invoiceDTO = controller.getInvoiceDetails(slot.bookingDetailId());
                if (invoiceDTO != null) {
                    SwingUtilities.invokeLater(() -> setBookingData(
                            invoiceDTO.customerName(),
                            invoiceDTO.customerPhone(),
                            invoiceDTO.invoiceId(),
                            invoiceDTO.branchName(),
                            invoiceDTO.branchAddress(),
                            invoiceDTO.totalAmount(),
                            invoiceDTO.status(), // Lúc này đã mang giá trị trạng thái chi tiết thuê sân
                            invoiceDTO.pitches()
                    ));
                } else {
                    SwingUtilities.invokeLater(() -> showFallbackData(slot));
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> showFallbackData(slot));
            }
        }).start();
    }

    private void showFallbackData(BookingSlotDTO slot) {
        List<BookingPitchDetailDTO> fallbackPitches = new ArrayList<>();
        int duration = slot.endHour() - slot.startHour();
        double estimatedPrice = duration * 250000;

        fallbackPitches.add(new BookingPitchDetailDTO(
                slot.courtId(),
                slot.startHour(),
                slot.endHour(),
                slot.bookingDate() != null ? slot.bookingDate() : LocalDate.now(),
                estimatedPrice
        ));

        setBookingData(
                slot.customerName(),
                slot.customerPhone(),
                slot.invoiceId(),
                "Chi nhánh mặc định",
                "Địa chỉ hệ thống",
                estimatedPrice,
                "Đã xác nhận",
                fallbackPitches
        );
    }

    // ================= BANNER PANEL =================
    private JPanel createBannerPanel() {
        RoundedPanel bannerPanel = new RoundedPanel(30, new Color(63, 82, 60));
        bannerPanel.setLayout(new BorderLayout());
        bannerPanel.setPreferredSize(new Dimension(820, 200));
        bannerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        bannerImageLabel = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int radius = 30;
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight() + radius, radius, radius));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        bannerImageLabel.setPreferredSize(new Dimension(820, 120));
        bannerPanel.add(bannerImageLabel, BorderLayout.CENTER);

        JPanel titleBox = new JPanel(new GridBagLayout());
        titleBox.setOpaque(false);
        titleBox.setPreferredSize(new Dimension(820, 80));

        JLabel bannerTitle = new JLabel("CHI TIẾT ĐẶT SÂN");
        bannerTitle.setFont(new Font("Lexend", Font.BOLD, 28));
        bannerTitle.setForeground(Color.WHITE);

        titleBox.add(bannerTitle);
        bannerPanel.add(titleBox, BorderLayout.SOUTH);

        return bannerPanel;
    }

    // ================= LEFT PANEL (THÔNG TIN ĐẶT SÂN) =================
    private JPanel createLeftPanel() {
        RoundedPanel panel = new RoundedPanel(25, new Color(245, 245, 245));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerRow.setOpaque(false);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        infoIconLabel = new JLabel();
        JLabel title = new JLabel("THÔNG TIN ĐẶT SÂN");
        title.setFont(new Font("Lexend", Font.BOLD, 16));

        headerRow.add(infoIconLabel);
        headerRow.add(title);
        panel.add(headerRow);

        panel.add(Box.createVerticalStrut(20));

        customerNameLabel = new JLabel();
        panel.add(createRow("Khách hàng", customerNameLabel, false));
        panel.add(Box.createVerticalStrut(12));

        customerPhoneLabel = new JLabel();
        panel.add(createRow("Số điện thoại", customerPhoneLabel, false));
        panel.add(Box.createVerticalStrut(12));

        bookingIdLabel = new JLabel();
        panel.add(createRow("Mã đơn", bookingIdLabel, true));

        panel.add(Box.createVerticalStrut(15));
        panel.add(createSeparator());
        panel.add(Box.createVerticalStrut(15));

        branchNameLabel = new JLabel();
        panel.add(createRow("Chi nhánh", branchNameLabel, true));
        panel.add(Box.createVerticalStrut(12));

        branchAddressLabel = new JLabel();
        panel.add(createRow("Địa chỉ", branchAddressLabel, true));

        panel.add(Box.createVerticalStrut(20));

        pitchesPanel = new JPanel();
        pitchesPanel.setLayout(new BoxLayout(pitchesPanel, BoxLayout.Y_AXIS));
        pitchesPanel.setOpaque(false);
        panel.add(pitchesPanel);

        panel.add(Box.createVerticalGlue());
        panel.add(Box.createVerticalStrut(15));
        panel.add(createSeparator());
        panel.add(Box.createVerticalStrut(15));

        totalAmountLabel = new JLabel();
        JPanel totalRow = createRow("Tổng tiền thuê sân", totalAmountLabel, true);
        Component[] comps = totalRow.getComponents();
        for (Component comp : comps) {
            if (comp == totalAmountLabel) {
                ((JLabel) comp).setFont(new Font("Lexend", Font.BOLD, 22));
            }
        }
        panel.add(totalRow);

        return panel;
    }

    // ================= RIGHT PANEL (CHI TIẾT THANH TOÁN) =================
    private JPanel createRightPanel() {
        RoundedPanel panel = new RoundedPanel(25, new Color(235, 235, 235));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("CHI TIẾT THANH TOÁN");
        title.setFont(new Font("Lexend", Font.BOLD, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        JPanel statusWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusWrapper.setOpaque(false);
        statusWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        statusWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel statusBadge = new RoundedPanel(15, new Color(220, 252, 231));
        statusBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 6));
        paymentStatusLabel = new JLabel("Chờ xác nhận");
        paymentStatusLabel.setFont(new Font("Lexend", Font.BOLD, 13));
        paymentStatusLabel.setForeground(new Color(22, 101, 52));
        statusBadge.add(paymentStatusLabel);

        statusWrapper.add(statusBadge);
        panel.add(statusWrapper);
        panel.add(Box.createVerticalStrut(10));

        subTotalLabel = new JLabel();
        JPanel subTotalRow = createRow("Tiền thuê sân", subTotalLabel, false);
        subTotalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(subTotalRow);
        panel.add(Box.createVerticalStrut(15));

        JSeparator sepTop = createSeparator();
        sepTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(sepTop);
        panel.add(Box.createVerticalStrut(10));

        totalPaymentLabel = new JLabel();
        JPanel totalRow = createRow("TỔNG CỘNG", totalPaymentLabel, true);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        Component[] comps = totalRow.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JLabel) {
                JLabel lbl = (JLabel) comp;
                lbl.setFont(new Font("Lexend", Font.BOLD, lbl.getText().equals("TỔNG CỘNG") ? 16 : 22));
                if (lbl == totalPaymentLabel) lbl.setForeground(new Color(22, 163, 74));
            }
        }
        panel.add(totalRow);

        panel.add(Box.createVerticalStrut(20));

        // Nút HỦY ĐẶT SÂN tích hợp logic kiểm soát và kết nối xử lý cơ sở dữ liệu
        btnConfirm = new JButton("XÁC NHẬN");
        styleButton(btnConfirm, new Color(57, 255, 20), new Color(16, 110, 0));
        btnConfirm.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConfirm.setVisible(false);
        btnConfirm.addActionListener(e -> executeConfirmBookingAction());
        panel.add(btnConfirm);
        panel.add(Box.createVerticalStrut(12));

        btnCancel = new JButton("HỦY ĐẶT SÂN");
        styleButton(btnCancel, new Color(239, 68, 68), Color.WHITE);
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // SỬA TẠI ĐÂY: Thêm chức năng Backend khi tương tác bấm nút hủy
        btnCancel.addActionListener(e -> executeCancelBookingAction());

        panel.add(btnCancel);

        return panel;
    }

    private void executeConfirmBookingAction() {
        if (invoiceId == null || invoiceId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy mã hóa đơn hợp lệ!", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận yêu cầu đặt sân đã cọc này?",
                "Xác nhận đặt sân",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        btnConfirm.setEnabled(false);
        new Thread(() -> {
            boolean success = controller.confirmPendingDepositBooking(invoiceId);
            SwingUtilities.invokeLater(() -> {
                btnConfirm.setEnabled(true);
                if (success) {
                    paymentStatusLabel.setText("ĐÃ XÁC NHẬN");
                    paymentStatusLabel.setForeground(new Color(22, 101, 52));
                    paymentStatusLabel.getParent().setBackground(new Color(220, 252, 231));
                    btnConfirm.setVisible(false);
                    if (onConfirmed != null) {
                        onConfirmed.run();
                    }
                    JOptionPane.showMessageDialog(this, "Xác nhận đặt sân thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Không còn yêu cầu đã cọc chờ xác nhận cho hóa đơn này.", "Không thể xác nhận", JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }

    /**
     * Thực hiện logic hộp thoại và gọi Controller cập nhật xuống DB
     */
    private void executeCancelBookingAction() {
        if (bookingDetailId == null || bookingDetailId.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy mã chi tiết đặt sân hợp lệ!", "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn hủy lượt đặt sân này không?\nHệ thống sẽ tự động tính toán lại tiền cọc theo quy định.",
                "Xác nhận hủy đặt sân nghiệp vụ",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                // Gọi qua Controller -> Service -> DAO -> Gọi Procedure
                boolean success = controller.cancelBooking(bookingDetailId);

                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Hủy đặt sân thành công!\nHệ thống đã tự động tính lại tiền thuê và tiền cọc.",
                                "Thông báo thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        // Cập nhật nhãn trạng thái sang 'Đã hủy' theo chuẩn hiển thị giao diện
                        paymentStatusLabel.setText("Đã hủy");
                        paymentStatusLabel.setForeground(new Color(153, 27, 27));
                        paymentStatusLabel.getParent().setBackground(new Color(254, 226, 226));

                        // Ẩn hoàn toàn nút hủy đặt sân
                        btnCancel.setVisible(false);
                        btnCancel.getParent().revalidate();
                        btnCancel.getParent().repaint();

                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Hủy đặt sân thất bại!\nLượt đặt có thể đã hoàn thành hoặc hóa đơn đã thanh toán trước đó.",
                                "Lỗi nghiệp vụ",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            }).start();
        }
    }

    // ================= HELPER METHODS =================
    private JPanel createRow(String leftText, JLabel rightLabel, boolean isRightBold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel leftLabel = new JLabel(leftText);
        leftLabel.setForeground(new Color(80, 80, 80));
        leftLabel.setFont(new Font("Lexend", Font.PLAIN, 14));

        rightLabel.setFont(new Font("Lexend", isRightBold ? Font.BOLD : Font.PLAIN, 14));
        rightLabel.setForeground(Color.BLACK);

        row.add(leftLabel, BorderLayout.WEST);
        row.add(rightLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createPitchRow(String courtName, String time, String price) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        RoundedPanel badgePanel = new RoundedPanel(15, Color.WHITE);
        badgePanel.setLayout(new BorderLayout(15, 0));
        badgePanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        JLabel nameLabel = new JLabel(courtName);
        nameLabel.setFont(new Font("Lexend", Font.BOLD, 14));
        nameLabel.setForeground(new Color(50, 50, 50));

        JPanel rightInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 2));
        rightInfoPanel.setOpaque(false);

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Lexend", Font.BOLD, 13));
        timeLabel.setForeground(new Color(16, 110, 0));

        JLabel priceLabel = new JLabel(price);
        priceLabel.setFont(new Font("Lexend", Font.BOLD, 13));
        priceLabel.setForeground(new Color(16, 110, 0));

        rightInfoPanel.add(timeLabel);
        rightInfoPanel.add(priceLabel);

        badgePanel.add(nameLabel, BorderLayout.WEST);
        badgePanel.add(rightInfoPanel, BorderLayout.EAST);

        row.add(badgePanel);
        return row;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(new Color(200, 200, 200));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void styleButton(JButton btn, Color bgColor, Color fgColor) {
        btn.setFont(new Font("Lexend", Font.BOLD, 18));
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 15");
    }

    class RoundedPanel extends JPanel {
        private int cornerRadius;
        private Color backgroundColor;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            this.backgroundColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("defaultFont", new Font("Lexend", Font.PLAIN, 14));
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            new BookingDetailPanel().setVisible(true);
        });
    }
}
