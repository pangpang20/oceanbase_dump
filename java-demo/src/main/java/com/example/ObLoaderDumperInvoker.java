package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * OceanBase ob-loader-dumper 工具调用示例
 *
 * 功能：
 * - 调用 obdumper 导出数据
 * - 调用 obloader 导入数据
 * - 调用 obdumper 导出表结构（DDL）
 * - 调用 obloader 导入表结构（DDL）
 * - 使用 JDBC 执行 SQL（创建表、插入数据、查询验证）
 *
 * @author Claude
 */
public class ObLoaderDumperInvoker {

    private static final Logger logger = LoggerFactory.getLogger(ObLoaderDumperInvoker.class);

    // ==================== 配置区域 ====================

    // ob-loader-dumper 工具路径
    public static final String TOOL_HOME = "/opt/oceanbase/ob-loader-dumper-4.2.5-RELEASE";

    // 源数据库配置（IP1）
    public static final String SOURCE_HOST = "172.16.1.96";
    public static final int SOURCE_PORT = 52881;
    public static final String SOURCE_USER = "audaque";
    public static final String SOURCE_TENANT = "test";
    public static final String SOURCE_PASSWORD = "Audaque@123";
    public static final String SOURCE_DATABASE = "test";

    // 目标数据库配置（IP2）
    public static final String TARGET_HOST = "172.16.1.223";
    public static final int TARGET_PORT = 2881;
    public static final String TARGET_USER = "audaque";
    public static final String TARGET_TENANT = "sys";
    public static final String TARGET_PASSWORD = "Audaque@123";
    public static final String TARGET_DATABASE = "test";

    // 数据导出目录
    public static final String DATA_OUTPUT_DIR = "/opt/oceanbase/data/output";

    // 表名
    public static final String TABLE_NAME = "test01";

    // ==================== 工具脚本路径 ====================
    private static final String OBDUMPER_SCRIPT = TOOL_HOME + "/bin/obdumper";
    private static final String OBLOADER_SCRIPT = TOOL_HOME + "/bin/obloader";

    public static void main(String[] args) {
        logger.info("========================================");
        logger.info("  OceanBase 数据导出导入工具");
        logger.info("========================================");

        ObLoaderDumperInvoker invoker = new ObLoaderDumperInvoker();

        try {
            // 步骤 1: 检查 ob-loader-dumper 工具是否可用
            logger.info("步骤 1: 检查 ob-loader-dumper 工具...");
            if (!invoker.checkToolAvailable()) {
                logger.error("ob-loader-dumper 工具不可用，请检查路径：{}", TOOL_HOME);
                System.exit(1);
            }
            logger.info("ob-loader-dumper 工具检查通过");

            // 步骤 2: 在源数据库创建表并插入数据
            logger.info("=========================================");
            logger.info("步骤 2: 在源数据库创建表并插入数据...");
            logger.info("=========================================");
            invoker.createTableAndInsertData(
                    SOURCE_HOST, SOURCE_PORT,
                    SOURCE_USER, SOURCE_TENANT, SOURCE_PASSWORD,
                    SOURCE_DATABASE, TABLE_NAME);

            // 步骤 3: 使用 obdumper 导出数据
            logger.info("=========================================");
            logger.info("步骤 3: 使用 obdumper 导出数据...");
            logger.info("=========================================");
            boolean exportSuccess = invoker.exportData(
                    SOURCE_HOST, SOURCE_PORT,
                    SOURCE_USER, SOURCE_TENANT, SOURCE_PASSWORD,
                    SOURCE_DATABASE, TABLE_NAME,
                    DATA_OUTPUT_DIR, "csv");

            if (!exportSuccess) {
                logger.error("导出数据失败");
                System.exit(1);
            }

            // 步骤 4: 在目标数据库创建表
            logger.info("=========================================");
            logger.info("步骤 4: 在目标数据库创建表...");
            logger.info("=========================================");
            invoker.createTable(
                    TARGET_HOST, TARGET_PORT,
                    TARGET_USER, TARGET_TENANT, TARGET_PASSWORD,
                    TARGET_DATABASE, TABLE_NAME);

            // 步骤 5: 使用 obloader 导入数据
            logger.info("=========================================");
            logger.info("步骤 5: 使用 obloader 导入数据...");
            logger.info("=========================================");
            boolean importSuccess = invoker.importData(
                    TARGET_HOST, TARGET_PORT,
                    TARGET_USER, TARGET_TENANT, TARGET_PASSWORD,
                    TARGET_DATABASE, TABLE_NAME,
                    DATA_OUTPUT_DIR, "csv");

            if (!importSuccess) {
                logger.error("导入数据失败");
                System.exit(1);
            }

            // 步骤 6: 验证数据
            logger.info("=========================================");
            logger.info("步骤 6: 验证目标数据库数据...");
            logger.info("=========================================");
            invoker.verifyData(
                    TARGET_HOST, TARGET_PORT,
                    TARGET_USER, TARGET_TENANT, TARGET_PASSWORD,
                    TARGET_DATABASE, TABLE_NAME);

            logger.info("========================================");
            logger.info("  所有步骤完成！");
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("执行失败", e);
            System.exit(1);
        }
    }

    // ==================== ob-loader-dumper 工具调用方法 ====================

    /**
     * 使用 obdumper 导出数据
     */
    public boolean exportData(String host, int port, String user, String tenant,
                              String password, String database, String table,
                              String outputDir, String format) {
        logger.info("开始使用 obdumper 导出数据...");
        logger.info("主机：{}:{}, 数据库：{}, 表：{}, 输出目录：{}", host, port, database, table, outputDir);

        List<String> command = buildBaseCommand(OBDUMPER_SCRIPT, host, port, user, tenant, password, database);
        command.add("--table");
        command.add(table);
        command.add("-f");
        command.add(outputDir);
        command.add("--no-sys");

        // 添加格式选项
        if ("csv".equalsIgnoreCase(format)) {
            command.add("--csv");
        } else if ("sql".equalsIgnoreCase(format)) {
            command.add("--sql");
        } else if ("cut".equalsIgnoreCase(format)) {
            command.add("--cut");
        }

        command.add("--skip-check-dir");

        return executeCommand(command, "导出");
    }

    /**
     * 使用 obloader 导入数据
     */
    public boolean importData(String host, int port, String user, String tenant,
                              String password, String database, String table,
                              String inputDir, String format) {
        logger.info("开始使用 obloader 导入数据...");
        logger.info("主机：{}:{}, 数据库：{}, 表：{}, 输入目录：{}", host, port, database, table, inputDir);

        List<String> command = buildBaseCommand(OBLOADER_SCRIPT, host, port, user, tenant, password, database);
        command.add("--table");
        command.add(table);
        command.add("-f");
        command.add(inputDir);
        command.add("--no-sys");

        // 添加格式选项
        if ("csv".equalsIgnoreCase(format)) {
            command.add("--csv");
        } else if ("sql".equalsIgnoreCase(format)) {
            command.add("--sql");
        } else if ("cut".equalsIgnoreCase(format)) {
            command.add("--cut");
        }

        return executeCommand(command, "导入");
    }

    /**
     * 使用 obdumper 导出表结构（DDL）
     */
    public boolean exportSchema(String host, int port, String user, String tenant,
                                String password, String database, String outputDir) {
        logger.info("开始使用 obdumper 导出表结构...");

        List<String> command = buildBaseCommand(OBDUMPER_SCRIPT, host, port, user, tenant, password, database);
        command.add("-f");
        command.add(outputDir);
        command.add("--ddl");
        command.add("--no-sys");
        command.add("--skip-check-dir");

        return executeCommand(command, "导出表结构");
    }

    /**
     * 使用 obloader 导入表结构（DDL）
     */
    public boolean importSchema(String host, int port, String user, String tenant,
                                String password, String database, String inputDir) {
        logger.info("开始使用 obloader 导入表结构...");

        List<String> command = buildBaseCommand(OBLOADER_SCRIPT, host, port, user, tenant, password, database);
        command.add("-f");
        command.add(inputDir);
        command.add("--ddl");
        command.add("--no-sys");

        return executeCommand(command, "导入表结构");
    }

    /**
     * 构建基础命令
     */
    private List<String> buildBaseCommand(String script, String host, int port,
                                          String user, String tenant, String password, String database) {
        List<String> command = new ArrayList<>();
        command.add(script);
        command.add("-h");
        command.add(host);
        command.add("-P");
        command.add(String.valueOf(port));
        command.add("-u");
        command.add(user + "@" + tenant);  // OceanBase 用户名格式：user@tenant
        command.add("-t");
        command.add(tenant);
        command.add("-p");
        command.add(password);
        command.add("-D");
        command.add(database);
        return command;
    }

    /**
     * 执行命令
     */
    private boolean executeCommand(List<String> command, String operation) {
        try {
            logger.info("执行命令：{}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 读取输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[ob-loader-dumper] {}", line);
                }
            }

            // 等待完成
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("{} 操作成功完成", operation);
                return true;
            } else {
                logger.error("{} 操作失败，退出码：{}", operation, exitCode);
                return false;
            }

        } catch (IOException e) {
            logger.error("{} 操作执行命令失败：{}", operation, e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            logger.error("{} 操作被中断：{}", operation, e.getMessage(), e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 检查 ob-loader-dumper 工具是否可用
     */
    public boolean checkToolAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(OBDUMPER_SCRIPT, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            logger.error("检查 ob-loader-dumper 工具失败：{}", e.getMessage());
            return false;
        }
    }

    // ==================== JDBC 数据库操作方法 ====================

    /**
     * 获取数据库连接
     */
    private Connection getConnection(String host, int port, String user, String tenant,
                                     String password, String database) throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&useUnicode=true&" +
                "characterEncoding=utf8&allowMultiQueries=true&" +
                "rewriteBatchedStatements=true&useServerPrepStmts=false",
                host, port, database);

        Properties props = new Properties();
        props.setProperty("user", user + "@" + tenant);
        props.setProperty("password", password);
        props.setProperty("useSSL", "false");
        props.setProperty("characterEncoding", "utf8");

        return DriverManager.getConnection(url, props);
    }

    /**
     * 创建表并插入测试数据
     */
    public void createTableAndInsertData(String host, int port, String user, String tenant,
                                         String password, String database, String table) throws SQLException {
        logger.info("连接到数据库：{}:{}, 数据库：{}", host, port, database);

        try (Connection conn = getConnection(host, port, user, tenant, password, database)) {
            // 创建表
            String createTableSQL = "DROP TABLE IF EXISTS " + table + "; " +
                    "CREATE TABLE " + table + " (" +
                    "id BIGINT PRIMARY KEY COMMENT '主键 ID'," +
                    "name VARCHAR(100) COMMENT '姓名'," +
                    "code VARCHAR(50) COMMENT '编码'," +
                    "age INT COMMENT '年龄'," +
                    "salary DECIMAL(10,2) COMMENT '薪资'," +
                    "balance DOUBLE COMMENT '余额'," +
                    "status TINYINT COMMENT '状态'," +
                    "create_time DATETIME COMMENT '创建时间'," +
                    "update_time TIMESTAMP COMMENT '更新时间'," +
                    "birth_date DATE COMMENT '出生日期'" +
                    ") COMMENT '测试表 " + table + "'";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                logger.info("表 {} 创建成功", table);
            }

            // 插入测试数据
            String insertSQL = "INSERT INTO " + table + " (id, name, code, age, salary, balance, status, create_time, update_time, birth_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                Object[][] testData = generateTestData();
                for (Object[] data : testData) {
                    pstmt.setLong(1, (Long) data[0]);
                    pstmt.setString(2, (String) data[1]);
                    pstmt.setString(3, (String) data[2]);
                    pstmt.setInt(4, (Integer) data[3]);
                    pstmt.setBigDecimal(5, (java.math.BigDecimal) data[4]);
                    pstmt.setDouble(6, (Double) data[5]);
                    pstmt.setInt(7, (Integer) data[6]);
                    pstmt.setTimestamp(8, (Timestamp) data[7]);
                    pstmt.setTimestamp(9, (Timestamp) data[8]);
                    pstmt.setDate(10, (Date) data[9]);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                logger.info("插入 {} 条测试数据成功", testData.length);
            }
        }
    }

    /**
     * 仅创建表
     */
    public void createTable(String host, int port, String user, String tenant,
                            String password, String database, String table) throws SQLException {
        logger.info("连接到数据库：{}:{}, 数据库：{}", host, port, database);

        try (Connection conn = getConnection(host, port, user, tenant, password, database)) {
            String createTableSQL = "DROP TABLE IF EXISTS " + table + "; " +
                    "CREATE TABLE " + table + " (" +
                    "id BIGINT PRIMARY KEY COMMENT '主键 ID'," +
                    "name VARCHAR(100) COMMENT '姓名'," +
                    "code VARCHAR(50) COMMENT '编码'," +
                    "age INT COMMENT '年龄'," +
                    "salary DECIMAL(10,2) COMMENT '薪资'," +
                    "balance DOUBLE COMMENT '余额'," +
                    "status TINYINT COMMENT '状态'," +
                    "create_time DATETIME COMMENT '创建时间'," +
                    "update_time TIMESTAMP COMMENT '更新时间'," +
                    "birth_date DATE COMMENT '出生日期'" +
                    ") COMMENT '测试表 " + table + "'";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                logger.info("表 {} 创建成功", table);
            }
        }
    }

    /**
     * 验证数据
     */
    public void verifyData(String host, int port, String user, String tenant,
                           String password, String database, String table) throws SQLException {
        try (Connection conn = getConnection(host, port, user, tenant, password, database);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + table + " ORDER BY id")) {

            logger.info("目标数据库数据：");
            logger.info("------------------------------------------------------------------------------------------");
            logger.info(String.format("%-5s %-10s %-10s %-5s %-10s %-10s %-8s %-20s %-20s %-12s",
                    "ID", "Name", "Code", "Age", "Salary", "Balance", "Status", "CreateTime", "UpdateTime", "BirthDate"));
            logger.info("------------------------------------------------------------------------------------------");

            int count = 0;
            while (rs.next()) {
                logger.info(String.format("%-5d %-10s %-10s %-5d %-10.2f %-10.2f %-8d %-20s %-20s %-12s",
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getInt("age"),
                        rs.getBigDecimal("salary").doubleValue(),
                        rs.getDouble("balance"),
                        rs.getInt("status"),
                        formatTimestamp(rs.getTimestamp("create_time")),
                        formatTimestamp(rs.getTimestamp("update_time")),
                        formatDate(rs.getDate("birth_date"))));
                count++;
            }
            logger.info("------------------------------------------------------------------------------------------");
            logger.info("共 {} 条记录", count);
        }
    }

    private String formatTimestamp(Timestamp ts) {
        return ts != null ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts) : "null";
    }

    private String formatDate(Date d) {
        return d != null ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(d) : "null";
    }

    /**
     * 生成测试数据
     */
    private Object[][] generateTestData() {
        return new Object[][] {
            {1L, "张三", "CODE001", 25, new java.math.BigDecimal("8000.00"), 15000.50, 1,
                    Timestamp.valueOf("2024-01-15 10:30:00"), Timestamp.valueOf("2024-01-15 10:30:00"), Date.valueOf("1999-05-20")},
            {2L, "李四", "CODE002", 30, new java.math.BigDecimal("12000.00"), 28000.75, 1,
                    Timestamp.valueOf("2024-02-20 14:45:00"), Timestamp.valueOf("2024-02-20 14:45:00"), Date.valueOf("1994-08-15")},
            {3L, "王五", "CODE003", 28, new java.math.BigDecimal("9500.00"), 12000.00, 2,
                    Timestamp.valueOf("2024-03-10 09:15:00"), Timestamp.valueOf("2024-03-10 09:15:00"), Date.valueOf("1996-03-10")},
            {4L, "赵六", "CODE004", 35, new java.math.BigDecimal("15000.00"), 45000.25, 1,
                    Timestamp.valueOf("2024-04-05 16:20:00"), Timestamp.valueOf("2024-04-05 16:20:00"), Date.valueOf("1989-11-25")},
            {5L, "孙七", "CODE005", 22, new java.math.BigDecimal("6000.00"), 8000.50, 3,
                    Timestamp.valueOf("2024-05-12 11:00:00"), Timestamp.valueOf("2024-05-12 11:00:00"), Date.valueOf("2002-07-08")},
            {6L, "周八", "CODE006", 40, new java.math.BigDecimal("20000.00"), 68000.00, 1,
                    Timestamp.valueOf("2024-06-18 08:30:00"), Timestamp.valueOf("2024-06-18 08:30:00"), Date.valueOf("1984-02-14")},
            {7L, "吴九", "CODE007", 27, new java.math.BigDecimal("8800.00"), 19500.80, 2,
                    Timestamp.valueOf("2024-07-22 15:45:00"), Timestamp.valueOf("2024-07-22 15:45:00"), Date.valueOf("1997-09-30")},
            {8L, "郑十", "CODE008", 33, new java.math.BigDecimal("14000.00"), 32000.60, 1,
                    Timestamp.valueOf("2024-08-08 13:10:00"), Timestamp.valueOf("2024-08-08 13:10:00"), Date.valueOf("1991-04-18")},
            {9L, "刘一", "CODE009", 29, new java.math.BigDecimal("10500.00"), 22000.40, 3,
                    Timestamp.valueOf("2024-09-14 10:25:00"), Timestamp.valueOf("2024-09-14 10:25:00"), Date.valueOf("1995-12-05")},
            {10L, "陈二", "CODE010", 31, new java.math.BigDecimal("13000.00"), 35000.90, 1,
                    Timestamp.valueOf("2024-10-01 17:00:00"), Timestamp.valueOf("2024-10-01 17:00:00"), Date.valueOf("1993-06-22")}
        };
    }
}
