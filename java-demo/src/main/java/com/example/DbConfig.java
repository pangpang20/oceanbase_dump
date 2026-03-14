package com.example;

/**
 * 数据库连接配置
 */
public class DbConfig {
    // IP1 数据库配置（源数据库）
    public static final String SOURCE_HOST = "172.16.1.96";
    public static final int SOURCE_PORT = 52881;
    public static final String SOURCE_USER = "audaque";
    public static final String SOURCE_TENANT = "test";
    public static final String SOURCE_PASSWORD = "Audaque@123";
    public static final String SOURCE_DATABASE = "test";

    // IP2 数据库配置（目标数据库）
    public static final String TARGET_HOST = "172.16.1.223";
    public static final int TARGET_PORT = 2881;
    public static final String TARGET_USER = "audaque";
    public static final String TARGET_TENANT = "test";
    public static final String TARGET_PASSWORD = "Audaque@123";
    public static final String TARGET_DATABASE = "test";

    // 数据导出目录
    public static final String DATA_OUTPUT_DIR = "/opt/oceanbase/data/output";

    /**
     * 构建 OceanBase JDBC 连接 URL
     */
    public static String buildJdbcUrl(String host, int port, String database) {
        // OceanBase 使用 MySQL 协议，用户名格式为 user@tenant
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&useUnicode=true&" +
                "characterEncoding=utf8&allowMultiQueries=true&" +
                "rewriteBatchedStatements=true&useServerPrepStmts=false",
                host, port, database);
    }

    /**
     * 构建带租户的用户名
     */
    public static String buildUserWithTenant(String user, String tenant) {
        return user + "@" + tenant;
    }
}
