package com.sportcourt.modules.booking_management.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.sportcourt.modules.auth.service.SessionManager;
import com.sportcourt.modules.booking_management.controller.BookingRequestController;
import com.sportcourt.modules.booking_management.dto.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BookingRequest extends JPanel {

    private static final Color COLOR_PRIMARY_GREEN = new Color(16, 110, 0);
    private static final Color COLOR_BG_LIGHT = new Color(250, 250, 250);
    private static final Color COLOR_BG_GRAY = new Color(243, 243, 246);
    private static final Color COLOR_TEXT_DARK = new Color(26, 28, 30);
    private static final Color COLOR_TEXT_LABEL = new Color(60, 75, 53);
    private static final Color COLOR_BOOKED_BLUE = new Color(0, 97, 171);
    private static final Color COLOR_BORDER_LIGHT = new Color(230, 230, 230);
    private static final int ROUND_ARC = 20;
    private static final String STATUS_CONFIRMED = "ĐÃ XÁC NHẬN";
    private static final String STATUS_DEPOSITED = "ĐÃ CỌC CHỜ XÁC NHẬN";
    private static final String STATUS_IN_USE = "ĐANG SỬ DỤNG";
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int BOOKING_BLOCK_PAD_X = 4;
    private static final int BOOKING_BLOCK_PAD_Y = 8;
    private static final int BOOKING_BLOCK_ARC = 18;

    private final BookingRequestController controller = new BookingRequestController();

    private JComboBox<BookingBranchOption> branchCombo;
    private JComboBox<SportTypeAreaOption> sportTypeAreaCombo;
    private DatePicker bookingDatePicker;
    private JButton pendingButton;

    private BookingOpenHours openHours = BookingOpenHours.fullDay();
    private JTable table;
    private DefaultTableModel model;
    private final Set<String> selectedEmptySlots = new HashSet<>();

    // Biến cờ để tránh trigger gọi API liên tục khi đang làm mới các ComboBox
    private boolean isUpdatingCombos = false;

    public BookingRequest() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG_LIGHT);
        add(createHeader(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG_LIGHT);
        mainPanel.add(createFilterBar(), BorderLayout.NORTH);
        mainPanel.add(createBookingGrid(), BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Chạy khởi tạo bất đồng bộ để tránh nghẽn luồng EDT của Swing
        SwingUtilities.invokeLater(this::loadInitialOptions);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_PRIMARY_GREEN);
        header.setBorder(new EmptyBorder(14, 36, 14, 36));

        JLabel title = new JLabel("Quản lý đặt sân");
        title.setFont(new Font("Lexend", Font.BOLD, 22));
        title.setForeground(new Color(248, 250, 252));

        JPanel requestWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        requestWrapper.setOpaque(false);

        pendingButton = new JButton("Yêu cầu đặt sân (0)");
        pendingButton.setFont(new Font("Lexend", Font.BOLD, 14));
        pendingButton.setForeground(COLOR_TEXT_DARK);
        pendingButton.setBackground(Color.WHITE);
        pendingButton.setFocusPainted(false);
        pendingButton.setBorder(new EmptyBorder(10, 22, 10, 22));
        pendingButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pendingButton.putClientProperty(FlatClientProperties.STYLE, "arc: 999");
        pendingButton.addActionListener(e -> showPendingRequestsDialog());

        requestWrapper.add(pendingButton);

        header.add(title, BorderLayout.WEST);
        header.add(requestWrapper, BorderLayout.EAST);
        return header;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 18));
        bar.setBackground(COLOR_BG_GRAY);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER_LIGHT));

        // Chi nhánh
        branchCombo = new JComboBox<>();
        branchCombo.addActionListener(e -> onBranchChanged());
        bar.add(createLabeledControl("CHI NHÁNH", branchCombo, 220));

        // Loại thể thao - Khu vực
        sportTypeAreaCombo = new JComboBox<>();
        sportTypeAreaCombo.addActionListener(e -> {
            if (!isUpdatingCombos) refreshGridFromSelection();
        });
        bar.add(createLabeledControl("LOẠI THỂ THAO - KHU VỰC", sportTypeAreaCombo, 420));

        // Ngày đặt
        bookingDatePicker = createDatePicker();
        bookingDatePicker.addDateChangeListener(e -> refreshGridFromSelection());
        bar.add(createLabeledControl("NGÀY", bookingDatePicker, 180));

        return bar;
    }

    private JPanel createLabeledControl(String label, Component control, int width) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(COLOR_BG_GRAY);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Lexend", Font.BOLD, 11));
        lbl.setForeground(COLOR_TEXT_LABEL);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(width, 38));

        if (control instanceof JComponent jc && !(control instanceof JPanel)) {
            jc.setFont(new Font("Lexend", Font.PLAIN, 13));
            jc.putClientProperty(FlatClientProperties.STYLE, "arc: " + ROUND_ARC);
            jc.setBackground(Color.WHITE);
        }

        wrapper.add(control, BorderLayout.CENTER);
        p.add(lbl, BorderLayout.NORTH);
        p.add(wrapper, BorderLayout.CENTER);
        return p;
    }

    private DatePicker createDatePicker() {
        DatePickerSettings settings = new DatePickerSettings(Locale.forLanguageTag("vi-VN"));
        settings.setFormatForDatesCommonEra("dd/MM/yyyy");
        settings.setAllowKeyboardEditing(false);

        DatePicker picker = new DatePicker(settings);
        picker.setDate(LocalDate.now());
        picker.setBackground(Color.WHITE);
        picker.setFont(new Font("Lexend", Font.PLAIN, 13));

        JTextField txt = picker.getComponentDateTextField();
        txt.putClientProperty(FlatClientProperties.STYLE, "arc: " + ROUND_ARC);
        picker.getComponentToggleCalendarButton().putClientProperty(FlatClientProperties.STYLE, "arc: " + ROUND_ARC);

        return picker;
    }

    private JPanel createBookingGrid() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        panel.setBackground(COLOR_BG_LIGHT);

        String[] cols = buildColumns(openHours);
        model = new DefaultTableModel(new Object[0][cols.length], cols);
        table = new JTable(model) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                paintBlockLabels((Graphics2D) g);
            }
        };

        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(92);
        table.setShowGrid(false);
        table.setGridColor(new Color(235, 235, 235));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Lexend", Font.BOLD, 10));
        header.setForeground(new Color(80, 80, 80));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER_LIGHT));

        table.setDefaultRenderer(Object.class, new BookingGridRenderer());
        configureTableColumns();

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r < 0 || c <= 0) return;

                Object val = table.getValueAt(r, c);
                if (val instanceof BookingCell cell) {
                    handleBookingClicked(cell.slot());
                } else {
                    String courtId = (String) model.getValueAt(r, 0);
                    int hour = openHours.startHourInclusive() + (c - 1);
                    String slotKey = courtId + "-" + hour;

                    if (selectedEmptySlots.contains(slotKey)) {
                        selectedEmptySlots.remove(slotKey);
                    } else {
                        selectedEmptySlots.add(slotKey);
                    }
                    table.repaint();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_LIGHT));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll);
        return panel;
    }

    // TẢI DỮ LIỆU BAN ĐẦU TỪ BACKEND (BẤT ĐỒNG BỘ)
    private void loadInitialOptions() {
        // Lấy thông tin chi nhánh của nhân viên từ session (null nếu là Owner hoặc không xác định)
        String sessionBranchId = SessionManager.getCurrentSession()
                .map(s -> s.isOwner() ? null : s.getBranchId())
                .orElse(null);

        new Thread(() -> {
            try {
                List<BookingBranchOption> branches = controller.getBranchOptions();

                SwingUtilities.invokeLater(() -> {
                    this.openHours = BookingOpenHours.fullDay();
                    rebuildTableColumns();

                    isUpdatingCombos = true;
                    branchCombo.removeAllItems();

                    if (sessionBranchId != null) {
                        // Cashier / Branch Manager: chỉ thêm chi nhánh của họ
                        for (BookingBranchOption b : branches) {
                            if (sessionBranchId.equals(b.branchId())) {
                                branchCombo.addItem(b);
                                break;
                            }
                        }
                        branchCombo.setEnabled(false); // readonly — không cho chọn chi nhánh khác
                    } else {
                        // Owner: thêm tất cả chi nhánh
                        for (BookingBranchOption b : branches) {
                            branchCombo.addItem(b);
                        }
                    }

                    isUpdatingCombos = false;

                    if (branchCombo.getItemCount() > 0) {
                        branchCombo.setSelectedIndex(0); // Trigger onBranchChanged()
                    } else {
                        refreshPendingRequestCount();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // XỬ LÝ KHI THAY ĐỔI CHI NHÁNH -> TẢI DANH SÁCH KHU VỰC TƯƠNG ỨNG TỪ BACKEND
    private void onBranchChanged() {
        selectedEmptySlots.clear();
        BookingBranchOption selectedBranch = (BookingBranchOption) branchCombo.getSelectedItem();
        if (selectedBranch == null) return;
        refreshPendingRequestCount();

        new Thread(() -> {
            try {
                // Gọi API lấy thông tin Khu vực theo Chi nhánh
                List<SportTypeAreaOption> sportTypeAreas = controller.getSportTypeAreaOptionsByBranch(selectedBranch.branchId());

                SwingUtilities.invokeLater(() -> {
                    isUpdatingCombos = true;
                    sportTypeAreaCombo.removeAllItems();
                    for (SportTypeAreaOption sta : sportTypeAreas) {
                        sportTypeAreaCombo.addItem(sta);
                    }
                    isUpdatingCombos = false;

                    // Nếu có dữ liệu khu vực, tự động kích hoạt tải lưới dữ liệu đặt sân
                    if (!sportTypeAreas.isEmpty()) {
                        sportTypeAreaCombo.setSelectedIndex(0);
                    } else {
                        model.setRowCount(0);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    // LẤY DỮ LIỆU ĐẶT SÂN TỪ BACKEND VÀ CẬP NHẬT LÊN LƯỚI (Mấu chốt xử lý của bạn)
    private void refreshGridFromSelection() {
        selectedEmptySlots.clear();

        BookingBranchOption branch = (BookingBranchOption) branchCombo.getSelectedItem();
        SportTypeAreaOption selectedSportTypeArea = (SportTypeAreaOption) sportTypeAreaCombo.getSelectedItem();
        LocalDate date = bookingDatePicker.getDate();
        System.out.println("ngày lấy được:" + date);

        if (branch == null || selectedSportTypeArea == null || date == null) {
            model.setRowCount(0);
            return;
        }

        new Thread(() -> {
            try {
                List<String> areaIds = selectedSportTypeArea.areaIds();
                if (areaIds == null || areaIds.isEmpty()) {
                    SwingUtilities.invokeLater(() -> model.setRowCount(0));
                    return;
                }

                // 1. Khởi tạo danh sách cục bộ (local)
                List<BookingCourtOption> localCourts = new ArrayList<>();
                List<BookingSlotDTO> localBookings = new ArrayList<>();

                for (String areaId : areaIds) {
                    List<BookingCourtOption> courts = controller.getCourtsByArea(areaId);
                    if (courts != null) localCourts.addAll(courts);

                    addBookingsByStatus(localBookings, branch.branchId(), areaId, date, STATUS_CONFIRMED);
                    addBookingsByStatus(localBookings, branch.branchId(), areaId, date, STATUS_IN_USE);
                }
                // 2. Truyền chính xác biến local vào luồng EDT
                SwingUtilities.invokeLater(() -> {
                    populateGrid(localCourts, localBookings, openHours); // Đã sửa ở đây
                    table.revalidate();
                    table.repaint();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> model.setRowCount(0));
            }
        }).start();
    }

    private void addBookingsByStatus(List<BookingSlotDTO> target,
                                     String branchId,
                                     String areaId,
                                     LocalDate date,
                                     String status) {
        List<BookingSlotDTO> bookings = controller.getBookings(branchId, areaId, date, status);
        if (bookings == null) return;

        List<BookingSlotDTO> filteredBookings = bookings.stream()
                .filter(b -> b.bookingDate() != null && b.bookingDate().isEqual(date))
                .toList();

        target.addAll(filteredBookings);
    }

    private void populateGrid(List<BookingCourtOption> courts, List<BookingSlotDTO> bookings, BookingOpenHours hours) {
        model.setRowCount(0);
        int start = hours.startHourInclusive();
        int end = hours.endHourExclusive();
        List<BookingBlock> bookingBlocks = buildBookingBlocks(bookings);

        for (BookingCourtOption court : courts) {
            Object[] row = new Object[(end - start) + 1];
            row[0] = court.courtId(); // Cột 0: Tên mã sân

            for (int h = start; h < end; h++) {
                final int hour = h;
                BookingBlock block = bookingBlocks.stream()
                        .filter(b -> b.courtId().equals(court.courtId()) && hour >= b.startHour() && hour < b.endHour())
                        .findFirst().orElse(null);

                row[(h - start) + 1] = (block == null) ? null : new BookingCell(block);
            }
            model.addRow(row);
        }
    }

    private List<BookingBlock> buildBookingBlocks(List<BookingSlotDTO> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return List.of();
        }

        Map<String, List<BookingSlotDTO>> grouped = new LinkedHashMap<>();
        bookings.stream()
                .filter(slot -> slot != null && slot.courtId() != null && slot.invoiceId() != null)
                .sorted(Comparator.comparing(BookingSlotDTO::courtId, Comparator.nullsLast(String::compareTo))
                        .thenComparing(BookingSlotDTO::invoiceId, Comparator.nullsLast(String::compareTo))
                        .thenComparing(BookingSlotDTO::bookingDate, Comparator.nullsLast(LocalDate::compareTo))
                        .thenComparingInt(BookingSlotDTO::startHour))
                .forEach(slot -> {
                    String key = slot.courtId() + "|" + slot.invoiceId() + "|" + slot.bookingDate();
                    grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(slot);
                });

        List<BookingBlock> blocks = new ArrayList<>();
        for (List<BookingSlotDTO> slots : grouped.values()) {
            BookingBlock current = null;
            for (BookingSlotDTO slot : slots) {
                if (current == null || slot.startHour() > current.endHour()) {
                    current = new BookingBlock(slot, slot.startHour(), slot.endHour(), 1);
                    blocks.add(current);
                } else {
                    current.merge(slot);
                }
            }
        }
        return blocks;
    }

    private void rebuildTableColumns() {
        String[] cols = buildColumns(openHours);
        model.setColumnIdentifiers(cols);
        configureTableColumns();
    }

    private void configureTableColumns() {
        if (table == null || table.getColumnModel().getColumnCount() == 0) {
            return;
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(104);
        table.getColumnModel().getColumn(0).setMinWidth(96);
        for (int i = 1; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(72);
            table.getColumnModel().getColumn(i).setMinWidth(64);
        }
    }

    private String[] buildColumns(BookingOpenHours hours) {
        List<String> cols = new ArrayList<>();
        cols.add("SÂN CON");
        for (int h = hours.startHourInclusive(); h < hours.endHourExclusive(); h++) {
            cols.add(String.format("%02d:00", h));
        }
        return cols.toArray(new String[0]);
    }

    private void refreshPendingRequestCount() {
        String branchId = selectedBranchIdOrNull();
        new Thread(() -> {
            int count = controller.countPendingDepositRequests(branchId);
            SwingUtilities.invokeLater(() -> {
                if (pendingButton != null) {
                    pendingButton.setText("Yêu cầu đặt sân (" + count + ")");
                }
            });
        }).start();
    }

    // Vẽ text thông tin khách hàng đè lên toàn bộ block (kể cả khi span nhiều ô giờ)
    private void paintBlockLabels(Graphics2D g2) {
        if (model == null || model.getRowCount() == 0 || table == null) return;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int colCount = table.getColumnCount();
        int rowCount = model.getRowCount();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 1; col < colCount; col++) {
                Object val = model.getValueAt(row, col);
                if (!(val instanceof BookingCell bc)) continue;

                int hour = openHours.startHourInclusive() + (col - 1);
                if (hour != bc.startHour()) continue;

                java.awt.Rectangle startRect = table.getCellRect(row, col, false);
                int blockCols = bc.endHour() - bc.startHour();
                int totalWidth = blockCols * startRect.width;

                int blockX = startRect.x + BOOKING_BLOCK_PAD_X;
                int blockY = startRect.y + BOOKING_BLOCK_PAD_Y;
                int blockW = Math.max(0, totalWidth - BOOKING_BLOCK_PAD_X * 2);
                int blockH = Math.max(0, startRect.height - BOOKING_BLOCK_PAD_Y * 2);

                java.awt.Shape oldClip = g2.getClip();
                g2.setClip(blockX, blockY, blockW, blockH);

                g2.setColor(Color.WHITE);
                Font titleFont = new Font("Lexend", Font.BOLD, 10);
                Font metaFont = new Font("Lexend", Font.PLAIN, 9);
                String[] lines = {
                        safe(bc.customerName()),
                        safe(bc.customerPhone()),
                        safe(bc.areaId()) + " - " + safe(bc.courtId()) + " - " + safe(bc.sportTypeName()),
                        bc.slotCount() + " slot"
                };
                Font[] fonts = {titleFont, titleFont, metaFont, metaFont};
                int lineGap = 2;
                int totalTextHeight = 0;
                for (Font font : fonts) {
                    totalTextHeight += g2.getFontMetrics(font).getHeight();
                }
                totalTextHeight += lineGap * (lines.length - 1);

                int y = blockY + Math.max(0, (blockH - totalTextHeight) / 2);
                for (int i = 0; i < lines.length; i++) {
                    g2.setFont(fonts[i]);
                    FontMetrics fm = g2.getFontMetrics();
                    String line = fitText(lines[i], fm, blockW - 8);
                    int x = blockX + Math.max(4, (blockW - fm.stringWidth(line)) / 2);
                    y += fm.getAscent();
                    g2.drawString(line, x, y);
                    y += fm.getDescent() + lineGap;
                }

                g2.setClip(oldClip);
            }
        }
    }

    private String selectedBranchIdOrNull() {
        if (branchCombo == null) {
            return null;
        }
        Object selected = branchCombo.getSelectedItem();
        if (selected instanceof BookingBranchOption branch) {
            return branch.branchId();
        }
        return null;
    }

    private void showPendingRequestsDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        PendingRequestsDialog dialog = new PendingRequestsDialog(owner, selectedBranchIdOrNull());
        dialog.setVisible(true);
    }

    private BookingSlotDTO toSlot(PendingBookingRequestDTO request) {
        return new BookingSlotDTO(
                request.bookingDetailId(),
                request.invoiceId(),
                request.courtSummary(),
                "",
                request.sportTypeName(),
                request.customerName(),
                request.customerPhone(),
                request.startHour(),
                request.endHour(),
                request.bookingDate()
        );
    }

    private String formatDate(LocalDate date) {
        return date == null ? "--" : date.format(DATE_FORMATTER);
    }

    private String formatHourRange(int startHour, int endHour) {
        return String.format("%02d:00 - %02d:00", startHour, endHour);
    }

    private String money(double value) {
        return MONEY_FORMAT.format(value) + " VND";
    }

    private void handleBookingClicked(BookingSlotDTO slot) {
        if (slot != null && slot.invoiceId() != null && !slot.invoiceId().isEmpty()) {
            BookingDetailPanel detailPanel = new BookingDetailPanel(slot);
            detailPanel.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Không có thông tin chi tiết đặt sân cho ô này.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    private static String fitText(String value, FontMetrics fm, int maxWidth) {
        String text = safe(value);
        if (maxWidth <= 0 || fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        if (ellipsisWidth >= maxWidth) {
            return "";
        }
        int end = text.length();
        while (end > 0 && fm.stringWidth(text.substring(0, end)) + ellipsisWidth > maxWidth) {
            end--;
        }
        return end <= 0 ? ellipsis : text.substring(0, end) + ellipsis;
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        private RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private final class PendingRequestsDialog extends JDialog {
        private final String branchId;
        private final JTextField phoneSearchField = new JTextField();
        private final DatePicker requestDatePicker = createDatePicker();
        private final JCheckBox allDatesCheck = new JCheckBox("Tất cả ngày", true);
        private final JPanel requestListPanel = new JPanel();
        private final JLabel requestResultLabel = new JLabel("Đang tải...");

        private PendingRequestsDialog(Window owner, String branchId) {
            super(owner, "Yêu cầu đặt sân", ModalityType.MODELESS);
            this.branchId = branchId;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(buildContent());
            setSize(820, 680);
            setMinimumSize(new Dimension(700, 560));
            setLocationRelativeTo(owner);
            loadRequests();
        }

        private JComponent buildContent() {
            JPanel root = new JPanel(new BorderLayout(0, 18));
            root.setBackground(new Color(249, 249, 252));
            root.setBorder(new EmptyBorder(28, 32, 28, 32));

            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("YÊU CẦU ĐẶT SÂN");
            title.setFont(new Font("Lexend", Font.BOLD, 28));
            title.setForeground(COLOR_TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(title);
            top.add(Box.createVerticalStrut(18));

            JPanel searchPanel = new JPanel(new BorderLayout(12, 0));
            searchPanel.setOpaque(false);
            searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

            phoneSearchField.setFont(new Font("Lexend", Font.PLAIN, 14));
            phoneSearchField.setBackground(Color.WHITE);
            phoneSearchField.setBorder(new EmptyBorder(0, 14, 0, 14));
            phoneSearchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập số điện thoại khách hàng");
            phoneSearchField.putClientProperty(FlatClientProperties.STYLE, "arc: 18");
            phoneSearchField.addActionListener(e -> loadRequests());

            JButton searchButton = smallPillButton("Tìm kiếm", new Color(57, 255, 20), COLOR_PRIMARY_GREEN);
            searchButton.addActionListener(e -> loadRequests());
            searchPanel.add(phoneSearchField, BorderLayout.CENTER);
            searchPanel.add(searchButton, BorderLayout.EAST);
            top.add(searchPanel);
            top.add(Box.createVerticalStrut(14));

            JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            filters.setOpaque(false);
            filters.setAlignmentX(Component.LEFT_ALIGNMENT);
            filters.add(filterLabel("Ngày đặt"));
            requestDatePicker.setPreferredSize(new Dimension(170, 38));
            filters.add(requestDatePicker);

            allDatesCheck.setOpaque(false);
            allDatesCheck.setFont(new Font("Lexend", Font.PLAIN, 13));
            allDatesCheck.setForeground(COLOR_TEXT_LABEL);
            allDatesCheck.addActionListener(e -> {
                requestDatePicker.setEnabled(!allDatesCheck.isSelected());
                loadRequests();
            });
            requestDatePicker.setEnabled(false);
            requestDatePicker.addDateChangeListener(e -> {
                if (!allDatesCheck.isSelected()) {
                    loadRequests();
                }
            });
            filters.add(allDatesCheck);

            JButton reloadButton = smallPillButton("Tải lại", Color.WHITE, COLOR_TEXT_DARK);
            reloadButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COLOR_BORDER_LIGHT),
                    new EmptyBorder(8, 16, 8, 16)));
            reloadButton.addActionListener(e -> loadRequests());
            filters.add(reloadButton);
            top.add(filters);

            root.add(top, BorderLayout.NORTH);

            RoundedPanel listBox = new RoundedPanel(24, Color.WHITE);
            listBox.setLayout(new BorderLayout(0, 12));
            listBox.setBorder(new EmptyBorder(18, 18, 18, 18));

            requestResultLabel.setFont(new Font("Lexend", Font.BOLD, 14));
            requestResultLabel.setForeground(COLOR_TEXT_LABEL);
            listBox.add(requestResultLabel, BorderLayout.NORTH);

            requestListPanel.setLayout(new BoxLayout(requestListPanel, BoxLayout.Y_AXIS));
            requestListPanel.setOpaque(false);

            JScrollPane scroll = new JScrollPane(requestListPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.getVerticalScrollBar().setUnitIncrement(18);
            listBox.add(scroll, BorderLayout.CENTER);
            root.add(listBox, BorderLayout.CENTER);

            return root;
        }

        private JLabel filterLabel(String text) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Lexend", Font.BOLD, 12));
            label.setForeground(COLOR_TEXT_LABEL);
            return label;
        }

        private JButton smallPillButton(String text, Color background, Color foreground) {
            JButton button = new JButton(text);
            button.setFont(new Font("Lexend", Font.BOLD, 13));
            button.setForeground(foreground);
            button.setBackground(background);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(9, 18, 9, 18));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.putClientProperty(FlatClientProperties.STYLE, "arc: 999");
            return button;
        }

        private void loadRequests() {
            requestResultLabel.setText("Đang tải yêu cầu...");
            requestListPanel.removeAll();
            requestListPanel.add(emptyState("Đang tải dữ liệu"));
            requestListPanel.revalidate();
            requestListPanel.repaint();

            LocalDate filterDate = allDatesCheck.isSelected() ? null : requestDatePicker.getDate();
            String phone = phoneSearchField.getText();

            new Thread(() -> {
                try {
                    List<PendingBookingRequestDTO> requests =
                            controller.getPendingDepositRequests(branchId, filterDate, phone);
                    SwingUtilities.invokeLater(() -> renderRequests(requests));
                } catch (RuntimeException ex) {
                    SwingUtilities.invokeLater(() -> {
                        requestResultLabel.setText("Không thể tải yêu cầu");
                        requestListPanel.removeAll();
                        requestListPanel.add(emptyState(ex.getMessage()));
                        requestListPanel.revalidate();
                        requestListPanel.repaint();
                    });
                }
            }).start();
        }

        private void renderRequests(List<PendingBookingRequestDTO> requests) {
            requestListPanel.removeAll();
            int count = requests == null ? 0 : requests.size();
            requestResultLabel.setText(count + " yêu cầu đã cọc chờ xác nhận");

            if (count == 0) {
                requestListPanel.add(emptyState("Không có yêu cầu phù hợp"));
            } else {
                for (PendingBookingRequestDTO request : requests) {
                    requestListPanel.add(requestCard(request));
                    requestListPanel.add(Box.createVerticalStrut(12));
                }
            }

            requestListPanel.revalidate();
            requestListPanel.repaint();
        }

        private JComponent emptyState(String message) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            panel.setPreferredSize(new Dimension(0, 160));
            JLabel label = new JLabel(message == null || message.isBlank() ? "Không có dữ liệu" : message);
            label.setFont(new Font("Lexend", Font.PLAIN, 14));
            label.setForeground(new Color(113, 113, 122));
            panel.add(label);
            return panel;
        }

        private JComponent requestCard(PendingBookingRequestDTO request) {
            RoundedPanel card = new RoundedPanel(20, new Color(249, 249, 252));
            card.setLayout(new BorderLayout(18, 0));
            card.setBorder(new EmptyBorder(16, 18, 16, 18));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JLabel customer = new JLabel(safe(request.customerName()) + "  •  " + safe(request.customerPhone()));
            customer.setFont(new Font("Lexend", Font.BOLD, 15));
            customer.setForeground(COLOR_TEXT_DARK);
            info.add(customer);
            info.add(Box.createVerticalStrut(6));

            JLabel bookingLine = new JLabel(formatDate(request.bookingDate())
                    + " | " + formatHourRange(request.startHour(), request.endHour())
                    + " | " + safe(request.sportTypeName())
                    + " | " + safe(request.courtSummary())
                    + " | " + request.slotCount() + " slot");
            bookingLine.setFont(new Font("Lexend", Font.PLAIN, 13));
            bookingLine.setForeground(COLOR_TEXT_LABEL);
            bookingLine.setToolTipText(safe(request.sportTypeName()));
            info.add(bookingLine);
            info.add(Box.createVerticalStrut(6));

            JLabel moneyLine = new JLabel("Cọc: " + money(request.depositAmount()) + "  •  Tổng: " + money(request.totalAmount()));
            moneyLine.setFont(new Font("Lexend", Font.BOLD, 13));
            moneyLine.setForeground(COLOR_PRIMARY_GREEN);
            info.add(moneyLine);

            JPanel right = new JPanel(new BorderLayout(0, 10));
            right.setOpaque(false);
            JLabel status = new JLabel(STATUS_DEPOSITED, SwingConstants.CENTER);
            status.setFont(new Font("Lexend", Font.BOLD, 12));
            status.setForeground(new Color(180, 83, 9));
            status.setOpaque(true);
            status.setBackground(new Color(254, 243, 199));
            status.setBorder(new EmptyBorder(6, 12, 6, 12));

            JButton detailButton = smallPillButton("Chi tiết", new Color(57, 255, 20), COLOR_PRIMARY_GREEN);
            detailButton.addActionListener(e -> openPendingDetail(request));
            right.add(status, BorderLayout.NORTH);
            right.add(detailButton, BorderLayout.SOUTH);

            card.add(info, BorderLayout.CENTER);
            card.add(right, BorderLayout.EAST);
            return card;
        }

        private void openPendingDetail(PendingBookingRequestDTO request) {
            BookingDetailPanel detailPanel = new BookingDetailPanel(toSlot(request), true, () -> {
                loadRequests();
                refreshPendingRequestCount();
                refreshGridFromSelection();
            });
            detailPanel.setLocationRelativeTo(this);
            detailPanel.setVisible(true);
        }
    }

    private static final class BookingBlock {
        private final BookingSlotDTO firstSlot;
        private int startHour;
        private int endHour;
        private int slotCount;

        private BookingBlock(BookingSlotDTO firstSlot, int startHour, int endHour, int slotCount) {
            this.firstSlot = firstSlot;
            this.startHour = startHour;
            this.endHour = endHour;
            this.slotCount = slotCount;
        }

        private void merge(BookingSlotDTO slot) {
            this.startHour = Math.min(startHour, slot.startHour());
            this.endHour = Math.max(endHour, slot.endHour());
            this.slotCount++;
        }

        private BookingSlotDTO slot() {
            return firstSlot;
        }

        private String courtId() {
            return firstSlot.courtId();
        }

        private String customerName() {
            return firstSlot.customerName();
        }

        private String customerPhone() {
            return firstSlot.customerPhone();
        }

        private String sportTypeName() {
            return firstSlot.sportTypeName();
        }

        private String areaId() {
            return firstSlot.areaId();
        }

        private int startHour() {
            return startHour;
        }

        private int endHour() {
            return endHour;
        }

        private int slotCount() {
            return slotCount;
        }
    }

    private static class BookingCell {
        final BookingBlock block;

        BookingCell(BookingBlock block) {
            this.block = block;
        }

        @Override
        public String toString() {
            return "<html>" + safe(block.customerName()) + "<br>" + safe(block.customerPhone()) + "</html>";
        }

        BookingSlotDTO slot() {
            return block.slot();
        }

        int startHour() {
            return block.startHour();
        }

        int endHour() {
            return block.endHour();
        }

        String customerName() {
            return block.customerName();
        }

        String customerPhone() {
            return block.customerPhone();
        }

        String areaId() {
            return block.areaId();
        }

        String courtId() {
            return block.courtId();
        }

        String sportTypeName() {
            return block.sportTypeName();
        }

        int slotCount() {
            return block.slotCount();
        }
    }

    private class BookingGridRenderer extends DefaultTableCellRenderer {
        private int row, col;
        private Object val;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            this.row = r;
            this.col = c;
            this.val = v;
            setHorizontalAlignment(CENTER);
            if (c == 0) {
                setBackground(COLOR_BG_GRAY);
                setFont(new Font("Lexend", Font.BOLD, 13));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            if (col > 0) {
                String courtId = (String) model.getValueAt(row, 0);
                int hour = openHours.startHourInclusive() + (col - 1);

                // 1. Luôn tô nền trắng mặc định cho ô trước
                g2.setColor(getBackground());
                g2.fillRect(0, 0, w, h);

                // 2. Nếu ô này chứa dữ liệu đặt sân từ backend, tiến hành vẽ dải màu xanh dương
                if (val instanceof BookingCell bc) {
                    g2.setColor(COLOR_BOOKED_BLUE);

                    boolean isStartHour = (hour == bc.startHour());
                    boolean isEndHour = (hour == bc.endHour() - 1);

                    int arc = BOOKING_BLOCK_ARC;
                    int yOffset = BOOKING_BLOCK_PAD_Y;
                    int rectH = h - (yOffset * 2);

                    if (isStartHour && isEndHour) {
                        g2.fill(new RoundRectangle2D.Double(BOOKING_BLOCK_PAD_X, yOffset,
                                w - BOOKING_BLOCK_PAD_X * 2, rectH, arc, arc));
                    } else if (isStartHour) {
                        g2.fillRoundRect(BOOKING_BLOCK_PAD_X, yOffset,
                                w - BOOKING_BLOCK_PAD_X + arc, rectH, arc, arc);
                        g2.fillRect(w - arc / 2, yOffset, arc / 2, rectH);
                    } else if (isEndHour) {
                        g2.fillRoundRect(-arc, yOffset,
                                w + arc - BOOKING_BLOCK_PAD_X, rectH, arc, arc);
                        g2.fillRect(0, yOffset, arc / 2, rectH);
                    } else {
                        g2.fillRect(0, yOffset, w, rectH);
                    }

                    // Text được vẽ bởi paintBlockLabels() sau khi tất cả ô được render xong

                    // 3. Viền liền mạch: chỉ vẽ cạnh trên/dưới cho ô giữa block,
                    //    vẽ thêm cạnh trái ở ô đầu block và cạnh phải ở ô cuối block
                    g2.setColor(new Color(230, 230, 230));
                    g2.drawLine(0, 0, w - 1, 0);
                    g2.drawLine(0, h - 1, w - 1, h - 1);
                    if (isStartHour) g2.drawLine(0, 0, 0, h - 1);
                    if (isEndHour) g2.drawLine(w - 1, 0, w - 1, h - 1);
                } else {
                    // 3. Ô trống: vẽ viền đầy đủ bốn cạnh
                    g2.setColor(new Color(230, 230, 230));
                    g2.drawRect(0, 0, w - 1, h - 1);
                }

            } else {
                // Vẽ cột 0 (Cột hiển thị tên SÂN CON)
                g2.setColor(getBackground());
                g2.fillRect(0, 0, w, h);

                // Kẻ viền cho cột 0
                g2.setColor(new Color(220, 220, 225));
                g2.drawRect(0, 0, w - 1, h - 1);

                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, h / 2 + 5);
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Button.arc", ROUND_ARC);
            UIManager.put("Component.arc", ROUND_ARC);
            UIManager.put("ComboBox.arc", ROUND_ARC);
            UIManager.put("TextComponent.arc", ROUND_ARC);
        } catch (Exception e) {
            e.printStackTrace();
        }


        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hệ Thống Đặt Sân");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new BookingRequest());
            frame.setSize(1300, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
