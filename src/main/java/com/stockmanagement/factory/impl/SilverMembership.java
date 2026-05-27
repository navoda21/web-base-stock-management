package com.stockmanagement.factory.impl;

import com.stockmanagement.factory.MembershipLevel;

/**
 * Silver Membership Level - Bronze Tier
 * Factory Pattern - Concrete Product
 */
public class SilverMembership implements MembershipLevel {

    private static final String LEVEL_NAME = "Silver";
    private static final double DISCOUNT_PERCENTAGE = 5.0;
    private static final double POINTS_MULTIPLIER = 1.5;
    private static final double MINIMUM_SPENDING = 1000.0;
    private static final double UPGRADE_THRESHOLD = 5000.0;

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
        return "Gold";
    }

    @Override
    public String getBenefitsDescription() {
        return "Silver Member Benefits: 5% discount on all purchases, " +
                "1.5x points multiplier, priority customer support. " +
                "Spend $5,000+ to unlock Gold membership!";
    }

    @Override
    public String toString() {
        return String.format("SilverMembership[discount=%.1f%%, points=%.1fx, minSpending=$%.2f]",
                DISCOUNT_PERCENTAGE, POINTS_MULTIPLIER, MINIMUM_SPENDING);
    }
}
