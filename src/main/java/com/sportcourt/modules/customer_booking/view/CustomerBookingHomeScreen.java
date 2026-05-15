package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public class CustomerBookingHomeScreen extends JPanel {
    private final Consumer<CustomerBookingSampleData.CourtOption> onBookingRequested;

    public CustomerBookingHomeScreen(Consumer<CustomerBookingSampleData.CourtOption> onBookingRequested) {
        this.onBookingRequested = onBookingRequested;
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(s(24), s(24), s(32), s(24)));
        add(buildContent(), BorderLayout.NORTH);
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, s(24), 0);
        content.add(buildHeader(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, s(28), 0);
        content.add(buildFilterCard(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, s(18), 0);
        content.add(buildResultHeader(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(buildCourtGrid(CustomerBookingSampleData.courts()), gbc);

        return content;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(s(24), 0));
        header.setOpaque(false);

        JLabel brand = label("RENTSTA", bold(36f), BRAND_DARK);
        header.add(brand, BorderLayout.WEST);
        header.add(buildSearchField(), BorderLayout.CENTER);
        return header;
    }

    private JComponent buildSearchField() {
        JPanel wrapper = new JPanel(new BorderLayout(s(10), 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(244, 244, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, s(18), 0, s(18)));
        wrapper.setPreferredSize(new Dimension(0, s(48)));
        wrapper.setMinimumSize(new Dimension(s(280), s(48)));

        JLabel searchIcon = new JLabel(icon("/icon/search.png", 16, 16));
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Tìm kiếm sân hoặc câu lạc bộ...");
        search.setFont(regular(16f));
        search.setForeground(TEXT_DARK);
        search.setOpaque(false);
        search.setBorder(null);

        wrapper.add(searchIcon, BorderLayout.WEST);
        wrapper.add(search, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildFilterCard() {
        RoundedPanel card = new RoundedPanel(s(24), SURFACE_BG, true);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(s(20), s(22), s(22), s(22)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label = label("TÊN CHI NHÁNH", bold(12f), TEXT_MUTED);
        card.add(label, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(s(9), 0, 0, 0);
        card.add(buildBranchField(), gbc);
        return card;
    }

    private JComponent buildBranchField() {
        JPanel field = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(250, 250, 250));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), s(12), s(12));
                g2.setColor(BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, s(12), s(12));
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, s(14), 0, s(14)));
        field.setPreferredSize(new Dimension(0, s(44)));
        JLabel value = label("Tên chi nhánh", regular(15f), TEXT_DARK);
        field.add(value, BorderLayout.CENTER);
        return field;
    }

    private JComponent buildResultHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, s(10), 0));
        left.setOpaque(false);
        left.add(label("Kết quả tìm kiếm", bold(20f), TEXT_DARK));
        left.add(label("(124 sân khả dụng)", regular(12f), new Color(161, 161, 170)));
        row.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(8), 0));
        right.setOpaque(false);
        right.add(label("Sắp xếp theo:", regular(13f), new Color(161, 161, 170)));
        right.add(smallSortPill("Giá thấp nhất"));
        right.add(smallSortPill("Giá cao nhất"));
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComponent smallSortPill(String text) {
        JLabel label = label(text, bold(11f), new Color(82, 82, 91));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(new EmptyBorder(s(6), s(14), s(6), s(14)));
        JPanel pill = new RoundedPanel(s(999), SURFACE_BG);
        pill.setLayout(new BorderLayout());
        pill.setBorder(new RoundedBorder(BORDER, s(999)));
        pill.add(label, BorderLayout.CENTER);
        return pill;
    }

    private JComponent buildCourtGrid(List<CustomerBookingSampleData.CourtOption> courts) {
        JPanel grid = new JPanel(new GridLayout(0, 3, s(24), s(24)));
        grid.setOpaque(false);
        for (CustomerBookingSampleData.CourtOption court : courts) {
            grid.add(new CourtCard(court));
        }
        return grid;
    }

    private final class CourtCard extends RoundedPanel {
        CourtCard(CustomerBookingSampleData.CourtOption court) {
            super(s(28), SURFACE_BG, true);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(s(22), s(22), s(22), s(22)));
            setPreferredSize(new Dimension(s(300), s(172)));

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setOpaque(false);

            JLabel title = label(court.name(), bold(17f), TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            body.add(title);
            body.add(Box.createVerticalStrut(s(8)));
            body.add(buildAddressRow(court.address()));
            body.add(Box.createVerticalStrut(s(10)));
            body.add(buildMetaRow(court));
            body.add(Box.createVerticalStrut(s(12)));
            body.add(buildDivider());
            body.add(Box.createVerticalStrut(s(12)));
            body.add(buildActionRow(court));

            add(body, BorderLayout.CENTER);
        }

        private JComponent buildAddressRow(String address) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, s(6), 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel dot = label("●", regular(11f), new Color(161, 161, 170));
            row.add(dot);
            row.add(label(address, regular(12f), TEXT_MUTED));
            return row;
        }

        private JComponent buildMetaRow(CustomerBookingSampleData.CourtOption court) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel tag = label(court.sportType(), bold(11f), TAG_BLUE_TEXT);
            tag.setBorder(new EmptyBorder(s(4), s(8), s(4), s(8)));
            RoundedPanel tagBg = new RoundedPanel(s(999), TAG_BLUE_BG);
            tagBg.setLayout(new BorderLayout());
            tagBg.add(tag, BorderLayout.CENTER);
            row.add(tagBg, BorderLayout.WEST);

            row.add(label(court.price(), bold(14f), Color.BLACK), BorderLayout.EAST);
            return row;
        }

        private JComponent buildDivider() {
            JPanel line = new JPanel();
            line.setOpaque(true);
            line.setBackground(new Color(0, 0, 0, 50));
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            line.setPreferredSize(new Dimension(0, 1));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            return line;
        }

        private JComponent buildActionRow(CustomerBookingSampleData.CourtOption court) {
            JPanel row = new JPanel(new BorderLayout(s(16), 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton detail = pillButton("Chi tiết", new Color(212, 212, 212), TEXT_DARK);
            JButton book = pillButton("Đặt Ngay", GREEN, TEXT_DARK);
            detail.addActionListener(e -> onBookingRequested.accept(court));
            book.addActionListener(e -> onBookingRequested.accept(court));
            row.add(detail, BorderLayout.WEST);
            row.add(book, BorderLayout.EAST);
            return row;
        }
    }
}
