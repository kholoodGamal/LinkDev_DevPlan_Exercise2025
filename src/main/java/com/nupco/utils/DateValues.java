package com.nupco.utils;

import java.time.LocalDate;

public class DateValues {
    public static final LocalDate normalDeliveryDate = LocalDate.now().plusDays(1);
    public static final LocalDate updatedDeliveryDateForOutbound = LocalDate.now().plusDays(2);

    public static final LocalDate today = LocalDate.now();

    // ---------- High Shelf Life (> 66%) ------------
    public static final LocalDate manufacturingDateHigh = LocalDate.now().minusDays(2);
    public static final LocalDate expiryDateHigh = manufacturingDateHigh.plusMonths(12);
    public static final LocalDate deliveryDateHigh = manufacturingDateHigh.plusDays(90);

    // ---------- Medium Shelf Life (50%–66%) ----------
    public static final LocalDate manufacturingDateMedium = LocalDate.now().minusDays(2);
    public static final LocalDate expiryDateMedium = manufacturingDateMedium.plusMonths(12);
    public static final LocalDate deliveryDateMedium = manufacturingDateMedium.plusDays(150);

    // ---------- Expired Dates which User can't make any request with these dates ----------
    public static final LocalDate expiredManufacturingDate = LocalDate.now().minusDays(2);
    public static final LocalDate expiredExpiryDate = expiredManufacturingDate.plusDays(250);
    public static final LocalDate expiredDeliveryDate = expiredManufacturingDate.plusDays(250);

}

