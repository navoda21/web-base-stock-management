package com.stockmanagement.factory;

/**
 * Interface for different membership levels
 * Factory Pattern - Product Interface
 */
public interface MembershipLevel {

    /**
     * Get the name of the membership level
     *
     * @return Membership level name (e.g., "Standard", "Silver", "Gold", "Platinum")
     */
    String getLevelName();

    /**
     * Get the discount percentage for this membership level
     *
     * @return Discount percentage (0-100)
     */
    double getDiscountPercentage();

    /**
     * Get the points multiplier for this membership level
     *
     * @return Points multiplier (e.g., 1.0x, 1.5x, 2.0x, 3.0x)
     */
    double getPointsMultiplier();

    /**
     * Get the minimum spending required to maintain this level
     *
     * @return Minimum spending amount
     */
    double getMinimumSpending();

    /**
     * Check if customer qualifies for upgrade based on total spending
     *
     * @param totalSpending Customer's total spending
     * @return true if eligible for upgrade
     */
    boolean isEligibleForUpgrade(double totalSpending);

    /**
     * Get the next membership level name
     *
     * @return Next level name or "Maximum Level Reached"
     */
    String getNextLevelName();

    /**
     * Get membership benefits description
     *
     * @return Description of benefits
     */
    String getBenefitsDescription();
}
