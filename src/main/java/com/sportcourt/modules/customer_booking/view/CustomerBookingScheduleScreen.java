package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public class CustomerBookingScheduleScreen extends JPanel {
    private final Runnable onBack;
    private final Runnable onConfirm;
    private final JLabel selectedSlotLabel = label("10:00 - 12:00 | 02/08/2024", bold(14f), GREEN_DARK);
    private final JLabel totalLabel = label("Tổng tiền: 300.000VNĐ", bold(14f), GREEN_DARK);

    public CustomerBookingScheduleScreen(Runnable onBack, Runnable onConfirm) {
        this.onBack = onBack;
        this.onConfirm = onConfirm;
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setPreferredSize(new Dimension(s(1240), s(820)));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GREEN_DARK);
        header.setBorder(new EmptyBorder(s(16), s(24), s(16), s(24)));
        JLabel title = label("ĐẶT SÂN", bold(28f), Color.WHITE);
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JComponent buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.add(buildFilterStrip(), BorderLayout.NORTH);
        main.add(buildGridScroll(), BorderLayout.CENTER);
        return main;
    }

    private JComponent buildFilterStrip() {
        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(new Color(243, 243, 246));
        strip.setBorder(new EmptyBorder(s(22), s(24), s(22), s(24)));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, s(36), 0));
        filters.setOpaque(false);
        filters.add(filterControl("LOẠI THỂ THAO - KHU VỰC", "Bóng đá - A", "/icon/branch.png"));
        filters.add(filterControl("THỜI LƯỢNG", "90 phút", "/icon/calendar.png"));
        filters.add(filterControl("NGÀY ĐẶT", "02/08/2024", "/icon/calendar.png"));
        strip.add(filters, BorderLayout.WEST);
        strip.add(buildLegend(), BorderLayout.EAST);
        return strip;
    }

    private JComponent filterControl(String labelText, String valueText, String iconPath) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        JLabel label = label(labelText, bold(12f), TEXT_OLIVE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(s(8)));

        RoundedPanel pill = new RoundedPanel(s(48), PAGE_BG, true);
        pill.setLayout(new BorderLayout(s(10), 0));
        pill.setBorder(new EmptyBorder(s(9), s(16), s(9), s(16)));
        pill.setPreferredSize(new Dimension(s(210), s(48)));
        pill.setMaximumSize(new Dimension(s(230), s(48)));
        Icon icon = icon(iconPath, 16, 16);
        if (icon != null) {
            pill.add(new JLabel(icon), BorderLayout.WEST);
        }
        pill.add(label(valueText, regular(14f), TEXT_DARK), BorderLayout.CENTER);
        wrapper.add(pill);
        return wrapper;
    }

    private JComponent buildLegend() {
        RoundedPanel legend = new RoundedPanel(s(16), new Color(232, 232, 234, 130));
        legend.setLayout(new FlowLayout(FlowLayout.RIGHT, s(16), s(12)));
        legend.add(legendItem("Trống", Color.WHITE, true));
        legend.add(legendItem("Đang chọn", new Color(59, 130, 246), false));
        legend.add(legendItem("Đã hết", new Color(148, 163, 184), false));
        return legend;
    }

    private JComponent legendItem(String text, Color color, boolean border) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, s(6), 0));
        item.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                if (border) {
                    g2.setColor(new Color(186, 204, 176));
                    g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                }
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(s(12), s(12)));
        item.add(dot);
        item.add(label(text, bold(11f), TEXT_DARK));
        return item;
    }

    private JComponent buildGridScroll() {
        ScheduleGrid grid = new ScheduleGrid();
        grid.setSelectionChangedListener((slot, total) -> {
            selectedSlotLabel.setText(slot);
            totalLabel.setText(total);
        });

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(s(16), s(16), s(16), s(16)));
        wrapper.add(grid, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.setBackground(PAGE_BG);
        scroll.getHorizontalScrollBar().setUnitIncrement(s(20));
        scroll.getVerticalScrollBar().setUnitIncrement(s(20));
        return scroll;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(s(48), 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(s(18), s(24), s(18), s(24)));

        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, s(46), 0));
        summary.setOpaque(false);
        summary.add(selectedSlotLabel);
        summary.add(totalLabel);
        footer.add(summary, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(24), 0));
        actions.setOpaque(false);
        JButton reset = pillButton("Chọn lại", Color.WHITE, GREEN_DARK);
        reset.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(GREEN_DARK, s(999)),
                new EmptyBorder(s(8), s(28), s(8), s(28))
        ));
        reset.addActionListener(e -> onBack.run());

        JButton book = pillButton("Đặt lịch", GREEN, GREEN_DARK);
        book.addActionListener(e -> onConfirm.run());
        actions.add(reset);
        actions.add(book);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private static final class ScheduleGrid extends JPanel {
        private final String[] courts = {"Sân 1", "Sân 2", "Sân 3", "Sân 4", "Sân 5"};
        private final String[] times = {"05:00", "06:00", "07:00", "08:00", "9:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
        private final boolean[][] booked = new boolean[courts.length][times.length];
        private int selectedRow = 1;
        private int selectedCol = 5;
        private SelectionChangedListener listener;

        ScheduleGrid() {
            setOpaque(false);
            setPreferredSize(new Dimension(s(1260), s(456)));
            markBooked();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    handleClick(event.getX(), event.getY());
                }
            });
        }

        void setSelectionChangedListener(SelectionChangedListener listener) {
            this.listener = listener;
        }

        private void markBooked() {
            booked[0][0] = true;
            booked[0][9] = true;
            booked[0][10] = true;
            booked[1][10] = true;
            booked[1][11] = true;
            booked[3][6] = true;
            booked[3][7] = true;
            booked[3][8] = true;
            booked[4][10] = true;
            booked[4][11] = true;
        }

        private void handleClick(int x, int y) {
            int left = s(96);
            int header = s(48);
            int rowHeight = s(80);
            int colWidth = (getWidth() - left) / times.length;
            if (x < left || y < header) {
                return;
            }
            int row = (y - header) / rowHeight;
            int col = (x - left) / Math.max(1, colWidth);
            if (row < 0 || row >= courts.length || col < 0 || col >= times.length || booked[row][col]) {
                return;
            }
            selectedRow = row;
            selectedCol = col;
            if (listener != null) {
                String end = times[Math.min(times.length - 1, col + 2)];
                listener.selectionChanged(times[col] + " - " + end + " | 02/08/2024", "Tổng tiền: 300.000VNĐ");
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), s(32), s(32));

            int left = s(96);
            int header = s(48);
            int rowHeight = s(80);
            int gridWidth = getWidth() - left;
            int colWidth = gridWidth / times.length;

            g2.setColor(new Color(232, 232, 234));
            g2.fillRoundRect(0, 0, left, header, s(24), s(24));
            g2.fillRect(left, 0, gridWidth, header);
            g2.setColor(new Color(243, 243, 246));
            g2.fillRect(0, header, left, courts.length * rowHeight);

            g2.setFont(bold(10f));
            g2.setColor(TEXT_OLIVE);
            drawCentered(g2, "COURTS", 0, 0, left, header);
            for (int col = 0; col < times.length; col++) {
                drawCentered(g2, times[col], left + col * colWidth, 0, colWidth, header);
            }

            g2.setFont(bold(13f));
            g2.setColor(TEXT_DARK);
            for (int row = 0; row < courts.length; row++) {
                drawCentered(g2, courts[row], 0, header + row * rowHeight, left, rowHeight);
            }

            for (int row = 0; row < courts.length; row++) {
                for (int col = 0; col < times.length; col++) {
                    if (booked[row][col]) {
                        g2.setColor(new Color(161, 161, 170));
                        g2.fillRect(left + col * colWidth, header + row * rowHeight, colWidth, rowHeight);
                    }
                }
            }

            g2.setColor(new Color(59, 130, 246));
            int selectedWidth = Math.min(colWidth * 2, getWidth() - (left + selectedCol * colWidth));
            g2.fillRect(left + selectedCol * colWidth, header + selectedRow * rowHeight, selectedWidth, rowHeight);

            g2.setColor(new Color(186, 204, 176, 90));
            for (int col = 0; col <= times.length; col++) {
                int x = left + col * colWidth;
                g2.drawLine(x, 0, x, header + courts.length * rowHeight);
            }
            for (int row = 0; row <= courts.length; row++) {
                int y = header + row * rowHeight;
                g2.drawLine(0, y, getWidth(), y);
            }
            g2.dispose();
        }

        private void drawCentered(Graphics2D g2, String text, int x, int y, int width, int height) {
            FontMetrics metrics = g2.getFontMetrics();
            int tx = x + (width - metrics.stringWidth(text)) / 2;
            int ty = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
            g2.drawString(text, tx, ty);
        }
    }

    private interface SelectionChangedListener {
        void selectionChanged(String slot, String total);
    }
}
