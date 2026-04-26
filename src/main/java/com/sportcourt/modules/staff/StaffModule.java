package com.sportcourt.modules.staff;

import com.sportcourt.modules.staff.controller.StaffController;
import com.sportcourt.modules.staff.view.StaffManagementView;

import javax.swing.JPanel;

public final class StaffModule {
    private StaffModule() {
    }

    public static JPanel createPanel() {
        StaffManagementView view = new StaffManagementView();
        new StaffController(view);
        return view;
    }
}
