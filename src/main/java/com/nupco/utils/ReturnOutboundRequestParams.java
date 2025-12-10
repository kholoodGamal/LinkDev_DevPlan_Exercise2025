package com.nupco.utils;

public class ReturnOutboundRequestParams {
    private String connectionString;
    private String userName;
    private String password;
    private String iconTitle;
    private String orderId;
    private String requestedQty;
    private String desiredStatus;
    private Boolean fillOptionalFields;
    private String noteText;
    private String filePath;

    public ReturnOutboundRequestParams(String connectionString, String userName, String password, String iconTitle, String orderId ,String qty
            ,String desiredStatus, Boolean fillOptionalFields, String noteText,String filePath) {
        this.connectionString = connectionString;
        this.userName = userName;
        this.password = password;
        this.iconTitle = iconTitle;
        this.orderId = orderId ;
        this.requestedQty = qty;
        this.desiredStatus = desiredStatus;
        this.fillOptionalFields = fillOptionalFields;
        this.noteText = noteText;
        this.filePath = filePath;
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
    public String getOrderId() {
        return orderId;
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
    public String getFilePath(){ return  filePath;}


}
