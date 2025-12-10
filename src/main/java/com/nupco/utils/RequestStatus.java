package com.nupco.utils;

public final class RequestStatus {
    private RequestStatus() {} // prevent instantiation

    public static final String NEW = "New";
    public static final String Created = "Created";
    public static final String DRAFTED = "Drafted";
    public static final String APPROVED = "Approved";
    public static final String REJECTED = "Rejected";
    public static final String CANCELLED = "Cancelled";
    public static final String EDIT_REQUESTED = "EditRequestedByApprovalUser";
    public static final String DeliveredRequestStatus = "Delivered";
    public static final String physicallyReceivedStatus = "PhysicallyReceived";
    public static final String ApprovedByApprovalUser = "ApprovedByApproval";
    public static final String L1_APPROVED = "L1 Approved";
    public static final String INBOUND_APPROVED = "ApprovedByInboundUser";


}
