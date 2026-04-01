package com.tictactoe.multiplayer.config;

import org.springframework.context.annotation.Configuration;

/**
 * Database configuration is handled by Spring Boot auto-configuration
 * using environment variables set in application.properties:
 * - spring.datasource.url
 * - spring.datasource.username
 * - spring.datasource.password
 * 
 * For Render deployment, these are set via:
 * - DATABASE_URL (from pserv service)
 * - DATABASE_USER (optional, from environment)
 * - DATABASE_PASSWORD (optional, from environment)
 */
@Configuration
public class DatabaseConfig {
    // Spring Boot auto-configuration handles datasource creation from properties
}