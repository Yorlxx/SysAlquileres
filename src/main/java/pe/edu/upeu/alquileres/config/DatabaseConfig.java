package pe.edu.upeu.alquileres.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Configuración de la base de datos con pool HikariCP.
 * Por defecto usa H2 embebida en modo PostgreSQL; también acepta PostgreSQL.
 */
public final class DatabaseConfig {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final String PROPERTIES_FILE = "application.properties";
    private static final String DEFAULT_URL =
            "jdbc:h2:file:./data/alquileres;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_SCRIPT = "schema.sql";

    private static volatile HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    public static synchronized void init() {
        if (dataSource != null) {
            return;
        }
        Properties props = loadProperties();
        String url = props.getProperty("db.url", DEFAULT_URL);

        HikariDataSource ds = createDataSource(url, props);
        try {
            if (Boolean.parseBoolean(props.getProperty("db.ddl.auto", "true"))) {
                runScript(ds, props.getProperty("db.ddl.script", DEFAULT_SCRIPT));
            }
            if (Boolean.parseBoolean(props.getProperty("db.seed.demo", "true"))) {
                seedIfEmpty(ds);
            }
        } catch (RuntimeException e) {
            ds.close();
            throw e;
        }
        dataSource = ds;
        LOG.info("Base de datos lista: {}", url);
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            init();
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    private static HikariDataSource createDataSource(String url, Properties props) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("SysAlquileresPool");
        config.setDriverClassName(url.startsWith("jdbc:postgresql") ? "org.postgresql.Driver" : "org.h2.Driver");
        config.setJdbcUrl(url);
        config.setUsername(props.getProperty("db.username", DEFAULT_USER));
        config.setPassword(props.getProperty("db.password", ""));
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10_000);
        try {
            return new HikariDataSource(config);
        } catch (RuntimeException e) {
            throw new IllegalStateException("No fue posible abrir la base de datos: " + url, e);
        }
    }

    private static Properties loadProperties() {
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/" + PROPERTIES_FILE)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + PROPERTIES_FILE);
            }
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo " + PROPERTIES_FILE, e);
        }
    }

    /** Ejecuta un script SQL sentencia por sentencia. */
    private static void runScript(DataSource ds, String script) {
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/" + script)) {
            if (in == null) {
                LOG.warn("No se encontró el script SQL {}", script);
                return;
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .filter(line -> !line.strip().startsWith("--"))
                    .collect(Collectors.joining("\n"));

            try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
                for (String statement : sql.split(";")) {
                    if (!statement.isBlank()) {
                        stmt.execute(statement);
                    }
                }
            }
            LOG.info("Script ejecutado: {}", script);
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Error ejecutando el script " + script, e);
        }
    }

    private static void seedIfEmpty(DataSource ds) {
        String countSql = "SELECT COUNT(*) FROM inquilino";
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getLong(1) == 0) {
                runScript(ds, "seed.sql");
                LOG.info("Datos de ejemplo cargados");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error comprobando los datos de ejemplo", e);
        }
    }
}
