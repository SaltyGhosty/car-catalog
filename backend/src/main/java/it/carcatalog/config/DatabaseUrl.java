package it.carcatalog.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Render fornisce DATABASE_URL nel formato "postgresql://user:password@host[:port]/db".
 * JDBC invece vuole "jdbc:postgresql://host:port/db" con user e password separati:
 * questa classe fa la conversione. Accetta anche un URL già in formato jdbc:.
 */
@Configuration
public class DatabaseUrl {

    @Bean
    public DataSource dataSource(
            @Value("${DATABASE_URL:postgresql://carcatalog:carcatalog@localhost:5432/carcatalog}") String databaseUrl,
            @Value("${app.db.pool-size:5}") int poolSize) {
        Parsed p = parse(databaseUrl);
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(p.jdbcUrl());
        if (p.username() != null) ds.setUsername(p.username());
        if (p.password() != null) ds.setPassword(p.password());
        ds.setMaximumPoolSize(poolSize);
        ds.setPoolName("car-catalog-pool");
        return ds;
    }

    public record Parsed(String jdbcUrl, String username, String password) {
        /** Niente password nei log, neanche per sbaglio. */
        @Override
        public String toString() {
            return "Parsed[jdbcUrl=" + jdbcUrl + "]";
        }
    }

    public static Parsed parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("DATABASE_URL non impostata");
        }
        String url = raw.trim();
        if (url.startsWith("jdbc:")) {
            return new Parsed(url, null, null);
        }
        if (!url.startsWith("postgres://") && !url.startsWith("postgresql://")) {
            throw new IllegalStateException("DATABASE_URL deve iniziare con postgres://, postgresql:// o jdbc:");
        }
        URI uri = URI.create(url.replaceFirst("^postgres(ql)?://", "postgresql://"));

        String user = null;
        String password = null;
        if (uri.getRawUserInfo() != null) {
            String[] parts = uri.getRawUserInfo().split(":", 2);
            user = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            if (parts.length > 1) password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
        }
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String jdbc = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getRawPath()
                + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        return new Parsed(jdbc, user, password);
    }
}
