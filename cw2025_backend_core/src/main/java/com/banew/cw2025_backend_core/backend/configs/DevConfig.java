package com.banew.cw2025_backend_core.backend.configs;

import com.banew.cw2025_backend_core.backend.utils.tstats.AlertInterceptor;
import com.banew.cw2025_backend_core.backend.utils.tstats.TransactionStatsLogger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("dev")
public class DevConfig implements WebMvcConfigurer {

    private final TransactionStatsLogger statsLogger;

    // Впроваджуємо наш слухач
    public DevConfig(TransactionStatsLogger statsLogger) {
        this.statsLogger = statsLogger;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Реєструємо наш інтерцептор
        registry.addInterceptor(new AlertInterceptor(statsLogger));
    }
}
