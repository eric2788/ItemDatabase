package com.ericlam.mysql;

import com.ericlam.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLManager {
    private static DataSource source;
    private static MySQLManager manager;

    public static MySQLManager getInstance() {
        if (manager == null) manager = new MySQLManager();
        return manager;
    }

    private MySQLManager(){
        //Load the fucking yml
        Config cf = Config.getInstance();
        FileConfiguration sqlConfig = cf.getConfig();

        //Create dat fucking config
        HikariConfig config = new HikariConfig();
        String host = sqlConfig.getString("MySQL.host");
        String port = sqlConfig.getString("MySQL.port");
        String database = sqlConfig.getString("MySQL.database");
        String username = sqlConfig.getString("MySQL.username");
        String password = sqlConfig.getString("MySQL.password");
        String poolname = sqlConfig.getString("MySQL.Pool.name");
        int minsize = sqlConfig.getInt("MySQL.Pool.min-size");
        int maxsize = sqlConfig.getInt("MySQL.Pool.max-size");
        boolean SSL = sqlConfig.getBoolean("MySQL.use-SSL");
        String jdbc = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + "useSSL=" + SSL;
        config.setJdbcUrl(jdbc);
        config.setPoolName(poolname);
        config.setMaximumPoolSize(maxsize);
        config.setMinimumIdle(minsize);
        config.setUsername(username);
        config.setPassword(password);
        /*config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setAutoCommit(false);*/
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        //config.addDataSourceProperty("useUnicode",true);
        config.addDataSourceProperty("characterEncoding","utf8");

        //Create the fucking datasource
        source = new HikariDataSource(config);
    }

    public Connection getConneciton() throws SQLException {
        return source.getConnection();
    }

}
