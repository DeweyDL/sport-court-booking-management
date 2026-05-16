package com.sportcourt.modules.booking_management.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

    private final BookingRequestController controller = new BookingRequestController();

    private JComboBox<BookingBranchOption> branchCombo;
    private JComboBox<SportTypeAreaOption> sportTypeAreaCombo;
    private DatePicker bookingDatePicker;

    private BookingOpenHours openHours = BookingOpenHours.defaultHours();
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
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 15));
        header.setBackground(COLOR_PRIMARY_GREEN);

        JLabel subTitle = new JLabel("Yêu cầu đặt sân");
        subTitle.setFont(new Font("Lexend", Font.BOLD, 16));
        subTitle.setForeground(new Color(248, 250, 252));
        subTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE));

        header.add(Box.createRigidArea(new Dimension(20, 0)));
        header.add(subTitle, BorderLayout.WEST);
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
        };

        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(90);
        table.setShowGrid(true);
        table.setGridColor(new Color(235, 235, 235));
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Lexend", Font.BOLD, 10));
        header.setForeground(new Color(80, 80, 80));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER_LIGHT));

        table.setDefaultRenderer(Object.class, new BookingGridRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(100);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r < 0 || c <= 0) return;

                Object val = table.getValueAt(r, c);
                if (val instanceof BookingCell cell) {
                    handleBookingClicked(cell.slot);
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
        new Thread(() -> {
            try {
                // Gọi API backend lấy cấu hình giờ mở cửa và DS chi nhánh
                BookingOpenHours hours = controller.getOpenHours();
                List<BookingBranchOption> branches = controller.getBranchOptions();

                // Cập nhật lại giao diện trên EDT sau khi đã nhận được dữ liệu thành công
                SwingUtilities.invokeLater(() -> {
                    this.openHours = hours;
                    rebuildTableColumns();

                    isUpdatingCombos = true;
                    branchCombo.removeAllItems();
                    for (BookingBranchOption b : branches) {
                        branchCombo.addItem(b);
                    }
                    isUpdatingCombos = false;

                    if (!branches.isEmpty()) {
                        branchCombo.setSelectedIndex(0); // Trigger hàm onBranchChanged()
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

                    List<BookingSlotDTO> bookings = controller.getBookings(branch.branchId(), areaId, date, "ĐÃ XÁC NHẬN");

                    if (bookings != null) {
                        // Lọc chỉ lấy những lịch đặt có ngày trùng với ngày chọn từ DatePicker
                        List<BookingSlotDTO> filteredBookings = bookings.stream()
                                .filter(b -> b.bookingDate() != null && b.bookingDate().isEqual(date))
                                .toList();

                        localBookings.addAll(filteredBookings);
                    }
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

    private void populateGrid(List<BookingCourtOption> courts, List<BookingSlotDTO> bookings, BookingOpenHours hours) {
        model.setRowCount(0);
        int start = hours.startHourInclusive();
        int end = hours.endHourExclusive();

        for (BookingCourtOption court : courts) {
            Object[] row = new Object[(end - start) + 1];
            row[0] = court.courtId(); // Cột 0: Tên mã sân

            for (int h = start; h < end; h++) {
                final int hour = h;
                // Kiểm tra xem giờ hiện tại của sân này có khớp với điều kiện đặt từ backend không
                BookingSlotDTO slot = bookings.stream()
                        .filter(b -> b.courtId().equals(court.courtId()) && hour >= b.startHour() && hour < b.endHour())
                        .findFirst().orElse(null);

                // Nếu khớp điều kiện -> Tạo class bọc BookingCell, nếu không -> Để null (ô trống)
                row[(h - start) + 1] = (slot == null) ? null : new BookingCell(slot);
            }
            model.addRow(row);
        }
    }

    private void rebuildTableColumns() {
        String[] cols = buildColumns(openHours);
        model.setColumnIdentifiers(cols);
        if (table != null && table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(100);
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

    private static class BookingCell {
        final BookingSlotDTO slot;

        BookingCell(BookingSlotDTO s) {
            this.slot = s;
        }

        @Override
        public String toString() {
            return "<html>" + safe(slot.customerName()) + "<br>" + safe(slot.customerPhone()) + "</html>";
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

                    boolean isStartHour = (hour == bc.slot.startHour());
                    boolean isEndHour = (hour == bc.slot.endHour() - 1);

                    int arc = 30;
                    int yOffset = 15;
                    int rectH = h - (yOffset * 2);

                    if (isStartHour && isEndHour) {
                        g2.fill(new RoundRectangle2D.Double(5, yOffset, w - 10, rectH, arc, arc));
                    } else if (isStartHour) {
                        g2.fillRoundRect(5, yOffset, w + 20, rectH, arc, arc);
                    } else if (isEndHour) {
                        g2.fillRoundRect(-20, yOffset, w + 15, rectH, arc, arc);
                    } else {
                        g2.fillRect(0, yOffset, w, rectH);
                    }

                    if (isStartHour) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Lexend", Font.BOLD, 9));

                        String line1 = bc.slot.customerName();
                        String line2 = bc.slot.customerPhone();
                        String line3 = bc.slot.areaId() + " - " + bc.slot.courtId() + " - " + bc.slot.sportTypeName();

                        int textX = 15;
                        g2.drawString(line1, textX, h / 2 - 14);
                        g2.drawString(line2, textX, h / 2 );

                        g2.setFont(new Font("Lexend", Font.PLAIN, 9));
                        g2.drawString(line3, textX, h / 2 + 14);
                    }
                }

                // 3. THÊM TẠI ĐÂY: Kẻ đường viền bao quanh ô trống/ô đặt để lưới bảng rõ ràng
                g2.setColor(new Color(230, 230, 230)); // Màu xám mảnh tinh tế, không bị thô
                g2.drawRect(0, 0, w - 1, h - 1);

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