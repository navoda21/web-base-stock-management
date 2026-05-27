package com.stockmanagement.factory.impl;

import com.stockmanagement.factory.MembershipLevel;

/**
 * Platinum Membership Level - VIP Tier
 * Factory Pattern - Concrete Product
 */
public class PlatinumMembership implements MembershipLevel {

    private static final String LEVEL_NAME = "Platinum";
    private static final double DISCOUNT_PERCENTAGE = 15.0;
    private static final double POINTS_MULTIPLIER = 3.0;
    private static final double MINIMUM_SPENDING = 10000.0;

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
        return false; // Platinum is the highest level
    }

    @Override
    public String getNextLevelName() {
        return "Maximum Level Reached";
    }

    @Override
    public String getBenefitsDescription() {
        return "Platinum VIP Benefits: 15% discount on all purchases, " +
                "3.0x points multiplier, free expedited shipping, " +
                "dedicated account manager, exclusive invitations to VIP events, " +
                "birthday gifts, and personalized shopping experience. " +
                "You're at our highest tier - enjoy premium privileges!";
    }

    @Override
    public String toString() {
        return String.format("PlatinumMembership[discount=%.1f%%, points=%.1fx, minSpending=$%.2f, VIP]",
                DISCOUNT_PERCENTAGE, POINTS_MULTIPLIER, MINIMUM_SPENDING);
    }
}
