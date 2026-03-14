package com.example;

import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180ParserBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OceanBase 数据库操作示例
 * 功能：创建表、插入数据、导出数据、导入数据
 *
 * 支持两种方式：
 * 1. 使用 JDBC 直接操作数据库（本类实现）
 * 2. 调用 ob-loader-dumper 工具（见 ObLoaderDumperInvoker 类）
 */
public class OceanBaseDemo {

    private static final Logger logger = LoggerFactory.getLogger(OceanBaseDemo.class);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        // 检查参数，决定使用哪种方式
        boolean useTool = args.length > 0 && "--use-tool".equals(args[0]);

        if (useTool) {
            // 使用 ob-loader-dumper 工具方式
            runWithObLoaderDumper();
        } else {
            // 使用 JDBC 直接操作方式
            runWithJdbc();
        }
    }

    /**
     * 使用 JDBC 方式执行
     */
    private static void runWithJdbc() {
        OceanBaseDemo demo = new OceanBaseDemo();
        try {
            // 1. 在源数据库创建表并插入数据
            logger.info("========== 步骤 1: 在源数据库创建表并插入数据 ==========");
            demo.createTableAndInsertData(DbConfig.SOURCE_HOST, DbConfig.SOURCE_PORT,
                    DbConfig.buildUserWithTenant(DbConfig.SOURCE_USER, DbConfig.SOURCE_TENANT),
                    DbConfig.SOURCE_PASSWORD, DbConfig.SOURCE_DATABASE);

            // 2. 从源数据库导出数据到 CSV
            logger.info("========== 步骤 2: 从源数据库导出数据到 CSV ==========");
            demo.exportDataToCsv(DbConfig.SOURCE_HOST, DbConfig.SOURCE_PORT,
                    DbConfig.buildUserWithTenant(DbConfig.SOURCE_USER, DbConfig.SOURCE_TENANT),
                    DbConfig.SOURCE_PASSWORD, DbConfig.SOURCE_DATABASE,
                    DbConfig.DATA_OUTPUT_DIR);

            // 3. 在目标数据库创建表
            logger.info("========== 步骤 3: 在目标数据库创建表 ==========");
            demo.createTable(DbConfig.TARGET_HOST, DbConfig.TARGET_PORT,
                    DbConfig.buildUserWithTenant(DbConfig.TARGET_USER, DbConfig.TARGET_TENANT),
                    DbConfig.TARGET_PASSWORD, DbConfig.TARGET_DATABASE);

            // 4. 从 CSV 导入数据到目标数据库
            logger.info("========== 步骤 4: 从 CSV 导入数据到目标数据库 ==========");
            demo.importDataFromCsv(DbConfig.TARGET_HOST, DbConfig.TARGET_PORT,
                    DbConfig.buildUserWithTenant(DbConfig.TARGET_USER, DbConfig.TARGET_TENANT),
                    DbConfig.TARGET_PASSWORD, DbConfig.TARGET_DATABASE,
                    DbConfig.DATA_OUTPUT_DIR);

            // 5. 验证数据
            logger.info("========== 步骤 5: 验证目标数据库数据 ==========");
            demo.verifyData(DbConfig.TARGET_HOST, DbConfig.TARGET_PORT,
                    DbConfig.buildUserWithTenant(DbConfig.TARGET_USER, DbConfig.TARGET_TENANT),
                    DbConfig.TARGET_PASSWORD, DbConfig.TARGET_DATABASE);

            logger.info("========== 所有步骤完成（JDBC 方式）==========");
        } catch (Exception e) {
            logger.error("执行失败", e);
            System.exit(1);
        }
    }

    /**
     * 使用 ob-loader-dumper 工具方式执行
     */
    private static void runWithObLoaderDumper() {
        logger.info("========== 使用 ob-loader-dumper 工具方式 ==========");

        ObLoaderDumperInvoker invoker = new ObLoaderDumperInvoker();

        // 检查工具是否可用
        if (!invoker.checkToolAvailable()) {
            logger.error("ob-loader-dumper 工具不可用，请检查路径：{}", ObLoaderDumperInvoker.TOOL_HOME);
            System.exit(1);
        }
        logger.info("ob-loader-dumper 工具检查通过");

        OceanBaseDemo demo = new OceanBaseDemo();
        try {
            // 1. 在源数据库创建表并插入数据（使用 JDBC）
            logger.info("========== 步骤 1: 在源数据库创建表并插入数据 ==========");
            demo.createTableAndInsertData(DbConfig.SOURCE_HOST, DbConfig.SOURCE_PORT,
                    DbConfig.buildUserWithTenant(DbConfig.SOURCE_USER, DbConfig.SOURCE_TENANT),
                    DbConfig.SOURCE_PASSWORD, DbConfig.SOURCE_DATABASE);

            // 2. 使用 obdumper 导出数据
            logger.info("========== 步骤 2: 使用 obdumper 导出数据 ==========");
            boolean exportSuccess = invoker.exportData(
                    DbConfig.SOURCE_HOST, DbConfig.SOURCE_PORT,
                    DbConfig.SOURCE_USER, DbConfig.SOURCE_TENANT,
                    DbConfig.SOURCE_PASSWORD, DbConfig.SOURCE_DATABASE,
                    "test01", DbConfig.DATA_OUTPUT_DIR, "csv");

            if (!exportSuccess) {
                logger.error("导出数据失败");
                System.exit(1);
            }

            // 3. 在目标数据库创建表（使用 JDBC）
            logger.info("========== 步骤 3: 在目标数据库创建表 ==========");
            demo.createTable(DbConfig.TARGET_HOST, DbConfig.TARGET_PORT,
                    DbConfig.buildUserWithTenant(DbConfig.TARGET_USER, DbConfig.TARGET_TENANT),
                    DbConfig.TARGET_PASSWORD, DbConfig.TARGET_DATABASE);

            // 4. 使用 obloader 导入数据
            logger.info("========== 步骤 4: 使用 obloader 导入数据 ==========");
            boolean importSuccess = invoker.importData(
                    DbConfig.TARGET_HOST, DbConfig.TARGET_PORT,
                    DbConfig.TARGET_USER, DbConfig.TARGET_TENANT,
                    DbConfig.TARGET_PASSWORD, DbConfig.TARGET_DATABASE,
                    "test01", DbConfig.DATA_OUTPUT_DIR, "csv");

            if (!importSuccess) {
                logger.error("导入数据失败");
                System.exit(1);
            }

            // 5. 验证数据
            logger.info("========== 步骤 5: 验证目标数据库数据 ==========");
            demo.verifyData(DbConfig.TARGET_HOST, DbConfig.TARGET_PORT,
                    DbConfig.buildUserWithTenant(DbConfig.TARGET_USER, DbConfig.TARGET_TENANT),
                    DbConfig.TARGET_PASSWORD, DbConfig.TARGET_DATABASE);

            logger.info("========== 所有步骤完成（ob-loader-dumper 工具方式）==========");
        } catch (Exception e) {
            logger.error("执行失败", e);
            System.exit(1);
        }
    }

    /**
     * 创建表并插入测试数据
     */
    public void createTableAndInsertData(String host, int port, String user, String password, String database)
            throws SQLException {
        String url = DbConfig.buildJdbcUrl(host, port, database);
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("useSSL", "false");
        props.setProperty("characterEncoding", "utf8");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            logger.info("连接到数据库：{}:{}", host, port);

            // 创建表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS test01 (" +
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
                    ") COMMENT '测试表 test01'";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                logger.info("表 test01 创建成功");
            }

            // 插入测试数据
            String insertSQL = "INSERT INTO test01 (id, name, code, age, salary, balance, status, create_time, update_time, birth_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                List<TestData> testDataList = generateTestData();
                for (TestData data : testDataList) {
                    pstmt.setLong(1, data.getId());
                    pstmt.setString(2, data.getName());
                    pstmt.setString(3, data.getCode());
                    pstmt.setInt(4, data.getAge());
                    pstmt.setBigDecimal(5, data.getSalary());
                    pstmt.setDouble(6, data.getBalance());
                    pstmt.setInt(7, data.getStatus());
                    pstmt.setTimestamp(8, data.getCreateTime() != null ? Timestamp.valueOf(data.getCreateTime()) : null);
                    pstmt.setTimestamp(9, data.getUpdateTime() != null ? Timestamp.valueOf(data.getUpdateTime()) : null);
                    pstmt.setDate(10, data.getBirthDate() != null ? java.sql.Date.valueOf(data.getBirthDate()) : null);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                logger.info("插入 {} 条测试数据成功", testDataList.size());
            }
        }
    }

    /**
     * 仅创建表（用于目标数据库）
     */
    public void createTable(String host, int port, String user, String password, String database)
            throws SQLException {
        String url = DbConfig.buildJdbcUrl(host, port, database);
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("useSSL", "false");
        props.setProperty("characterEncoding", "utf8");

        try (Connection conn = DriverManager.getConnection(url, props)) {
            logger.info("连接到数据库：{}:{}", host, port);

            String createTableSQL = "CREATE TABLE IF NOT EXISTS test01 (" +
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
                    ") COMMENT '测试表 test01'";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
                logger.info("表 test01 创建成功");
            }
        }
    }

    /**
     * 导出数据到 CSV 文件
     */
    public void exportDataToCsv(String host, int port, String user, String password, String database, String outputDir)
            throws SQLException, IOException {
        String url = DbConfig.buildJdbcUrl(host, port, database);
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("useSSL", "false");
        props.setProperty("characterEncoding", "utf8");

        String csvFilePath = outputDir + "/test01.csv";

        try (Connection conn = DriverManager.getConnection(url, props);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM test01");
             Writer writer = new FileWriter(csvFilePath);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            logger.info("导出数据到：{}", csvFilePath);

            // 写入表头
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] headers = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                headers[i] = metaData.getColumnName(i + 1);
            }
            csvWriter.writeNext(headers);

            // 写入数据
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    Object value = rs.getObject(i + 1);
                    if (value instanceof Timestamp) {
                        row[i] = ((Timestamp) value).toLocalDateTime().format(DATETIME_FORMATTER);
                    } else if (value instanceof Date) {
                        row[i] = ((Date) value).toLocalDate().format(DATE_FORMATTER);
                    } else {
                        row[i] = value != null ? value.toString() : "";
                    }
                }
                csvWriter.writeNext(row);
            }

            logger.info("导出完成，共写入 {} 行", rs.getRow());
        }
    }

    /**
     * 从 CSV 文件导入数据
     */
    public void importDataFromCsv(String host, int port, String user, String password, String database, String inputDir)
            throws SQLException, IOException {
        String url = DbConfig.buildJdbcUrl(host, port, database);
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("useSSL", "false");
        props.setProperty("characterEncoding", "utf8");
        props.setProperty("allowMultiQueries", "true");

        String csvFilePath = inputDir + "/test01.csv";

        try (Connection conn = DriverManager.getConnection(url, props);
             Reader reader = new FileReader(csvFilePath);
             CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(new RFC4180ParserBuilder().build()).build()) {

            logger.info("从 CSV 文件导入数据：{}", csvFilePath);

            // 跳过表头
            String[] header = csvReader.readNext();
            if (header == null) {
                logger.error("CSV 文件为空");
                return;
            }

            // 使用 LOAD DATA 或者 PreparedStatement 插入
            String insertSQL = "INSERT INTO test01 (id, name, code, age, salary, balance, status, create_time, update_time, birth_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                String[] line;
                int count = 0;
                while ((line = csvReader.readNext()) != null) {
                    pstmt.setLong(1, Long.parseLong(line[0]));
                    pstmt.setString(2, line[1]);
                    pstmt.setString(3, line[2]);
                    pstmt.setInt(4, Integer.parseInt(line[3]));
                    pstmt.setBigDecimal(5, new BigDecimal(line[4]));
                    pstmt.setDouble(6, Double.parseDouble(line[5]));
                    pstmt.setInt(7, Integer.parseInt(line[6]));
                    pstmt.setTimestamp(8, Timestamp.valueOf(line[7]));
                    pstmt.setTimestamp(9, Timestamp.valueOf(line[8]));
                    pstmt.setDate(10, java.sql.Date.valueOf(line[9]));
                    pstmt.addBatch();
                    count++;

                    if (count % 100 == 0) {
                        pstmt.executeBatch();
                    }
                }
                pstmt.executeBatch();
                logger.info("导入完成，共导入 {} 条记录", count);
            }
        }
    }

    /**
     * 验证数据
     */
    public void verifyData(String host, int port, String user, String password, String database)
            throws SQLException {
        String url = DbConfig.buildJdbcUrl(host, port, database);
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("useSSL", "false");
        props.setProperty("characterEncoding", "utf8");

        try (Connection conn = DriverManager.getConnection(url, props);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM test01 ORDER BY id")) {

            logger.info("目标数据库数据验证:");
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
                        rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toLocalDateTime().format(DATETIME_FORMATTER) : "null",
                        rs.getTimestamp("update_time") != null ? rs.getTimestamp("update_time").toLocalDateTime().format(DATETIME_FORMATTER) : "null",
                        rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate().format(DATE_FORMATTER) : "null"));
                count++;
            }
            logger.info("------------------------------------------------------------------------------------------");
            logger.info("共 {} 条记录", count);
        }
    }

    /**
     * 生成测试数据
     */
    private List<TestData> generateTestData() {
        List<TestData> dataList = new ArrayList<>();
        dataList.add(new TestData(1L, "张三", "CODE001", 25, new BigDecimal("8000.00"), 15000.50, 1,
                LocalDateTime.of(2024, 1, 15, 10, 30, 0), LocalDateTime.of(2024, 1, 15, 10, 30, 0), LocalDate.of(1999, 5, 20)));
        dataList.add(new TestData(2L, "李四", "CODE002", 30, new BigDecimal("12000.00"), 28000.75, 1,
                LocalDateTime.of(2024, 2, 20, 14, 45, 0), LocalDateTime.of(2024, 2, 20, 14, 45, 0), LocalDate.of(1994, 8, 15)));
        dataList.add(new TestData(3L, "王五", "CODE003", 28, new BigDecimal("9500.00"), 12000.00, 2,
                LocalDateTime.of(2024, 3, 10, 9, 15, 0), LocalDateTime.of(2024, 3, 10, 9, 15, 0), LocalDate.of(1996, 3, 10)));
        dataList.add(new TestData(4L, "赵六", "CODE004", 35, new BigDecimal("15000.00"), 45000.25, 1,
                LocalDateTime.of(2024, 4, 5, 16, 20, 0), LocalDateTime.of(2024, 4, 5, 16, 20, 0), LocalDate.of(1989, 11, 25)));
        dataList.add(new TestData(5L, "孙七", "CODE005", 22, new BigDecimal("6000.00"), 8000.50, 3,
                LocalDateTime.of(2024, 5, 12, 11, 0, 0), LocalDateTime.of(2024, 5, 12, 11, 0, 0), LocalDate.of(2002, 7, 8)));
        dataList.add(new TestData(6L, "周八", "CODE006", 40, new BigDecimal("20000.00"), 68000.00, 1,
                LocalDateTime.of(2024, 6, 18, 8, 30, 0), LocalDateTime.of(2024, 6, 18, 8, 30, 0), LocalDate.of(1984, 2, 14)));
        dataList.add(new TestData(7L, "吴九", "CODE007", 27, new BigDecimal("8800.00"), 19500.80, 2,
                LocalDateTime.of(2024, 7, 22, 15, 45, 0), LocalDateTime.of(2024, 7, 22, 15, 45, 0), LocalDate.of(1997, 9, 30)));
        dataList.add(new TestData(8L, "郑十", "CODE008", 33, new BigDecimal("14000.00"), 32000.60, 1,
                LocalDateTime.of(2024, 8, 8, 13, 10, 0), LocalDateTime.of(2024, 8, 8, 13, 10, 0), LocalDate.of(1991, 4, 18)));
        dataList.add(new TestData(9L, "刘一", "CODE009", 29, new BigDecimal("10500.00"), 22000.40, 3,
                LocalDateTime.of(2024, 9, 14, 10, 25, 0), LocalDateTime.of(2024, 9, 14, 10, 25, 0), LocalDate.of(1995, 12, 5)));
        dataList.add(new TestData(10L, "陈二", "CODE010", 31, new BigDecimal("13000.00"), 35000.90, 1,
                LocalDateTime.of(2024, 10, 1, 17, 0, 0), LocalDateTime.of(2024, 10, 1, 17, 0, 0), LocalDate.of(1993, 6, 22)));
        return dataList;
    }
}
