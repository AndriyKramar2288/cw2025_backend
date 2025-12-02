package com.banew.cw2025_backend_core.backend.utils.tstats;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AlertInterceptor implements HandlerInterceptor {

    private final TransactionStatsLogger statsLogger;

    public AlertInterceptor(TransactionStatsLogger statsLogger) {
        this.statsLogger = statsLogger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Зафіксувати поточну к-ть запитів на початку запиту
        statsLogger.captureInitialStats();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Перевірити к-ть запитів після завершення обробки (включно з мапінгом DTO)
        String path = request.getRequestURI();
        statsLogger.checkAndAlert(path);
    }
}
