package com.example.demo.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

// Final class to prevent extension
public final class BDH {

    // Private constructor to prevent instantiation

    private void BigDecimalUtils() {}
    // --- COMMON CONSTANTS ---

    public static final BigDecimal zero = BigDecimal.ZERO;
    public static final BigDecimal one = BigDecimal.ONE;
    // Default scale for division operations to maintain consistency

    private static final int d_scale = 4; // Matches your numeric(10,4)
    private static final RoundingMode DRM = RoundingMode.HALF_UP;


    // --- COMPARISON METHODS ---
    /**
     * Checks if the first value is less than the second. Handles nulls gracefully.
     * A null value is treated as zero for comparison.
     */
    public static boolean lessThan(BigDecimal a, BigDecimal b) {
        BigDecimal valA = Objects.requireNonNullElse(a, zero);
        BigDecimal valB = Objects.requireNonNullElse(b, zero);
        return valA.compareTo(valB) < 0;
    }

    /**
     * Checks if the first value is greater than the second. Handles nulls gracefully.
     */
    public static boolean greaterThan(BigDecimal a, BigDecimal b) {
        BigDecimal valA = Objects.requireNonNullElse(a, zero);
        BigDecimal valB = Objects.requireNonNullElse(b, zero);
        return valA.compareTo(valB) > 0;
    }

    /**
     * Checks if two values are numerically equal. Handles nulls and different scales.
     * This is the correct way to check for equality, NOT .equals().
     */
    public static boolean isEqual(BigDecimal a, BigDecimal b) {
        BigDecimal valA = Objects.requireNonNullElse(a, zero);
        BigDecimal valB = Objects.requireNonNullElse(b, zero);
        return valA.compareTo(valB) == 0;
    }


    // --- ZERO CHECKS ---
    /**
     * Checks if a value is numerically equal to zero.
     */
    public static boolean isZero(BigDecimal value) {
        return isEqual(value, zero);
    }

    /**
     * Checks if a value is greater than zero.
     */
    public static boolean isPositive(BigDecimal value) {
        return greaterThan(value, zero);
    }

    /**
     * Checks if a value is less than zero.
     */
    public static boolean isNegative(BigDecimal value) {
        return lessThan(value, zero);
    }


    // --- SAFE ARITHMETIC ---
    // These methods can be expanded to handle nulls as well
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        BigDecimal valA = Objects.requireNonNullElse(a, zero);
        BigDecimal valB = Objects.requireNonNullElse(b, zero);
        return valA.add(valB);
    }

    public static BigDecimal mult(BigDecimal a, BigDecimal b) {
        BigDecimal valA = Objects.requireNonNullElse(a, zero);
        BigDecimal valB = Objects.requireNonNullElse(b, zero);
        return valA.multiply(valB);
    }

    public static BigDecimal mult(BigDecimal ingredientQuantity, int quantity) {
        return mult(ingredientQuantity, BigDecimal.valueOf(quantity));
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        BigDecimal valA = Objects.requireNonNullElse(a, zero);
        BigDecimal valB = Objects.requireNonNullElse(b, zero);
        return valA.subtract(valB);
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        return divide(a, b, d_scale, DRM);
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale, RoundingMode roundingMode) {
        BigDecimal valA = Objects.requireNonNullElse(a, zero);
        BigDecimal valB = Objects.requireNonNullElse(b, zero);
        if (isZero(valB)) {
            // Or throw a more specific exception
            throw new IllegalArgumentException("Division by zero is not allowed.");
        }
        return valA.divide(valB, scale, roundingMode);
    }
}