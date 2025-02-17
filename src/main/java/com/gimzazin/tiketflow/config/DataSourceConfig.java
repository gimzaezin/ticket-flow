package com.gimzazin.tiketflow.config;


import io.github.cdimascio.dotenv.Dotenv;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {


    @Bean
    public DataSource dataSource() {

        Dotenv dotenv = Dotenv.load();

        String host = dotenv.get("DB_HOST", "localhost");
        String port = dotenv.get("DB_PORT", "3306");
        String dbName = dotenv.get("DB_NAME", "ticket_flow");
        String username = dotenv.get("DB_USER", "defaultUser");
        String password = dotenv.get("DB_PASSWORD", "defaultPassword");


        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?serverTimezone=UTC";

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setInitialSize(20);
        dataSource.setMinIdle(20);

        dataSource.setMaxTotal(200);
        dataSource.setMaxIdle(50);

        dataSource.setMaxWaitMillis(5000);

        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(false);
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(30000);
        dataSource.setMinEvictableIdleTimeMillis(60000);

        return dataSource;
    }
}
