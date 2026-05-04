package com.sportcourt.modules.auth.dto;

public class Permission {
    private final String functionId;
    private final boolean canView;
    private final boolean canAdd;
    private final boolean canEdit;
    private final boolean canDelete;
    private final boolean canDownload;

    public Permission(String functionId,
                      boolean canView,
                      boolean canAdd,
                      boolean canEdit,
                      boolean canDelete,
                      boolean canDownload
    ) {
        this.functionId = functionId;
        this.canView = canView;
        this.canAdd = canAdd;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canDownload = canDownload;
    }

    public String getFunctionId() {
        return functionId;
    }

    public boolean allows(PermissionAction action) {
        return switch (action) {
            case VIEW -> canView;
            case ADD -> canAdd;
            case EDIT -> canEdit;
            case DELETE -> canDelete;
            case DOWNLOAD -> canDownload;
        };
    }
}
