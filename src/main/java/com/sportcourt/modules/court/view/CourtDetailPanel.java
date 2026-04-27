package com.sportcourt.modules.court.view;

import com.sportcourt.modules.court.dto.CourtTableRow;
import com.sportcourt.modules.court.service.CourtService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

public class CourtDetailPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color PAGE_BACKGROUND = new Color(247, 247, 251);
    private static final Color CARD_BACKGROUND = new Color(241, 242, 246);
    private static final Color DARK_TEXT = new Color(30, 31, 36);
    private static final Color MUTED_TEXT = new Color(107, 114, 128);
    private static final Color GREEN_BG = new Color(216, 255, 208);
    private static final Color GREEN_TEXT = new Color(44, 154, 16);
    private static final Color RED_BG = new Color(254, 226, 226);
    private static final Color RED_TEXT = new Color(185, 28, 28);

    private final CourtService courtService;
    private final Runnable onBack;
    private final Consumer<CourtTableRow> onEdit;

    private final JLabel titleLabel = new JLabel("CHI TIẾT SÂN CON");
    private final JLabel courtIdValueLabel = createValueLabel("--");
    private final JLabel areaIdValueLabel = createValueLabel("--");
    private final JLabel sportTypeValueLabel = createValueLabel("--");
    private final JLabel branchValueLabel = createValueLabel("--");
    private final JLabel statusValueLabel = createValueLabel("--");
    private final JLabel createdAtValueLabel = createValueLabel("--");

    private CourtTableRow currentCourt;

    public CourtDetailPanel(
            CourtService courtService,
            Runnable onBack,
            Consumer<CourtTableRow> onEdit
    ) {
        this.courtService = courtService;
        this.onBack = onBack;
        this.onEdit = onEdit;

        setLayout(new BorderLayout(0, 18));
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(10, 0, 0, 0));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    public void showDetail(String courtId, String branchId) {
        try {
            Optional<CourtTableRow> courtOptional = courtService.findDetail(courtId, branchId);

            if (courtOptional.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không tìm thấy sân con thuộc chi nhánh hiện tại.",
                        "Chi tiết sân con",
                        JOptionPane.WARNING_MESSAGE
                );
                onBack.run();
                return;
            }

            currentCourt = courtOptional.get();
            bindCourt(currentCourt);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage(),
                    "Lỗi tải chi tiết sân con",
                    JOptionPane.ERROR_MESSAGE
            );
            onBack.run();
        }
    }

    private JPanel createHeader() {
        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.setOpaque(false);

        JLabel backLabel = new JLabel("< Quay lại trang Quản lý sân con");
        backLabel.setFont(new Font("Lexend", Font.PLAIN, 15));
        backLabel.setForeground(new Color(58, 134, 45));
        backLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                onBack.run();
            }
        });

        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setOpaque(false);
        backRow.add(backLabel);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(14, 0, 0, 0));

        titleLabel.setFont(new Font("Lexend", Font.BOLD, 30));
        titleLabel.setForeground(DARK_TEXT);
        titleRow.add(titleLabel, BorderLayout.WEST);

        headerWrapper.add(backRow);
        headerWrapper.add(titleRow);

        return headerWrapper;
    }

    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(28, 0));
        mainPanel.setOpaque(false);

        mainPanel.add(createBasicInfoCard(), BorderLayout.WEST);
        mainPanel.add(createOperationInfoCard(), BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createBasicInfoCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new EmptyBorder(26, 32, 26, 32));
        card.setPreferredSize(new Dimension(440, 0));

        JLabel cardTitle = new JLabel("Thông tin cơ bản");
        cardTitle.setFont(new Font("Lexend", Font.BOLD, 18));
        cardTitle.setForeground(DARK_TEXT);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hintLabel = new JLabel("Thông tin nhận diện sân con trong hệ thống.");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hintLabel.setForeground(MUTED_TEXT);
        hintLabel.setBorder(new EmptyBorder(6, 0, 18, 0));
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(cardTitle);
        card.add(hintLabel);
        card.add(createFieldBlock("MÃ SÂN", courtIdValueLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("MÃ KHU VỰC", areaIdValueLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("LOẠI THỂ THAO", sportTypeValueLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("CHI NHÁNH", branchValueLabel));
        card.add(Box.createVerticalGlue());

        JButton editButton = createPillButton(
                "SỬA THÔNG TIN",
                new Color(57, 255, 20),
                new Color(26, 42, 8),
                true
        );
        editButton.setPreferredSize(new Dimension(280, 48));
        editButton.addActionListener(event -> {
            if (currentCourt != null) {
                onEdit.accept(currentCourt);
            }
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(editButton);

        card.add(buttonRow);

        return card;
    }

    private JPanel createOperationInfoCard() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 18));
        wrapper.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("Thông tin vận hành");
        title.setFont(new Font("Lexend", Font.BOLD, 18));
        title.setForeground(DARK_TEXT);

        JLabel subtitle = new JLabel("Dùng để kiểm tra trạng thái trước khi cho thuê hoặc bảo trì sân.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED_TEXT);
        subtitle.setBorder(new EmptyBorder(6, 0, 0, 0));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(236, 236, 239)),
                new EmptyBorder(26, 28, 26, 28)
        ));

        infoCard.add(createFieldBlock("TRẠNG THÁI", statusValueLabel));
        infoCard.add(Box.createVerticalStrut(14));
        infoCard.add(createFieldBlock("NGÀY TẠO", createdAtValueLabel));
        infoCard.add(Box.createVerticalStrut(22));
        infoCard.add(createStatusNote());

        wrapper.add(titlePanel, BorderLayout.NORTH);
        wrapper.add(infoCard, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createStatusNote() {
        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.setBackground(new Color(248, 249, 250));
        notePanel.setBorder(new EmptyBorder(16, 18, 16, 18));
        notePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel label = new JLabel("<html>Sân ở trạng thái <b>ĐANG HOẠT ĐỘNG</b> mới nên được dùng cho đặt sân. "
                + "Nếu chuyển sang <b>BẢO TRÌ</b>, hệ thống cần tránh tạo lịch thuê mới cho sân này.</html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(MUTED_TEXT);

        notePanel.add(label, BorderLayout.CENTER);

        return notePanel;
    }

    private JPanel createFieldBlock(String labelText, JLabel valueLabel) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(92, 102, 77));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel valueWrapper = new JPanel(new BorderLayout());
        valueWrapper.setBackground(Color.WHITE);
        valueWrapper.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(229, 231, 235)),
                new EmptyBorder(10, 16, 10, 16)
        ));
        valueWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        valueWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueWrapper.add(valueLabel, BorderLayout.CENTER);

        fieldPanel.add(label);
        fieldPanel.add(Box.createVerticalStrut(6));
        fieldPanel.add(valueWrapper);

        return fieldPanel;
    }

    private void bindCourt(CourtTableRow court) {
        titleLabel.setText("CHI TIẾT SÂN CON - " + valueOrPlaceholder(court.getCourtId()));
        courtIdValueLabel.setText(valueOrPlaceholder(court.getCourtId()));
        areaIdValueLabel.setText(valueOrPlaceholder(court.getAreaId()));
        sportTypeValueLabel.setText(valueOrPlaceholder(court.getSportTypeName()));
        branchValueLabel.setText(valueOrPlaceholder(court.getBranchName()));
        statusValueLabel.setText(valueOrPlaceholder(court.getStatus()));
        statusValueLabel.setForeground(getStatusForeground(court.getStatus()));
        createdAtValueLabel.setText(formatDate(court.getCreatedAt()));
    }

    private Color getStatusForeground(String status) {
        if ("ĐANG HOẠT ĐỘNG".equals(status)) {
            return GREEN_TEXT;
        }

        if ("BẢO TRÌ".equals(status)) {
            return RED_TEXT;
        }

        return DARK_TEXT;
    }

    private JButton createPillButton(String text, Color background, Color foreground, boolean bold) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

                super.paintComponent(graphics);
                g2.dispose();
            }
        };

        button.setForeground(foreground);
        button.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 22, 10, 22));

        return button;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(DARK_TEXT);
        return label;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "--" : dateTime.format(DATE_FORMATTER);
    }

    private String valueOrPlaceholder(String value) {
        return value == null || value.isBlank() ? "--" : value;
    }
}