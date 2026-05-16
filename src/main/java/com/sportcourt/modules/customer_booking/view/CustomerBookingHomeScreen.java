package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer_booking.controller.CustomerBookingController;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.CourtSortBy;
import com.sportcourt.modules.customer_booking.dto.SportTypeOption;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BiConsumer;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public class CustomerBookingHomeScreen extends JPanel {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private final CustomerBookingController controller;
    private final BiConsumer<BranchOption, CourtSearchResult> onBookingRequested;

    private final JTextField searchField = new JTextField();
    private final JComboBox<SortOption> sortBox = new JComboBox<>(new SortOption[]{
            new SortOption("Gia thap den cao", CourtSortBy.PRICE, true),
            new SortOption("Gia cao den thap", CourtSortBy.PRICE, false),
            new SortOption("Ma san A-Z", CourtSortBy.COURT_NAME, true)
    });

    private JLabel filterValueLabel;
    private JLabel resultCountLabel;
    private JPanel courtContainer;
    private BranchOption selectedBranch;
    private SportTypeOption selectedSportType;

    public CustomerBookingHomeScreen(CustomerBookingController controller,
                                     BiConsumer<BranchOption, CourtSearchResult> onBookingRequested) {
        this.controller = controller;
        this.onBookingRequested = onBookingRequested;
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(s(24), s(24), s(32), s(24)));
        add(cleanScrollPane(buildContent()), BorderLayout.CENTER);
        renderEmptyState("Nhan Dat san de chon chi nhanh va loai the thao.");
    }

    public void refreshCourts() {
        reloadCourts();
    }

    public void triggerFilterDialog() {
        openFilterDialog();
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
        gbc.insets = new Insets(0, 0, s(22), 0);
        content.add(buildFilterCard(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, s(16), 0);
        content.add(buildResultHeader(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        courtContainer = new JPanel(new BorderLayout());
        courtContainer.setOpaque(false);
        content.add(courtContainer, gbc);

        return content;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(s(18), 0));
        header.setOpaque(false);

        JLabel brand = label("RENTSTA", bold(36f), BRAND_DARK);
        header.add(brand, BorderLayout.WEST);
        header.add(buildSearchBar(), BorderLayout.CENTER);
        return header;
    }

    private JComponent buildSearchBar() {
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
        wrapper.setBorder(new EmptyBorder(0, s(18), 0, s(10)));
        wrapper.setPreferredSize(new Dimension(0, s(48)));
        wrapper.setMinimumSize(new Dimension(s(320), s(48)));

        JLabel searchIcon = new JLabel(icon("/icon/search.png", 16, 16));
        searchField.putClientProperty("JTextField.placeholderText", "Tim theo ma san...");
        searchField.setFont(regular(15f));
        searchField.setForeground(TEXT_DARK);
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.addActionListener(e -> reloadCourts());

        JButton searchButton = pillButton("Tim", GREEN, GREEN_DARK);
        searchButton.addActionListener(e -> reloadCourts());

        wrapper.add(searchIcon, BorderLayout.WEST);
        wrapper.add(searchField, BorderLayout.CENTER);
        wrapper.add(searchButton, BorderLayout.EAST);
        return wrapper;
    }

    private JComponent buildFilterCard() {
        RoundedPanel card = new RoundedPanel(s(18), SURFACE_BG, true);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(s(18), s(22), s(18), s(22)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel title = label("BO LOC DAT SAN", bold(12f), TEXT_MUTED);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterValueLabel = label("Chua chon chi nhanh va loai the thao", bold(15f), TEXT_DARK);
        filterValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(title);
        info.add(Box.createVerticalStrut(s(6)));
        info.add(filterValueLabel);
        card.add(info, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, s(16), 0, 0);
        JButton selectFilterButton = pillButton("Dat san", GREEN, GREEN_DARK);
        selectFilterButton.addActionListener(e -> openFilterDialog());
        card.add(selectFilterButton, gbc);

        return card;
    }

    private JComponent buildResultHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, s(10), 0));
        left.setOpaque(false);
        left.add(label("Ket qua tim kiem", bold(20f), TEXT_DARK));
        resultCountLabel = label("(0 san)", regular(12f), new Color(161, 161, 170));
        left.add(resultCountLabel);
        row.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(8), 0));
        right.setOpaque(false);
        right.add(label("Sap xep:", regular(13f), new Color(161, 161, 170)));
        sortBox.setFont(regular(13f));
        sortBox.setPreferredSize(new Dimension(s(185), s(36)));
        sortBox.addActionListener(e -> reloadCourts());
        right.add(sortBox);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void openFilterDialog() {
        try {
            BookingFilterDialog.Selection selection = BookingFilterDialog.showDialog(
                    this,
                    controller,
                    selectedBranch,
                    selectedSportType
            );
            if (selection == null) {
                return;
            }

            selectedBranch = selection.branch();
            selectedSportType = selection.sportType();
            filterValueLabel.setText(selectedBranch.branchName()
                    + " - " + selectedBranch.address()
                    + " | " + selectedSportType.sportTypeName());
            reloadCourts();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void reloadCourts() {
        if (selectedBranch == null || selectedSportType == null) {
            renderEmptyState("Nhan Dat san de chon chi nhanh va loai the thao.");
            return;
        }

        try {
            SortOption sort = (SortOption) sortBox.getSelectedItem();
            if (sort == null) {
                sort = new SortOption("Gia thap den cao", CourtSortBy.PRICE, true);
            }
            List<CourtSearchResult> courts = controller.searchCourts(
                    searchField.getText(),
                    selectedBranch.branchId(),
                    selectedSportType.sportTypeId(),
                    sort.sortBy(),
                    sort.ascending()
            );
            renderCourts(courts);
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void renderCourts(List<CourtSearchResult> courts) {
        courtContainer.removeAll();
        resultCountLabel.setText("(" + courts.size() + " san)");

        if (courts.isEmpty()) {
            renderEmptyState("Khong tim thay san phu hop.");
            return;
        }

        JPanel grid = new JPanel(new GridLayout(0, 3, s(20), s(20)));
        grid.setOpaque(false);
        for (CourtSearchResult court : courts) {
            grid.add(new CourtCard(court));
        }
        courtContainer.add(grid, BorderLayout.NORTH);
        courtContainer.revalidate();
        courtContainer.repaint();
    }

    private void renderEmptyState(String message) {
        if (courtContainer == null) {
            return;
        }
        courtContainer.removeAll();
        RoundedPanel empty = new RoundedPanel(s(18), SURFACE_BG);
        empty.setLayout(new BorderLayout());
        empty.setBorder(new EmptyBorder(s(32), s(24), s(32), s(24)));
        JLabel label = label(message, bold(15f), TEXT_MUTED);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        empty.add(label, BorderLayout.CENTER);
        courtContainer.add(empty, BorderLayout.NORTH);
        resultCountLabel.setText("(0 san)");
        courtContainer.revalidate();
        courtContainer.repaint();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message == null || message.isBlank() ? "Co loi xay ra." : message,
                "Dat san",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private String money(BigDecimal value) {
        return MONEY_FORMAT.format(value == null ? BigDecimal.ZERO : value) + " VND";
    }

    private final class CourtCard extends RoundedPanel {
        CourtCard(CourtSearchResult court) {
            super(s(24), SURFACE_BG, true);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(s(24), s(24), s(24), s(24)));

            JLabel title = label(court.courtId(), bold(20f), TEXT_DARK);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(title);
            add(Box.createVerticalStrut(s(8)));
            add(locationRow(court.branchAddress()));
            add(Box.createVerticalStrut(s(10)));
            add(badgePriceRow(court));
            add(Box.createVerticalStrut(s(16)));
            add(divider());
            add(Box.createVerticalStrut(s(16)));
            add(bookRow(court));
        }

        private JComponent locationRow(String address) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, s(6), 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            Icon locIcon = icon("/icon/location.png", 14, 14);
            if (locIcon != null) {
                row.add(new JLabel(locIcon));
            }
            row.add(label(address == null ? "--" : address, regular(14f), TEXT_MUTED));
            return row;
        }

        private JComponent badgePriceRow(CourtSearchResult court) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel badge = new JLabel(court.sportTypeName()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(TAG_BLUE_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setFont(bold(12f));
            badge.setForeground(TAG_BLUE_TEXT);
            badge.setOpaque(false);
            badge.setBorder(new EmptyBorder(s(4), s(12), s(4), s(12)));

            JLabel price = label(money(court.minPrice()) + " / gio", bold(16f), TEXT_DARK);
            price.setHorizontalAlignment(SwingConstants.RIGHT);

            row.add(badge, BorderLayout.WEST);
            row.add(price, BorderLayout.EAST);
            return row;
        }

        private JComponent divider() {
            JPanel line = new JPanel();
            line.setOpaque(true);
            line.setBackground(new Color(0, 0, 0, 40));
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            line.setPreferredSize(new Dimension(0, 1));
            line.setAlignmentX(Component.LEFT_ALIGNMENT);
            return line;
        }

        private JComponent bookRow(CourtSearchResult court) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            JButton book = pillButton("Đặt ngay", GREEN, GREEN_DARK);
            book.addActionListener(e -> onBookingRequested.accept(selectedBranch, court));
            row.add(book);
            return row;
        }
    }

    private record SortOption(String label, CourtSortBy sortBy, boolean ascending) {
        @Override
        public String toString() {
            return label;
        }
    }

    private static final class BookingFilterDialog extends JDialog {
        private final CustomerBookingController controller;
        private final JComboBox<BranchOption> branchCombo = new JComboBox<>();
        private final JComboBox<SportTypeOption> sportTypeCombo = new JComboBox<>();
        private Selection result;

        static Selection showDialog(Component parent,
                                    CustomerBookingController controller,
                                    BranchOption currentBranch,
                                    SportTypeOption currentSportType) {
            Window owner = SwingUtilities.getWindowAncestor(parent);
            BookingFilterDialog dialog = new BookingFilterDialog(owner, controller, currentBranch, currentSportType);
            dialog.setVisible(true);
            return dialog.result;
        }

        private BookingFilterDialog(Window owner,
                                    CustomerBookingController controller,
                                    BranchOption currentBranch,
                                    SportTypeOption currentSportType) {
            super(owner, "Đặt sân", ModalityType.APPLICATION_MODAL);
            this.controller = controller;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(buildContent());
            pack();
            setMinimumSize(new Dimension(s(760), s(0)));
            setLocationRelativeTo(owner);
            loadBranches(currentBranch, currentSportType);
        }

        private JComponent buildContent() {
            JPanel root = new JPanel(new BorderLayout(0, s(18)));
            root.setBackground(Color.WHITE);
            root.setBorder(new EmptyBorder(s(25), s(25), s(25), s(25)));

            JLabel heading = label("Vui lòng chọn chi nhánh để tiếp tục!", bold(20f), TEXT_DARK);
            root.add(heading, BorderLayout.NORTH);

            JPanel controls = new JPanel(new GridBagLayout());
            controls.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.SOUTH;

            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.insets = new Insets(0, 0, 0, s(16));
            branchCombo.setRenderer(new BranchRenderer());
            branchCombo.addActionListener(e -> loadSportTypes((BranchOption) branchCombo.getSelectedItem(), null));
            controls.add(buildDropdownField("CHI NHÁNH", branchCombo), gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(0, 0, 0, s(16));
            sportTypeCombo.setRenderer(new SportTypeRenderer());
            controls.add(buildDropdownField("MÔN THỂ THAO", sportTypeCombo), gbc);

            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.insets = new Insets(0, 0, 0, 0);
            controls.add(buildApplyWrapper(), gbc);

            root.add(controls, BorderLayout.CENTER);
            return root;
        }

        private JComponent buildDropdownField(String labelText, JComboBox<?> combo) {
            JPanel col = new JPanel(new BorderLayout(0, s(8)));
            col.setOpaque(false);
            col.add(label(labelText, bold(12f), TEXT_MUTED), BorderLayout.NORTH);
            combo.setFont(regular(15f));
            col.add(combo, BorderLayout.CENTER);
            return col;
        }

        private JComponent buildApplyWrapper() {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            JLabel spacer = new JLabel(" ");
            spacer.setFont(bold(12f));
            spacer.setBorder(new EmptyBorder(0, 0, s(8), 0));
            wrapper.add(spacer, BorderLayout.NORTH);
            JButton apply = new JButton("Áp dụng") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(GREEN);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), s(12), s(12));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            apply.setFont(bold(15f));
            apply.setForeground(GREEN_DARK);
            apply.setFocusPainted(false);
            apply.setContentAreaFilled(false);
            apply.setBorderPainted(false);
            apply.setCursor(new Cursor(Cursor.HAND_CURSOR));
            apply.setBorder(new EmptyBorder(s(9), s(28), s(9), s(28)));
            apply.addActionListener(e -> accept());
            wrapper.add(apply, BorderLayout.CENTER);
            return wrapper;
        }

        private void loadBranches(BranchOption currentBranch, SportTypeOption currentSportType) {
            DefaultComboBoxModel<BranchOption> model = new DefaultComboBoxModel<>();
            List<BranchOption> branches = controller.loadBranches();
            for (BranchOption branch : branches) {
                model.addElement(branch);
            }
            branchCombo.setModel(model);
            selectBranch(currentBranch);
            loadSportTypes((BranchOption) branchCombo.getSelectedItem(), currentSportType);
        }

        private void selectBranch(BranchOption currentBranch) {
            if (currentBranch == null) {
                return;
            }
            for (int i = 0; i < branchCombo.getItemCount(); i++) {
                BranchOption item = branchCombo.getItemAt(i);
                if (item.branchId().equals(currentBranch.branchId())) {
                    branchCombo.setSelectedIndex(i);
                    return;
                }
            }
        }

        private void loadSportTypes(BranchOption branch, SportTypeOption currentSportType) {
            DefaultComboBoxModel<SportTypeOption> model = new DefaultComboBoxModel<>();
            if (branch != null) {
                List<SportTypeOption> sportTypes = controller.loadSportTypes(branch.branchId());
                for (SportTypeOption sportType : sportTypes) {
                    model.addElement(sportType);
                }
            }
            sportTypeCombo.setModel(model);
            if (currentSportType != null) {
                for (int i = 0; i < sportTypeCombo.getItemCount(); i++) {
                    SportTypeOption item = sportTypeCombo.getItemAt(i);
                    if (item.sportTypeId().equals(currentSportType.sportTypeId())) {
                        sportTypeCombo.setSelectedIndex(i);
                        return;
                    }
                }
            }
        }

        private void accept() {
            BranchOption branch = (BranchOption) branchCombo.getSelectedItem();
            SportTypeOption sportType = (SportTypeOption) sportTypeCombo.getSelectedItem();
            if (branch == null || sportType == null) {
                JOptionPane.showMessageDialog(this, "Vui long chon day du thong tin.", "Dat san",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            result = new Selection(branch, sportType);
            dispose();
        }

        private record Selection(BranchOption branch, SportTypeOption sportType) {
        }

        private static final class BranchRenderer extends DefaultListCellRenderer {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof BranchOption branch) {
                    lbl.setText(branch.branchName() + " - " + branch.address());
                }
                return lbl;
            }
        }

        private static final class SportTypeRenderer extends DefaultListCellRenderer {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SportTypeOption sportType) {
                    lbl.setText(sportType.sportTypeName());
                }
                return lbl;
            }
        }
    }
}
