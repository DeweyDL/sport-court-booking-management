package com.sportcourt.modules.account.dto;

public class RoleGroupOption {
    private String groupId;
    private String groupName;

    public RoleGroupOption() {
    }

    public RoleGroupOption(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return groupId + " - " + groupName;
    }
}
