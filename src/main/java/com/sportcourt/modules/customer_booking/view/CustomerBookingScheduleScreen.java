package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer_booking.controller.CustomerBookingController;
import com.sportcourt.modules.customer_booking.dto.BookingSlotStatus;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSchedule;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.SlotStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public class CustomerBookingScheduleScreen extends JPanel {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private static final Color CELL_BORDER_COLOR = new Color(186, 204, 176, 60);
    private static final Color LABEL_COL_BG      = new Color(243, 243, 246);
    private static final Color CORNER_BG         = new Color(232, 232, 234);
    private static final Color BOOKED_BG         = new Color(161, 161, 170);
    private static final Color SELECTED_BG       = new Color(59, 130, 246);
    private static final Color HOVER_BG          = new Color(240, 249, 240);
    private static final Color EMPTY_CELL_BG     = new Color(249, 249, 252);

    private final CustomerBookingController controller;
    private final Runnable onBack;
    private final Consumer<List<SlotStatus>> onConfirm;

    private final JLabel titleLabel    = label("ĐẶT SÂN", bold(28f), Color.WHITE);
    private final JLabel areaInfoLabel = label("--", bold(13f), TEXT_DARK);
    private final JLabel slotLabel     = label("Chưa chọn khung giờ", bold(14f), GREEN_DARK);
    private final JLabel totalLabel    = label("Tổng tiền: 0 VND", bold(14f), GREEN_DARK);
    private final JSpinner dateSpinner = new JSpinner(
            new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));

    private JPanel matrixPanel;
    private CourtSearchResult currentCourt;
    private LocalDate bookingDate = LocalDate.now();
    private List<CourtSchedule> schedules = List.of();
    private List<Integer> hours = List.of();
    private final List<SlotStatus> selectedSlots = new ArrayList<>();
    private boolean adjustingDate;

    public CustomerBookingScheduleScreen(CustomerBookingController controller,
                                         Runnable onBack,
                                         Consumer<List<SlotStatus>> onConfirm) {
        this.controller = controller;
        this.onBack = onBack;
        this.onConfirm = onConfirm;
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    public void showCourt(BranchOption branch, CourtSearchResult court) {
        showCourt(branch, court, LocalDate.now());
    }

    public void showCourt(BranchOption branch, CourtSearchResult court, LocalDate initialDate) {
        currentCourt  = court;
        selectedSlots.clear();
        bookingDate   = initialDate == null ? LocalDate.now() : initialDate;
        adjustingDate = true;
        dateSpinner.setValue(Date.from(bookingDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        adjustingDate = false;
        titleLabel.setText("ĐẶT SÂN - " + court.areaId());
        areaInfoLabel.setText(branch.branchName() + " | " + court.sportTypeName());
        updateFooter();
        loadMatrix();
    }

    public void refreshCurrentSchedule() {
        loadMatrix();
    }

    // ── header ───────────────────────────────────────────────────────────────

    private JComponent buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(GREEN_DARK);
        h.setBorder(new EmptyBorder(s(16), s(24), s(16), s(24)));
        h.add(titleLabel, BorderLayout.WEST);
        return h;
    }

    // ── center (filter strip + scrollable matrix) ─────────────────────────────

    private JComponent buildCenter() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(buildFilterStrip(), BorderLayout.NORTH);

        matrixPanel = new JPanel();
        matrixPanel.setLayout(new BoxLayout(matrixPanel, BoxLayout.Y_AXIS));
        matrixPanel.setBackground(Color.WHITE);

        JPanel matrixWrapper = new JPanel(new BorderLayout());
        matrixWrapper.setBackground(Color.WHITE);
        matrixWrapper.add(matrixPanel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(matrixWrapper);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getHorizontalScrollBar().setUnitIncrement(s(20));
        scroll.getVerticalScrollBar().setUnitIncrement(s(20));

        JPanel bg = new JPanel(new BorderLayout());
        bg.setBackground(new Color(249, 249, 252));
        bg.setBorder(new EmptyBorder(s(16), s(16), s(16), s(16)));
        bg.add(scroll, BorderLayout.CENTER);

        center.add(bg, BorderLayout.CENTER);
        return center;
    }

    // ── filter strip ─────────────────────────────────────────────────────────

    private JComponent buildFilterStrip() {
        JPanel strip = new JPanel(new BorderLayout(s(24), 0));
        strip.setBackground(new Color(243, 243, 246));
        strip.setBorder(new EmptyBorder(s(18), s(24), s(18), s(24)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(label("KHU VỰC - LOẠI THỂ THAO", bold(12f), TEXT_OLIVE));
        left.add(Box.createVerticalStrut(s(6)));
        left.add(areaInfoLabel);
        strip.add(left, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(12), 0));
        right.setOpaque(false);
        right.add(buildLegend());
        right.add(label("Ngày đặt", bold(12f), TEXT_OLIVE));

        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setPreferredSize(new Dimension(s(140), s(36)));
        dateSpinner.addChangeListener(e -> {
            if (!adjustingDate) {
                bookingDate = ((Date) dateSpinner.getValue())
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                selectedSlots.clear();
                updateFooter();
                loadMatrix();
            }
        });
        right.add(dateSpinner);

        JButton reload = pillButton("Tải lại", Color.WHITE, GREEN_DARK);
        reload.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(GREEN_DARK, s(999)),
                new EmptyBorder(s(7), s(16), s(7), s(16))));
        reload.addActionListener(e -> loadMatrix());
        right.add(reload);

        strip.add(right, BorderLayout.EAST);
        return strip;
    }

    private JComponent buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, s(10), 0));
        legend.setBackground(new Color(232, 232, 234, 128));
        legend.setBorder(new EmptyBorder(s(8), s(12), s(8), s(12)));
        legend.add(legendDot(Color.WHITE, true));
        legend.add(label("Trống", regular(12f), TEXT_DARK));
        legend.add(Box.createHorizontalStrut(s(4)));
        legend.add(legendDot(SELECTED_BG, false));
        legend.add(label("Đang chọn", regular(12f), TEXT_DARK));
        legend.add(Box.createHorizontalStrut(s(4)));
        legend.add(legendDot(BOOKED_BG, false));
        legend.add(label("Đã hết", regular(12f), TEXT_DARK));
        return legend;
    }

    private JLabel legendDot(Color fill, boolean bordered) {
        JLabel dot = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fillOval(0, 0, getWidth(), getHeight());
                if (bordered) {
                    g2.setColor(new Color(186, 204, 176, 128));
                    g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                }
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(s(12), s(12)));
        dot.setOpaque(false);
        return dot;
    }

    // ── footer ───────────────────────────────────────────────────────────────

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(s(16), 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(186, 204, 176, 50)),
                new EmptyBorder(s(10), s(16), s(10), s(16))));
        footer.setPreferredSize(new Dimension(0, s(70)));
        footer.setMinimumSize(new Dimension(0, s(70)));

        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, s(18), 0));
        summary.setOpaque(false);
        summary.add(slotLabel);
        summary.add(totalLabel);
        footer.add(summary, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(10), 0));
        actions.setOpaque(false);

        JButton reset = pillButton("Quay lại", SOFT_GRAY, TEXT_DARK);
        reset.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(SOFT_GRAY, s(999)),
                new EmptyBorder(s(9), s(24), s(9), s(24))));
        reset.setPreferredSize(new Dimension(s(124), s(42)));
        reset.addActionListener(e -> onBack.run());

        JButton book = pillButton("Tiếp theo", GREEN, GREEN_DARK);
        book.setPreferredSize(new Dimension(s(112), s(40)));
        book.addActionListener(e -> confirmSelection());

        actions.add(reset);
        actions.add(book);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    // ── data loading ──────────────────────────────────────────────────────────

    private void loadMatrix() {
        if (currentCourt == null) {
            renderEmpty("Chưa chọn sân.");
            return;
        }
        try {
            schedules = controller.loadAreaSchedule(currentCourt.areaId(), bookingDate);
            hours     = controller.loadPriceHours(currentCourt.areaId());
            pruneUnavailableSelections();
            renderMatrix();
        } catch (RuntimeException e) {
            renderEmpty("Không thể tải lịch: "
                    + (e.getMessage() != null ? e.getMessage() : ""));
        }
    }

    // ── matrix rendering ──────────────────────────────────────────────────────

    private void renderMatrix() {
        matrixPanel.removeAll();

        if (hours.isEmpty() || schedules.isEmpty()) {
            renderEmpty("Khu vực này chưa có bảng giá hoặc sân nào.");
            return;
        }

        int courtColW = s(78);
        int cellW     = s(64);
        int totalW    = courtColW + hours.size() * cellW;

        matrixPanel.add(buildHeaderRow(courtColW, cellW, totalW));
        for (CourtSchedule cs : schedules) {
            matrixPanel.add(buildCourtRow(cs, courtColW, cellW, totalW));
        }

        matrixPanel.revalidate();
        matrixPanel.repaint();
    }

    private void renderEmpty(String msg) {
        matrixPanel.removeAll();
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(Color.WHITE);
        placeholder.setPreferredSize(new Dimension(s(600), s(200)));
        placeholder.add(label(msg, bold(14f), TEXT_MUTED));
        matrixPanel.add(placeholder);
        matrixPanel.revalidate();
        matrixPanel.repaint();
    }

    // ── header row (COURTS corner + hour labels) ──────────────────────────────

    private JPanel buildHeaderRow(int courtColW, int cellW, int totalW) {
        int hH = s(38);
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(CORNER_BG);
        row.setPreferredSize(new Dimension(totalW, hH));
        row.setMinimumSize(new Dimension(totalW, hH));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, hH));

        JPanel corner = new JPanel(new GridBagLayout());
        corner.setBackground(CORNER_BG);
        corner.setPreferredSize(new Dimension(courtColW, hH));
        corner.setMinimumSize(new Dimension(courtColW, hH));
        corner.setMaximumSize(new Dimension(courtColW, hH));
        corner.setBorder(new MatteBorder(0, 0, 1, 1, CELL_BORDER_COLOR));
        corner.add(label("SÂN", bold(10f), TEXT_OLIVE));
        row.add(corner);

        for (int hour : hours) {
            JPanel cell = new JPanel(new GridBagLayout());
            cell.setOpaque(false);
            cell.setPreferredSize(new Dimension(cellW, hH));
            cell.setMinimumSize(new Dimension(cellW, hH));
            cell.setMaximumSize(new Dimension(cellW, hH));
            cell.setBorder(new MatteBorder(0, 0, 1, 1, CELL_BORDER_COLOR));
            cell.add(label(String.format("%02d:00", hour), bold(10f), TEXT_OLIVE));
            row.add(cell);
        }
        return row;
    }

    // ── court row (court label + slot cells) ──────────────────────────────────

    private JPanel buildCourtRow(CourtSchedule cs, int courtColW, int cellW, int totalW) {
        int rH = s(58);
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(Color.WHITE);
        row.setPreferredSize(new Dimension(totalW, rH));
        row.setMinimumSize(new Dimension(totalW, rH));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rH));

        JPanel courtCell = new JPanel(new GridBagLayout());
        courtCell.setBackground(LABEL_COL_BG);
        courtCell.setPreferredSize(new Dimension(courtColW, rH));
        courtCell.setMinimumSize(new Dimension(courtColW, rH));
        courtCell.setMaximumSize(new Dimension(courtColW, rH));
        courtCell.setBorder(new MatteBorder(0, 0, 1, 1, CELL_BORDER_COLOR));
        courtCell.add(label(cs.courtId(), bold(13f), TEXT_DARK));
        row.add(courtCell);

        for (int hour : hours) {
            SlotStatus slot = cs.slots().stream()
                    .filter(sl -> sl.startHour() == hour)
                    .findFirst().orElse(null);
            row.add(buildSlotCell(cs.courtId(), slot, cellW, rH));
        }
        return row;
    }

    // ── individual slot cell ──────────────────────────────────────────────────

    private JPanel buildSlotCell(String courtId, SlotStatus slot, int cellW, int rH) {
        boolean available = slot != null && slot.status() == BookingSlotStatus.AVAILABLE;
        boolean selected = isSelected(slot);

        Color normalBg = selected  ? SELECTED_BG
                : slot == null     ? EMPTY_CELL_BG
                : available        ? Color.WHITE
                :                    BOOKED_BG;

        JPanel cell = new JPanel(new GridBagLayout());
        cell.setOpaque(true);
        cell.setBackground(normalBg);
        cell.setPreferredSize(new Dimension(cellW, rH));
        cell.setMinimumSize(new Dimension(cellW, rH));
        cell.setMaximumSize(new Dimension(cellW, rH));
        cell.setBorder(new MatteBorder(0, 0, 1, 1, CELL_BORDER_COLOR));

        if (slot != null) {
            Color fg = (selected || !available) ? Color.WHITE : TEXT_DARK;
            JPanel inner = new JPanel();
            inner.setOpaque(false);
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

            JLabel priceLabel = label(compactMoney(slot.price()), bold(9f), fg);
            priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            priceLabel.setToolTipText(money(slot.price()));
            inner.add(priceLabel);

            if (selected || !available) {
                JLabel takenLabel = label(selected ? "Đã chọn" : "Đã đặt",
                        regular(9f), new Color(255, 255, 255, 210));
                takenLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                inner.add(Box.createVerticalStrut(s(2)));
                inner.add(takenLabel);
            }
            cell.add(inner);
        }

        if (available) {
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    toggleSlot(slot);
                    updateFooter();
                    renderMatrix();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) cell.setBackground(HOVER_BG);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) cell.setBackground(normalBg);
                }
            });
        }
        return cell;
    }

    // ── footer / confirm ──────────────────────────────────────────────────────

    private void toggleSlot(SlotStatus slot) {
        if (slot == null) {
            return;
        }
        for (int i = 0; i < selectedSlots.size(); i++) {
            if (sameSlot(selectedSlots.get(i), slot)) {
                selectedSlots.remove(i);
                return;
            }
        }
        selectedSlots.add(slot);
        selectedSlots.sort(slotComparator());
    }

    private boolean isSelected(SlotStatus slot) {
        if (slot == null) {
            return false;
        }
        return selectedSlots.stream().anyMatch(selected -> sameSlot(selected, slot));
    }

    private boolean sameSlot(SlotStatus left, SlotStatus right) {
        return left != null
                && right != null
                && Objects.equals(left.courtId(), right.courtId())
                && Objects.equals(left.priceBoardId(), right.priceBoardId())
                && Objects.equals(left.bookingDate(), right.bookingDate())
                && left.startHour() == right.startHour()
                && left.endHour() == right.endHour();
    }

    private void pruneUnavailableSelections() {
        if (selectedSlots.isEmpty()) {
            return;
        }
        selectedSlots.removeIf(selected -> schedules.stream()
                .flatMap(schedule -> schedule.slots().stream())
                .noneMatch(slot -> sameSlot(selected, slot)
                        && slot.status() == BookingSlotStatus.AVAILABLE));
        updateFooter();
    }

    private Comparator<SlotStatus> slotComparator() {
        return Comparator.comparing(SlotStatus::bookingDate)
                .thenComparing(SlotStatus::courtId)
                .thenComparingInt(SlotStatus::startHour)
                .thenComparingInt(SlotStatus::endHour);
    }

    private void updateFooter() {
        if (selectedSlots.isEmpty()) {
            slotLabel.setText("Chưa chọn khung giờ");
            totalLabel.setText("Tổng tiền: 0 VND");
            return;
        }
        if (selectedSlots.size() > 1) {
            slotLabel.setText("Đã chọn " + selectedSlots.size() + " khung giờ");
            totalLabel.setText("Tổng tiền: " + money(totalSelectedPrice()));
            return;
        }
        SlotStatus selectedSlot = selectedSlots.get(0);
        slotLabel.setText(selectedSlot.courtId()
                + " | " + String.format("%02d:00", selectedSlot.startHour())
                + " - " + String.format("%02d:00", selectedSlot.endHour())
                + " | " + selectedSlot.bookingDate());
        totalLabel.setText("Tổng tiền: " + money(selectedSlot.price()));
    }

    private void confirmSelection() {
        if (selectedSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một khung giờ.",
                    "Đặt sân", JOptionPane.WARNING_MESSAGE);
            return;
        }
        onConfirm.accept(List.copyOf(selectedSlots));
    }

    private BigDecimal totalSelectedPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (SlotStatus slot : selectedSlots) {
            if (slot.price() != null) {
                total = total.add(slot.price());
            }
        }
        return total;
    }

    private String money(BigDecimal value) {
        return MONEY_FORMAT.format(value == null ? BigDecimal.ZERO : value) + " VND";
    }

    private String compactMoney(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        BigDecimal thousand = new BigDecimal("1000");
        if (value.abs().compareTo(thousand) >= 0) {
            return MONEY_FORMAT.format(value.divide(thousand, 0, java.math.RoundingMode.HALF_UP)) + "K";
        }
        return MONEY_FORMAT.format(value);
    }
}
