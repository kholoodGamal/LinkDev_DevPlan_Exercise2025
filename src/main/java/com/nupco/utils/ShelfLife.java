package com.nupco.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ShelfLife {
    public static double calculateShelfLifePercentage(LocalDate manufacturingDate, LocalDate expiryDate, LocalDate deliveryDate) {
        long totalShelfDays = ChronoUnit.DAYS.between(manufacturingDate, expiryDate);
        long remainingShelfDays = ChronoUnit.DAYS.between(deliveryDate, expiryDate);

        if (totalShelfDays <= 0) {
            throw new IllegalArgumentException("Expiry date must be after manufacturing date.");
        }

        double shelfLifePercent = ((double) remainingShelfDays / totalShelfDays) * 100;
        return Math.max(0, Math.min(100, shelfLifePercent));
    }

    public static LocalDate ensureValidShelfLife(LocalDate manufacturingDate, LocalDate expiryDate, LocalDate deliveryDate) {
        double shelfLifePercent = calculateShelfLifePercentage(manufacturingDate, expiryDate, deliveryDate);
        long totalShelfDays = ChronoUnit.DAYS.between(manufacturingDate, expiryDate);

        if (totalShelfDays <= 0) {
            throw new IllegalArgumentException(" Expiry date must be after manufacturing date.");
        }

        // Handle edge conditions
        if (shelfLifePercent <= 0) {
            System.out.printf(" Product already expired! Delivery date (%s) is on or after expiry (%s)%n",
                    deliveryDate, expiryDate);
            throw new IllegalArgumentException("Delivery date cannot be on or after expiry date.");
        }

        if (shelfLifePercent > 100) {
            System.out.printf(" Invalid shelf life %.2f%% — delivery date (%s) is before manufacturing date (%s)%n",
                    shelfLifePercent, deliveryDate, manufacturingDate);
            throw new IllegalArgumentException("Delivery date cannot be before manufacturing date.");
        }

        // Otherwise, accept it as valid
        System.out.printf(" Shelf life valid: %.2f%% (MFG: %s | EXP: %s | DEL: %s)%n",
                shelfLifePercent, manufacturingDate, expiryDate, deliveryDate);

        return deliveryDate;
    }


}

