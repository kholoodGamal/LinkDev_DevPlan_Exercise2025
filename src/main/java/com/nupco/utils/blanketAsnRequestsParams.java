package com.nupco.utils;

import java.time.LocalDate;

public class blanketAsnRequestsParams {
    private String connectionString;
    private String userName;
    private String password;
    private String invoiceNum;
    private String location;
    private String deliveryTime;
    private String batchNum;
    private String qty;
    private String desiredStatus;
    private Boolean fillOptionalFields;
    private String noteText;
    private String value;
    private String iconTitle;
    private LocalDate deliveryDateValue, manufacturingDateValue, expiryDateValue;

    // Constructor to initialize all fields
    public blanketAsnRequestsParams(String connectionString, String userName, String password, String iconTitle, String invoiceNum,
                                    String location, String deliveryTime, String batchNum, String qty
            , String desiredStatus, Boolean fillOptionalFields, String noteText, String value,
                                    LocalDate deliveryDateValue, LocalDate manufacturingDateValue,
                                    LocalDate expiryDateValue) {
        this.connectionString = connectionString;
        this.userName = userName;
        this.password = password;
        this.iconTitle = iconTitle;
        this.invoiceNum = invoiceNum;
        this.location = location;
        this.deliveryTime = deliveryTime;
        this.batchNum = batchNum;
        this.qty = qty;
        this.desiredStatus = desiredStatus;
        this.fillOptionalFields = fillOptionalFields;
        this.noteText = noteText;
        this.value = value;
        this.deliveryDateValue = deliveryDateValue;
        this.manufacturingDateValue = manufacturingDateValue;
        this.expiryDateValue = expiryDateValue;

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
    public String getLocation() {return location;}
    public String getInvoiceNum() {
        return invoiceNum;
    }
    public String getDeliveryTime() {
        return deliveryTime;
    }
    public String getBatchNum() {
        return batchNum;
    }
    public String getQty() {
        return qty;
    }
    public String getIconTitle() {return iconTitle;}
    public String getDesiredStatus() {
        return desiredStatus;
    }
    public boolean getFillOptionalFields() {
        return fillOptionalFields;
    }
    public String getNoteText() {
        return noteText;
    }
    public String getvalue() {
        return value;
    }
    public LocalDate getDeliveryDateValue() {return deliveryDateValue;}
    public LocalDate getManufacturingDateValue() {return manufacturingDateValue;}
    public LocalDate getExpiryDateValue() {return expiryDateValue;}


}
