package com.nupco.utils;

import java.time.LocalDate;

public class InboundRequestsParams {
    private String connectionString;
    private String userName;
    private String password;
    private String iconTitle;
    private String region;
    private String customerName;
    private String sloc;
    private String invoiceNum;
    private String deliveryTime;
    private String palletsNum;
    private String batchNum;
    private String qty;
    private String desiredStatus;
    private Boolean fillOptionalFields;
    private String noteText;
    private String value;
    private LocalDate manufacturingDateValue ,  expiryDateValue ,  deliveryDateValue;

    // Constructor to initialize all fields
    public InboundRequestsParams(String connectionString, String userName, String password, String iconTitle,
                                 String region, String customerName, String sloc, String invoiceNum,
                                 String deliveryTime, String palletsNum, String batchNum, String qty
            , String desiredStatus, Boolean fillOptionalFields, String noteText, String value,LocalDate manufacturingDateValue , LocalDate expiryDateValue , LocalDate deliveryDateValue) {
        this.connectionString = connectionString;
        this.userName = userName;
        this.password = password;
        this.iconTitle = iconTitle;
        this.region = region;
        this.customerName = customerName;
        this.sloc = sloc;
        this.invoiceNum = invoiceNum;
        this.deliveryTime = deliveryTime;
        this.palletsNum = palletsNum;
        this.batchNum = batchNum;
        this.qty = qty;
        this.desiredStatus = desiredStatus;
        this.fillOptionalFields = fillOptionalFields;
        this.noteText = noteText;
        this.value = value;
        this.deliveryDateValue= deliveryDateValue;
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
    public String getIconTitle() {
        return iconTitle;
    }
    public String getRegion() {
        return region;
    }
    public String getCustomerName() {
        return customerName;
    }
    public String getSloc() {
        return sloc;
    }
    public String getInvoiceNum() {
        return invoiceNum;
    }
    public String getDeliveryTime() {
        return deliveryTime;
    }
    public String getPalletsNum() {
        return palletsNum;
    }
    public String getBatchNum() {
        return batchNum;
    }
    public String getQty() {
        return qty;
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
    public String getvalue() {
        return value;
    }
    public LocalDate getDeliveryDate() {
        return deliveryDateValue;
    }
    public LocalDate getManufacturingDate() {return manufacturingDateValue;}
    public LocalDate getExpiryDate() {
        return expiryDateValue;
    }


}
