package com.stockmanagement.config;

import com.stockmanagement.observer.impl.*;
import com.stockmanagement.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to register all Stock Observers
 * Observer Pattern - Spring Configuration
 */
@Configuration
public class ObserverConfig {

    @Autowired
    private ItemService itemService;

    @Autowired
    private LowStockAlertObserver lowStockAlertObserver;

    @Autowired
    private EmailNotificationObserver emailNotificationObserver;

    @Autowired
    private AuditLogObserver auditLogObserver;

    @Autowired
    private DashboardUpdateObserver dashboardUpdateObserver;

    @Autowired
    private AutoReorderObserver autoReorderObserver;

    /**
     * Register all observers on application startup
     */
    @Bean
    public CommandLineRunner registerStockObservers() {
        return args -> {
            System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║      🔧 Registering Stock Observers (Observer Pattern)    ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝");

            // Register all observers
            itemService.registerObserver(lowStockAlertObserver);
            itemService.registerObserver(emailNotificationObserver);
            itemService.registerObserver(auditLogObserver);
            itemService.registerObserver(dashboardUpdateObserver);
            itemService.registerObserver(autoReorderObserver);

            System.out.println("╔═══════════════════════════════════════════════════════════╗");
            System.out.println("║  ✅ All observers registered successfully!                ║");
            System.out.println("║  📊 5 observers are now monitoring stock changes          ║");
            System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
        };
    }
}
