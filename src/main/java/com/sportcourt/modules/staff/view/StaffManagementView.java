package com.sportcourt.modules.staff.view;

import com.sportcourt.modules.staff.dto.StaffCreateRequest;
import com.sportcourt.modules.staff.dto.StaffDetailResponse;
import com.sportcourt.modules.staff.dto.StaffResponse;
import com.sportcourt.modules.staff.dto.StaffSearchCriteria;
import com.sportcourt.modules.staff.dto.StaffUpdateRequest;
import com.sportcourt.modules.staff.service.StaffPermissionService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.List;

public class StaffManagementView extends JPanel {
    private final StaffPermissionService permissionService = new StaffPermissionService();

    private JTextField txtKeyword;
    private JComboBox<String> cboBranch;
    private JComboBox<String> cboStaffType;

    private JTable tblStaff;
    private StaffTableModel tableModel;

    private JButton btnSearch;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnDetail;
    private JButton btnRefresh;

    public StaffManagementView() {
        initComponents();
        initLayout();
        applyPermission();
    }

    private void initComponents() {
        txtKeyword = new JTextField(20);

        cboBranch = new JComboBox<>();
        cboBranch.addItem("Tất cả");

        cboStaffType = new JComboBox<>();
        cboStaffType.addItem("Tất cả");

        tableModel = new StaffTableModel();
        tblStaff = new JTable(tableModel);
        tblStaff.setRowHeight(32);
        tblStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStaff.setFillsViewportHeight(true);
        tblStaff.setAutoCreateRowSorter(true);

        btnSearch = StaffTheme.primaryButton("Tìm kiếm");
        btnAdd = StaffTheme.primaryButton("Thêm mới");
        btnUpdate = StaffTheme.secondaryButton("Cập nhật");
        btnDelete = StaffTheme.dangerButton("Xoá");
        btnDetail = StaffTheme.secondaryButton("Xem chi tiết");
        btnRefresh = StaffTheme.secondaryButton("Làm mới");
    }

    private void initLayout() {
        setLayout(new BorderLayout(12, 12));
        setBackground(StaffTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Quản lý nhân viên");
        title.setFont(StaffTheme.fontBold(24));
        title.setForeground(StaffTheme.TEXT);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(title, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        StaffTheme.applyCard(filterPanel);
        filterPanel.add(new JLabel("Từ khoá:"));
        filterPanel.add(txtKeyword);
        filterPanel.add(new JLabel("Chi nhánh:"));
        filterPanel.add(cboBranch);
        filterPanel.add(new JLabel("Vị trí:"));
        filterPanel.add(cboStaffType);
        filterPanel.add(btnSearch);
        filterPanel.add(btnRefresh);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        StaffTheme.applyCard(actionPanel);
        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnDetail);

        JPanel topPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        topPanel.setOpaque(false);
        topPanel.add(headerPanel);
        topPanel.add(filterPanel);
        topPanel.add(actionPanel);

        JScrollPane scrollPane = new JScrollPane(tblStaff);
        scrollPane.setBorder(BorderFactory.createLineBorder(StaffTheme.BORDER));

        JPanel tablePanel = new JPanel(new BorderLayout());
        StaffTheme.applyCard(tablePanel);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void applyPermission() {
        btnAdd.setVisible(permissionService.canAdd());
        btnUpdate.setVisible(permissionService.canEdit());
        btnDelete.setVisible(permissionService.canDelete());
    }

    public StaffSearchCriteria getSearchCriteria() {
        StaffSearchCriteria criteria = new StaffSearchCriteria();

        criteria.setKeyword(txtKeyword.getText().trim());

        Object selectedBranch = cboBranch.getSelectedItem();
        if (selectedBranch != null) {
            String maCn = selectedBranch.toString().trim();

            if (!maCn.isEmpty() && !maCn.equalsIgnoreCase("Tất cả")) {
                criteria.setMaCn(maCn);
            }
        }

        Object selectedStaffType = cboStaffType.getSelectedItem();
        if (selectedStaffType != null) {
            String maLoaiNv = selectedStaffType.toString().trim();

            if (!maLoaiNv.isEmpty() && !maLoaiNv.equalsIgnoreCase("Tất cả")) {
                criteria.setMaLoaiNv(maLoaiNv);
            }
        }

        return criteria;
    }

    public void showStaffTable(List<StaffResponse> staffList) {
        tableModel.setData(staffList);
    }

    public String getSelectedStaffId() {
        int row = tblStaff.getSelectedRow();

        if (row < 0) {
            return null;
        }

        int modelRow = tblStaff.convertRowIndexToModel(row);
        StaffResponse staff = tableModel.getStaffAt(modelRow);

        if (staff == null) {
            return null;
        }

        return staff.getMaNv();
    }

    public StaffCreateRequest showCreateDialog() {
        StaffFormDialog dialog = StaffFormDialog.createMode();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return dialog.getCreateRequest();
    }

    public StaffUpdateRequest showUpdateDialog(StaffDetailResponse detail) {
        StaffFormDialog dialog = StaffFormDialog.updateMode(detail);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return dialog.getUpdateRequest();
    }

    public void showDetailDialog(StaffDetailResponse detail) {
        StaffDetailDialog dialog = new StaffDetailDialog(detail);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirm(String message) {
        int result = JOptionPane.showConfirmDialog(
                this,
                message,
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        return result == JOptionPane.YES_OPTION;
    }

    public void setSearchAction(ActionListener listener) {
        btnSearch.addActionListener(listener);
    }

    public void setAddAction(ActionListener listener) {
        btnAdd.addActionListener(listener);
    }

    public void setUpdateAction(ActionListener listener) {
        btnUpdate.addActionListener(listener);
    }

    public void setDeleteAction(ActionListener listener) {
        btnDelete.addActionListener(listener);
    }

    public void setViewDetailAction(ActionListener listener) {
        btnDetail.addActionListener(listener);
    }

    public void setRefreshAction(ActionListener listener) {
        btnRefresh.addActionListener(listener);
    }
}
