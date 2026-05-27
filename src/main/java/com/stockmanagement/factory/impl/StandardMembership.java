package com.stockmanagement.factory.impl;

import com.stockmanagement.factory.MembershipLevel;

/**
 * Standard Membership Level - Entry Level
 * Factory Pattern - Concrete Product
 */
public class StandardMembership implements MembershipLevel {

    private static final String LEVEL_NAME = "Standard";
    private static final double DISCOUNT_PERCENTAGE = 0.0;
    private static final double POINTS_MULTIPLIER = 1.0;
    private static final double MINIMUM_SPENDING = 0.0;
    private static final double UPGRADE_THRESHOLD = 1000.0;

    @Override
    public String getLevelName() {
        return LEVEL_NAME;
    }

    @Override
    public double getDiscountPercentage() {
        return DISCOUNT_PERCENTAGE;
    }

    @Override
    public double getPointsMultiplier() {
        return POINTS_MULTIPLIER;
    }

    @Override
    public double getMinimumSpending() {
        return MINIMUM_SPENDING;
    }

    @Override
    public boolean isEligibleForUpgrade(double totalSpending) {
        return totalSpending >= UPGRADE_THRESHOLD;
    }

    @Override
    public String getNextLevelName() {
        return "Silver";
    }

    @Override
    public String getBenefitsDescription() {
        return "Welcome to our store! Start earning points with every purchase. " +
                "Spend $1,000+ to unlock Silver membership benefits.";
    }

    @Override
    public String toString() {
        return String.format("StandardMembership[discount=%.1f%%, points=%.1fx, minSpending=$%.2f]",
                DISCOUNT_PERCENTAGE, POINTS_MULTIPLIER, MINIMUM_SPENDING);
    }
}
