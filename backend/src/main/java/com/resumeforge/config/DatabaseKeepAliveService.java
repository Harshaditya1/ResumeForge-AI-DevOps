package com.resumeforge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DatabaseKeepAliveService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseKeepAliveService.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseKeepAliveService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Ping serverless PostgreSQL every 60 seconds to keep compute warm and prevent connection timeouts
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void keepAlive() {
        try {
            jdbcTemplate.execute("SELECT 1");
            log.debug("Database keep-alive ping successful.");
        } catch (Exception ex) {
            log.warn("Database keep-alive ping warning: {}", ex.getMessage());
        }
    }
}

