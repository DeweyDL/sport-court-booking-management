package com.sportcourt.modules.account.view;

import com.sportcourt.modules.account.dto.AccountRow;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class AccountDetailPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color PAGE_BACKGROUND = new Color(247, 247, 251);
    private static final Color CARD_BACKGROUND = new Color(241, 242, 246);
    private static final Color DARK_TEXT = new Color(30, 31, 36);
    private static final Color MUTED_TEXT = new Color(107, 114, 128);

    private final Runnable onBack;
    private final Consumer<AccountRow> onEdit;
    private final Consumer<AccountRow> onDelete;
    private final Consumer<AccountRow> onRestore;

    private final JLabel titleLabel = createValueLabel("CHI TIẾT TÀI KHOẢN");
    private final JLabel accountIdLabel = createValueLabel("--");
    private final JLabel displayNameLabel = createValueLabel("--");
    private final JLabel phoneLabel = createValueLabel("--");
    private final JLabel emailLabel = createValueLabel("--");
    private final JLabel usernameLabel = createValueLabel("--");
    private final JLabel statusLabel = createValueLabel("--");
    private final JLabel roleLabel = createValueLabel("--");
    private final JLabel createdAtLabel = createValueLabel("--");
    private final JButton restoreButton = createActionButton("Khôi phục", new Color(220, 252, 231), new Color(22, 101, 52));
    private AccountRow currentRow;

    public AccountDetailPanel(Runnable onBack, Consumer<AccountRow> onEdit, Consumer<AccountRow> onDelete, Consumer<AccountRow> onRestore) {
        this.onBack = onBack;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        this.onRestore = onRestore;

        setLayout(new BorderLayout(0, 18));
        setBackground(PAGE_BACKGROUND);
        setBorder(new EmptyBorder(10, 0, 0, 0));
        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    void setCurrentRow(AccountRow currentRow) {
        this.currentRow = currentRow;
        restoreButton.setVisible(currentRow != null && currentRow.isDeleted());
    }

    void bindAccount(
            String accountId,
            String displayName,
            String phone,
            String email,
            String username,
            String status,
            LocalDateTime createdAt,
            String role,
            boolean deleted
    ) {
        titleLabel.setText("CHI TIẾT TÀI KHOẢN - " + valueOrDash(accountId));
        accountIdLabel.setText(valueOrDash(accountId));
        displayNameLabel.setText(valueOrDash(displayName));
        phoneLabel.setText(valueOrDash(phone));
        emailLabel.setText(valueOrDash(email));
        usernameLabel.setText(valueOrDash(username));
        statusLabel.setText(deleted ? "DELETED" : valueOrDash(status));
        roleLabel.setText(valueOrDash(role));
        createdAtLabel.setText(createdAt == null ? "--" : createdAt.format(DATE_FORMATTER));
        restoreButton.setVisible(deleted);
    }

    private JPanel createHeader() {
        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.setOpaque(false);

        JLabel backLabel = new JLabel("< Quay lại trang Quản lý tài khoản");
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
        titleRow.add(titleLabel, BorderLayout.WEST);

        headerWrapper.add(backRow);
        headerWrapper.add(titleRow);
        return headerWrapper;
    }

    private JPanel createMainContent() {
        JPanel wrapper = new JPanel(new BorderLayout(22, 0));
        wrapper.setOpaque(false);
        wrapper.add(createInfoCard(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createInfoCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new EmptyBorder(26, 32, 26, 32));

        card.add(createFieldBlock("ACCOUNT ID", accountIdLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("HỌ TÊN", displayNameLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("SĐT", phoneLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("EMAIL", emailLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("USERNAME", usernameLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("STATUS", statusLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("ROLE GROUP", roleLabel));
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldBlock("NGÀY TẠO", createdAtLabel));
        card.add(Box.createVerticalStrut(20));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton editButton = createActionButton("Chỉnh sửa", new Color(239, 246, 255), new Color(29, 78, 216));
        JButton deleteButton = createActionButton("Xóa", new Color(254, 226, 226), new Color(185, 28, 28));

        editButton.addActionListener(e -> {
            if (currentRow != null) {
                onEdit.accept(currentRow);
            }
        });
        deleteButton.addActionListener(e -> {
            if (currentRow != null) {
                onDelete.accept(currentRow);
            }
        });
        restoreButton.addActionListener(e -> {
            if (currentRow != null) {
                onRestore.accept(currentRow);
            }
        });
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(restoreButton);
        card.add(actions);
        return card;
    }

    private JPanel createFieldBlock(String label, JLabel valueLabel) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        fieldPanel.setOpaque(false);

        JLabel labelView = new JLabel(label);
        labelView.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelView.setForeground(new Color(92, 102, 77));

        JPanel valueWrapper = new JPanel(new BorderLayout());
        valueWrapper.setBackground(Color.WHITE);
        valueWrapper.setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(229, 231, 235)),
                new EmptyBorder(10, 16, 10, 16)
        ));
        valueWrapper.add(valueLabel, BorderLayout.CENTER);

        fieldPanel.add(labelView);
        fieldPanel.add(Box.createVerticalStrut(6));
        fieldPanel.add(valueWrapper);
        return fieldPanel;
    }

    private JButton createActionButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setForeground(fg);
        button.setFont(new Font("Lexend", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(DARK_TEXT);
        return label;
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "--" : value;
    }
}
