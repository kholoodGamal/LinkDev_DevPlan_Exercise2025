package com.nupco.utils;

public class OutboundRequestParams {
    private String connectionString;
    private String userName;
    private String password;
    private String iconTitle;
    private String requestedQty;
    private String desiredStatus;
    private Boolean fillOptionalFields;
    private String noteText;
    private String value;

    public OutboundRequestParams(String connectionString, String userName, String password, String iconTitle, String qty
            , String desiredStatus, Boolean fillOptionalFields, String noteText, String value) {
        this.connectionString = connectionString;
        this.userName = userName;
        this.password = password;
        this.iconTitle = iconTitle;
        this.requestedQty = qty;
        this.desiredStatus = desiredStatus;
        this.fillOptionalFields = fillOptionalFields;
        this.noteText = noteText;
        this.value = value;
    }

    // Getters (No setters needed for this use case)
    public String getConnectionString() {
        return connectionString;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getIconTitle() {
        return iconTitle;
    }

    public String getRequestedQty() {
        return requestedQty;
    }

    public String getDesiredStatus() {
        return desiredStatus;
    }

    public boolean getFillOptionalFields() {
        return fillOptionalFields;
    }

    public String getNoteText() {
        return noteText;
    }

    public String getValue() {
        return value;
    }

}
