package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author sashank.adhikari on 5/7/2025
 */

public class DatabaseConfig {
 private static DataSource dataSource;

 static{
     HikariConfig config = new HikariConfig();
     config.setJdbcUrl("jdbc:postgresql://localhost:5432/keycloakdb");
     config.setUsername("your_db_user");
     config.setPassword("your_db_password");
     config.setDriverClassName("org.postgresql.Driver");

     // Optional settings
     config.setMaximumPoolSize(10);
     config.setMinimumIdle(2);

     dataSource = new HikariDataSource(config);
 }
    public static DataSource getDataSource() {
        return dataSource;
    }

}
