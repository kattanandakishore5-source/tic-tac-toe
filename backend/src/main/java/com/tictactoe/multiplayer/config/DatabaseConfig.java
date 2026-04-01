package com.tictactoe.multiplayer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            // Fallback to local development
            return DataSourceBuilder.create()
                    .url("jdbc:postgresql://localhost:5432/tictactoe")
                    .username("postgres")
                    .password("Nanda@110605")
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        URI dbUri = new URI(databaseUrl);
        String[] userInfo = dbUri.getUserInfo().split(":");
        String username = userInfo[0];
        String password = userInfo[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}