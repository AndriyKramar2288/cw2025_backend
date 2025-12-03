package com.banew.cw2025_backend_core.backend.utils.tstats;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile("dev")
public class TransactionStatsLogger {
    private static final Logger log = LoggerFactory.getLogger(TransactionStatsLogger.class);

    private final SessionFactory sessionFactory;
    private final AtomicLong initialQueryCount = new AtomicLong(0);

    public TransactionStatsLogger(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        // Увімкнути статистику програмно, на випадок якщо її не увімкнено в properties
        if (!sessionFactory.getStatistics().isStatisticsEnabled()) {
            sessionFactory.getStatistics().setStatisticsEnabled(true);
        }
    }

    /**
     * Зберігає поточну кількість запитів перед початком операції.
     */
    public void captureInitialStats() {
        Statistics stats = sessionFactory.getStatistics();
        initialQueryCount.set(stats.getPrepareStatementCount());
    }

    /**
     * Порівнює кінцеву кількість запитів з початковою і виводить попередження.
     */
    public void checkAndAlert(String path) {
        Statistics stats = sessionFactory.getStatistics();
        long currentQueryCount = stats.getPrepareStatementCount();
        long queriesExecuted = currentQueryCount - initialQueryCount.get();

        if (queriesExecuted > 5) {
            log.error("\n\n\n!!! ALERT N+1 (path: {}): Виконано {} запитів! (> 5) !!!\n\n\n", path, queriesExecuted);
        } else {
            log.info("Hibernate Stats (path: {}): {} запитів. OK", path, queriesExecuted);
        }
    }
}
