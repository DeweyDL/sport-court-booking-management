package com.sportcourt.modules.staff.view;
import com.sportcourt.modules.staff.dto.*;
import javax.swing.*;

public class StaffManagementView extends JPanel {
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
        cboStaffType = new JComboBox<>();

        tableModel = new StaffTableModel();
        tblStaff = new JTable(tableModel);

        btnSearch = new JButton("Tìm kiếm");
        btnAdd = new JButton("Thêm mới");
        btnUpdate = new JButton("Cập nhật");
        btnDelete = new JButton("Xoá");
        btnDetail = new JButton("Xem chi tiết");
        btnRefresh = new JButton("Làm mới");
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Từ khoá:"));
        filterPanel.add(txtKeyword);
        filterPanel.add(new JLabel("Chi nhánh:"));
        filterPanel.add(cboBranch);
        filterPanel.add(new JLabel("Vị trí:"));
        filterPanel.add(cboStaffType);
        filterPanel.add(btnSearch);
        filterPanel.add(btnRefresh);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnDetail);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(filterPanel, BorderLayout.NORTH);
        topPanel.add(actionPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(tblStaff), BorderLayout.CENTER);
    }

    private void applyPermission() {
        btnAdd.setVisible(UserSession.getInstance().hasPermission("STAFF_MANAGEMENT", "ADD"));
        btnUpdate.setVisible(UserSession.getInstance().hasPermission("STAFF_MANAGEMENT", "EDIT"));
        btnDelete.setVisible(UserSession.getInstance().hasPermission("STAFF_MANAGEMENT", "DELETE"));
    }

    public StaffSearchCriteria getSearchCriteria() {
        StaffSearchCriteria criteria = new StaffSearchCriteria();
        criteria.setKeyword(txtKeyword.getText().trim());
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

        return tableModel.getStaffAt(row).getMaNv();
    }

    public StaffCreateRequest showCreateDialog() {
        StaffFormDialog dialog = StaffFormDialog.createMode();
        dialog.setVisible(true);
        return dialog.getCreateRequest();
    }

    public StaffUpdateRequest showUpdateDialog(String maNv) {
        StaffFormDialog dialog = StaffFormDialog.updateMode(maNv);
        dialog.setVisible(true);
        return dialog.getUpdateRequest();
    }

    public void showDetailDialog(StaffDetailResponse detail) {
        StaffDetailDialog dialog = new StaffDetailDialog(detail);
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