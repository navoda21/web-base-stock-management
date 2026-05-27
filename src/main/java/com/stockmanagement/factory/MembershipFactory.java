package com.stockmanagement.factory;

import com.stockmanagement.factory.impl.GoldMembership;
import com.stockmanagement.factory.impl.PlatinumMembership;
import com.stockmanagement.factory.impl.SilverMembership;
import com.stockmanagement.factory.impl.StandardMembership;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating MembershipLevel instances
 * Factory Pattern - Creator
 */
@Component
public class MembershipFactory {

    /**
     * Create a MembershipLevel object based on the level name
     *
     * @param levelName The membership level name ("Standard", "Silver", "Gold", "Platinum")
     * @return MembershipLevel instance
     * @throws IllegalArgumentException if level name is invalid
     */
    public MembershipLevel createMembership(String levelName) {
        if (levelName == null || levelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Membership level name cannot be null or empty");
        }

        String normalizedLevel = levelName.trim().toLowerCase();

        switch (normalizedLevel) {
            case "standard":
                return new StandardMembership();
            case "silver":
                return new SilverMembership();
            case "gold":
                return new GoldMembership();
            case "platinum":
                return new PlatinumMembership();
            default:
                throw new IllegalArgumentException(
                        "Invalid membership level: " + levelName +
                                ". Valid levels are: Standard, Silver, Gold, Platinum"
                );
        }
    }

    /**
     * Create membership based on total spending
     * Automatically determines the appropriate level
     *
     * @param totalSpending Customer's total spending
     * @return Appropriate MembershipLevel instance
     */
    public MembershipLevel createMembershipBySpending(double totalSpending) {
        if (totalSpending < 0) {
            throw new IllegalArgumentException("Total spending cannot be negative");
        }

        if (totalSpending >= 10000) {
            return new PlatinumMembership();
        } else if (totalSpending >= 5000) {
            return new GoldMembership();
        } else if (totalSpending >= 1000) {
            return new SilverMembership();
        } else {
            return new StandardMembership();
        }
    }

    /**
     * Get the next level membership
     *
     * @param currentLevel Current membership level name
     * @return Next level MembershipLevel instance
     * @throws IllegalArgumentException if current level is invalid or already at maximum
     */
    public MembershipLevel getNextLevel(String currentLevel) {
        MembershipLevel current = createMembership(currentLevel);
        String nextLevelName = current.getNextLevelName();

        if ("Maximum Level Reached".equals(nextLevelName)) {
            throw new IllegalArgumentException("Already at maximum membership level");
        }

        return createMembership(nextLevelName);
    }

    /**
     * Check if upgrade is available for the customer
     *
     * @param currentLevel  Current membership level
     * @param totalSpending Customer's total spending
     * @return true if upgrade is available
     */
    public boolean canUpgrade(String currentLevel, double totalSpending) {
        MembershipLevel membership = createMembership(currentLevel);
        return membership.isEligibleForUpgrade(totalSpending);
    }

    /**
     * Get recommended membership level based on spending
     *
     * @param totalSpending Customer's total spending
     * @return Recommended level name
     */
    public String getRecommendedLevel(double totalSpending) {
        MembershipLevel membership = createMembershipBySpending(totalSpending);
        return membership.getLevelName();
    }
}
