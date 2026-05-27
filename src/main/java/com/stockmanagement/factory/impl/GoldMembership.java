package com.stockmanagement.factory.impl;

import com.stockmanagement.factory.MembershipLevel;

/**
 * Gold Membership Level - Premium Tier
 * Factory Pattern - Concrete Product
 */
public class GoldMembership implements MembershipLevel {

    private static final String LEVEL_NAME = "Gold";
    private static final double DISCOUNT_PERCENTAGE = 10.0;
    private static final double POINTS_MULTIPLIER = 2.0;
    private static final double MINIMUM_SPENDING = 5000.0;
    private static final double UPGRADE_THRESHOLD = 10000.0;

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
        return "Platinum";
    }

    @Override
    public String getBenefitsDescription() {
        return "Gold Member Benefits: 10% discount on all purchases, " +
                "2.0x points multiplier, free shipping, exclusive early access to sales. " +
                "Spend $10,000+ to unlock Platinum - our highest tier!";
    }

    @Override
    public String toString() {
        return String.format("GoldMembership[discount=%.1f%%, points=%.1fx, minSpending=$%.2f]",
                DISCOUNT_PERCENTAGE, POINTS_MULTIPLIER, MINIMUM_SPENDING);
    }
}
