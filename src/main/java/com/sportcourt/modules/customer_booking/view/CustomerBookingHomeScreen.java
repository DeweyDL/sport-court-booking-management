package com.sportcourt.modules.customer_booking.view;

import com.sportcourt.common.style.AppFonts;
import com.sportcourt.modules.customer_booking.controller.CustomerBookingController;
import com.sportcourt.modules.customer_booking.dto.BranchOption;
import com.sportcourt.modules.customer_booking.dto.CourtSearchResult;
import com.sportcourt.modules.customer_booking.dto.CourtSortBy;
import com.sportcourt.modules.customer_booking.dto.SportTypeOption;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.sportcourt.modules.customer_booking.view.CustomerBookingViewStyle.*;

public class CustomerBookingHomeScreen extends JPanel {
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CustomerBookingController controller;
    private final BookingRequestHandler onBookingRequested;

    private final JTextField searchField = new JTextField();
    private final JComboBox<SortOption> sortBox = new JComboBox<>(new SortOption[]{
            new SortOption("Giá thấp đến cao", CourtSortBy.PRICE, true),
            new SortOption("Giá cao đến thấp", CourtSortBy.PRICE, false),
            new SortOption("Mã sân A-Z", CourtSortBy.COURT_NAME, true)
    });

    private JLabel filterValueLabel;
    private JLabel resultCountLabel;
    private JPanel courtContainer;
    private BranchOption selectedBranch;
    private SportTypeOption selectedSportType;
    private LocalDate selectedDate = LocalDate.now();

    public CustomerBookingHomeScreen(CustomerBookingController controller,
                                     BookingRequestHandler onBookingRequested) {
        this.controller = controller;
        this.onBookingRequested = onBookingRequested;
        AppFonts.register();
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);
        setBorder(new EmptyBorder(s(24), s(24), s(32), s(24)));
        add(cleanScrollPane(buildContent()), BorderLayout.CENTER);
        renderEmptyState("Nhấn đặt sân để chọn chi nhánh và loại thể thao");
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

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        content.add(Box.createVerticalGlue(), gbc);

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
        searchField.putClientProperty("JTextField.placeholderText", "Tìm theo mã sân...");
        searchField.setFont(regular(15f));
        searchField.setForeground(TEXT_DARK);
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.addActionListener(e -> reloadCourts());

        JButton searchButton = pillButton("Tìm", GREEN, GREEN_DARK);
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
        JLabel title = label("Bộ lọc đặt sân", bold(12f), TEXT_MUTED);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterValueLabel = label("Chưa chọn chi nhánh và loại thể thao", bold(15f), TEXT_DARK);
        filterValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(title);
        info.add(Box.createVerticalStrut(s(6)));
        info.add(filterValueLabel);
        card.add(info, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, s(16), 0, 0);
        JButton selectFilterButton = pillButton("Chọn chi nhánh", GREEN, GREEN_DARK);
        selectFilterButton.addActionListener(e -> openFilterDialog());
        card.add(selectFilterButton, gbc);

        return card;
    }

    private JComponent buildResultHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, s(10), 0));
        left.setOpaque(false);
        left.add(label("Kết quả tìm kiếm", bold(20f), TEXT_DARK));
        resultCountLabel = label("(0 sân)", regular(12f), new Color(161, 161, 170));
        left.add(resultCountLabel);
        row.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, s(8), 0));
        right.setOpaque(false);
        right.add(label("Sắp xếp:", regular(13f), new Color(161, 161, 170)));
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
                    selectedSportType,
                    selectedDate
            );
            if (selection == null) {
                return;
            }

            selectedBranch = selection.branch();
            selectedSportType = selection.sportType();
            selectedDate = selection.bookingDate();
            filterValueLabel.setText(selectedBranch.branchName()
                    + " - " + selectedBranch.address()
                    + " | " + selectedSportType.sportTypeName()
                    + " | " + selectedDate.format(DATE_FORMATTER));
            reloadCourts();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void reloadCourts() {
        if (selectedBranch == null || selectedSportType == null) {
            renderEmptyState("Nhấn Đặt sân để chọn chi nhánh và loại thể thao");
            return;
        }

        try {
            SortOption sort = (SortOption) sortBox.getSelectedItem();
            if (sort == null) {
                sort = new SortOption("Giá thấp đến cao", CourtSortBy.PRICE, true);
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
        resultCountLabel.setText("(" + courts.size() + " sân)");

        if (courts.isEmpty()) {
            renderEmptyState("Không tìm thấy sân phù hợp.");
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
        resultCountLabel.setText("(0 sân)");
        courtContainer.revalidate();
        courtContainer.repaint();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message == null || message.isBlank() ? "Có lỗi xảy ra." : message,
                "Đặt sân",
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

            JLabel price = label(money(court.minPrice()) + " / giờ", bold(16f), TEXT_DARK);
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
            book.addActionListener(e -> onBookingRequested.accept(selectedBranch, court, selectedDate));
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

    @FunctionalInterface
    public interface BookingRequestHandler {
        void accept(BranchOption branch, CourtSearchResult court, LocalDate bookingDate);
    }

    private static final class BookingFilterDialog extends JDialog {
        private final CustomerBookingController controller;
        private final JComboBox<BranchOption> branchCombo = new JComboBox<>();
        private final JComboBox<SportTypeOption> sportTypeCombo = new JComboBox<>();
        private final DatePicker datePicker = createDatePicker();
        private Selection result;

        static Selection showDialog(Component parent,
                                    CustomerBookingController controller,
                                    BranchOption currentBranch,
                                    SportTypeOption currentSportType,
                                    LocalDate currentDate) {
            Window owner = SwingUtilities.getWindowAncestor(parent);
            BookingFilterDialog dialog = new BookingFilterDialog(owner, controller, currentBranch, currentSportType,
                    currentDate);
            dialog.setVisible(true);
            return dialog.result;
        }

        private BookingFilterDialog(Window owner,
                                    CustomerBookingController controller,
                                    BranchOption currentBranch,
                                    SportTypeOption currentSportType,
                                    LocalDate currentDate) {
            super(owner, "Đặt sân", ModalityType.APPLICATION_MODAL);
            this.controller = controller;
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setContentPane(buildContent());
            setInitialDate(currentDate);
            loadBranches(currentBranch, currentSportType);
            pack();
            fitToScreen(owner);
            setLocationRelativeTo(owner);
        }

        private JComponent buildContent() {
            JPanel root = new JPanel();
            root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
            root.setBackground(Color.WHITE);
            root.setBorder(new EmptyBorder(s(25), s(25), s(25), s(25)));

            JLabel heading = label("Vui lòng chọn chi nhánh để tiếp tục!", bold(20f), TEXT_DARK);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);
            root.add(heading);
            root.add(Box.createVerticalStrut(s(18)));

            JPanel controls = new JPanel();
            controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
            controls.setOpaque(false);
            controls.setAlignmentX(Component.LEFT_ALIGNMENT);
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
            controls.add(buildDatePickerField("NGÀY ĐẶT", datePicker));
            controls.add(Box.createVerticalStrut(s(18)));
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
            col.setBorder(new EmptyBorder(0, 0, s(10), 0));
            col.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(82)));
            return col;
        }

        private JComponent buildDatePickerField(String labelText, DatePicker picker) {
            JPanel col = new JPanel(new BorderLayout(0, s(8)));
            col.setOpaque(false);
            col.add(label(labelText, bold(12f), TEXT_MUTED), BorderLayout.NORTH);
            col.add(picker, BorderLayout.CENTER);
            col.setBorder(new EmptyBorder(0, 0, s(10), 0));
            col.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(82)));
            return col;
        }

        private DatePicker createDatePicker() {
            DatePickerSettings settings = new DatePickerSettings();
            settings.setFormatForDatesCommonEra("dd/MM/yyyy");
            settings.setAllowEmptyDates(false);
            settings.setColor(DatePickerSettings.DateArea.BackgroundOverallCalendarPanel, Color.WHITE);
            settings.setColor(DatePickerSettings.DateArea.BackgroundMonthAndYearMenuLabels, Color.WHITE);
            DatePicker picker = new DatePicker(settings);
            picker.setFont(regular(15f));
            picker.setDate(LocalDate.now());
            picker.setOpaque(false);
            picker.getComponentDateTextField().setFont(regular(15f));
            picker.getComponentDateTextField().setForeground(TEXT_DARK);
            return picker;
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
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, s(46)));
            return wrapper;
        }

        private void fitToScreen(Window owner) {
            Rectangle bounds = owner == null
                    ? GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds()
                    : owner.getGraphicsConfiguration().getBounds();
            int width = Math.min(s(520), Math.max(s(340), bounds.width - s(60)));
            int height = Math.min(getPreferredSize().height, Math.max(s(320), bounds.height - s(80)));
            setMinimumSize(new Dimension(Math.min(width, s(340)), s(300)));
            setSize(new Dimension(width, height));
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
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ thông tin", "Đặt sân",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            result = new Selection(branch, sportType, selectedDate());
            dispose();
        }

        private void setInitialDate(LocalDate date) {
            LocalDate value = date == null ? LocalDate.now() : date;
            datePicker.setDate(value);
        }

        private LocalDate selectedDate() {
            LocalDate value = datePicker.getDate();
            return value == null ? LocalDate.now() : value;
        }

        private record Selection(BranchOption branch, SportTypeOption sportType, LocalDate bookingDate) {
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
