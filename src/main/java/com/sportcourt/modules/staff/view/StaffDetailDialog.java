package com.sportcourt.modules.staff.view;
import com.sportcourt.modules.staff.dto.*;
import javax.swing.*;


public class StaffDetailDialog extends JDialog {
    private StaffDetailResponse detail;

    private JLabel lblMaNv;
    private JLabel lblHoTen;
    private JLabel lblNgaySinh;
    private JLabel lblSdt;
    private JLabel lblEmail;
    private JLabel lblDiaChi;

    private JLabel lblMaCn;
    private JLabel lblDiaChiChiNhanh;
    private JLabel lblMaLoaiNv;
    private JLabel lblViTri;
    private JLabel lblMucLuong;
    private JLabel lblNgayVaoLam;
    private JLabel lblCccd;
    private JLabel lblQuanLy;

    private JButton btnClose;

    public StaffDetailDialog(StaffDetailResponse detail) {
        this.detail = detail;

        setModal(true);
        setTitle("Chi tiết nhân viên");
        setSize(520, 560);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
        initLayout();
        fillData();
        initEvents();
    }

    private void initComponents() {
        lblMaNv = new JLabel();
        lblHoTen = new JLabel();
        lblNgaySinh = new JLabel();
        lblSdt = new JLabel();
        lblEmail = new JLabel();
        lblDiaChi = new JLabel();

        lblMaCn = new JLabel();
        lblDiaChiChiNhanh = new JLabel();
        lblMaLoaiNv = new JLabel();
        lblViTri = new JLabel();
        lblMucLuong = new JLabel();
        lblNgayVaoLam = new JLabel();
        lblCccd = new JLabel();
        lblQuanLy = new JLabel();

        btnClose = new JButton("Đóng");
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel contentPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        addSectionTitle(contentPanel, gbc, row++, "Thông tin cá nhân");

        addRow(contentPanel, gbc, row++, "Mã nhân viên:", lblMaNv);
        addRow(contentPanel, gbc, row++, "Họ tên:", lblHoTen);
        addRow(contentPanel, gbc, row++, "Ngày sinh:", lblNgaySinh);
        addRow(contentPanel, gbc, row++, "Số điện thoại:", lblSdt);
        addRow(contentPanel, gbc, row++, "Email:", lblEmail);
        addRow(contentPanel, gbc, row++, "Địa chỉ:", lblDiaChi);

        addSectionTitle(contentPanel, gbc, row++, "Thông tin công việc");

        addRow(contentPanel, gbc, row++, "Mã chi nhánh:", lblMaCn);
        addRow(contentPanel, gbc, row++, "Địa chỉ chi nhánh:", lblDiaChiChiNhanh);
        addRow(contentPanel, gbc, row++, "Mã loại nhân viên:", lblMaLoaiNv);
        addRow(contentPanel, gbc, row++, "Vị trí:", lblViTri);
        addRow(contentPanel, gbc, row++, "Mức lương:", lblMucLuong);
        addRow(contentPanel, gbc, row++, "Ngày vào làm:", lblNgayVaoLam);
        addRow(contentPanel, gbc, row++, "CCCD:", lblCccd);
        addRow(contentPanel, gbc, row++, "Là quản lý:", lblQuanLy);

        buttonPanel.add(btnClose);

        mainPanel.add(new JScrollPane(contentPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addSectionTitle(JPanel panel, GridBagConstraints gbc, int row, String title) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(label, gbc);

        gbc.gridwidth = 1;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String title, JLabel valueLabel) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;

        panel.add(valueLabel, gbc);
    }

    private void fillData() {
        lblMaNv.setText(safe(detail.getMaNv()));
        lblHoTen.setText(safe(detail.getHoTen()));
        lblNgaySinh.setText(detail.getNgaySinh() == null ? "" : detail.getNgaySinh().toString());
        lblSdt.setText(safe(detail.getSdt()));
        lblEmail.setText(safe(detail.getEmail()));
        lblDiaChi.setText(safe(detail.getDiaChi()));

        lblMaCn.setText(safe(detail.getMaCn()));
        lblDiaChiChiNhanh.setText(safe(detail.getDiaChiChiNhanh()));
        lblMaLoaiNv.setText(safe(detail.getMaLoaiNv()));
        lblViTri.setText(safe(detail.getViTri()));
        lblMucLuong.setText(detail.getMucLuong() == null ? "" : detail.getMucLuong().toPlainString());
        lblNgayVaoLam.setText(detail.getNgayVaoLam() == null ? "" : detail.getNgayVaoLam().toString());
        lblCccd.setText(safe(detail.getCccd()));
        lblQuanLy.setText(detail.isQuanLy() ? "Có" : "Không");
    }

    private void initEvents() {
        btnClose.addActionListener(e -> dispose());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}