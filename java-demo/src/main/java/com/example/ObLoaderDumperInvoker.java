package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 调用 OceanBase ob-loader-dumper 工具
 * 包括 obdumper（导出）和 obloader（导入）
 */
public class ObLoaderDumperInvoker {

    private static final Logger logger = LoggerFactory.getLogger(ObLoaderDumperInvoker.class);

    // ob-loader-dumper 工具路径
    private static final String TOOL_HOME = "/opt/oceanbase/ob-loader-dumper-4.2.5-RELEASE";
    private static final String OBDUMPER_SCRIPT = TOOL_HOME + "/bin/obdumper";
    private static final String OBLOADER_SCRIPT = TOOL_HOME + "/bin/obloader";

    /**
     * 使用 obdumper 导出数据
     *
     * @param host         主机 IP
     * @param port         端口
     * @param user         用户名（不含租户）
     * @param tenant       租户名
     * @param password     密码
     * @param database     数据库名
     * @param table        表名
     * @param outputDir    输出目录
     * @param format       导出格式：csv, sql, cut
     * @return 是否成功
     */
    public boolean exportData(String host, int port, String user, String tenant,
                              String password, String database, String table,
                              String outputDir, String format) {
        logger.info("开始使用 obdumper 导出数据...");
        logger.info("主机：{}:{}, 数据库：{}, 表：{}, 输出目录：{}", host, port, database, table, outputDir);

        List<String> command = new ArrayList<>();
        command.add(OBDUMPER_SCRIPT);
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
        command.add("--table");
        command.add(table);
        command.add("-f");
        command.add(outputDir);
        command.add("--no-sys");  // 不使用 sys 租户

        // 添加格式选项
        if ("csv".equalsIgnoreCase(format)) {
            command.add("--csv");
        } else if ("sql".equalsIgnoreCase(format)) {
            command.add("--sql");
        } else if ("cut".equalsIgnoreCase(format)) {
            command.add("--cut");
        }

        // 添加跳过目录检查
        command.add("--skip-check-dir");

        return executeCommand(command, "导出");
    }

    /**
     * 使用 obloader 导入数据
     *
     * @param host         主机 IP
     * @param port         端口
     * @param user         用户名（不含租户）
     * @param tenant       租户名
     * @param password     密码
     * @param database     数据库名
     * @param table        表名
     * @param inputDir     输入目录
     * @param format       数据格式：csv, sql, cut
     * @return 是否成功
     */
    public boolean importData(String host, int port, String user, String tenant,
                              String password, String database, String table,
                              String inputDir, String format) {
        logger.info("开始使用 obloader 导入数据...");
        logger.info("主机：{}:{}, 数据库：{}, 表：{}, 输入目录：{}", host, port, database, table, inputDir);

        List<String> command = new ArrayList<>();
        command.add(OBLOADER_SCRIPT);
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
        command.add("--table");
        command.add(table);
        command.add("-f");
        command.add(inputDir);
        command.add("--no-sys");  // 不使用 sys 租户

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
     *
     * @param host         主机 IP
     * @param port         端口
     * @param user         用户名
     * @param tenant       租户名
     * @param password     密码
     * @param database     数据库名
     * @param outputDir    输出目录
     * @return 是否成功
     */
    public boolean exportSchema(String host, int port, String user, String tenant,
                                String password, String database, String outputDir) {
        logger.info("开始使用 obdumper 导出表结构...");

        List<String> command = new ArrayList<>();
        command.add(OBDUMPER_SCRIPT);
        command.add("-h");
        command.add(host);
        command.add("-P");
        command.add(String.valueOf(port));
        command.add("-u");
        command.add(user + "@" + tenant);
        command.add("-t");
        command.add(tenant);
        command.add("-p");
        command.add(password);
        command.add("-D");
        command.add(database);
        command.add("-f");
        command.add(outputDir);
        command.add("--ddl");  // 只导出结构
        command.add("--no-sys");
        command.add("--skip-check-dir");

        return executeCommand(command, "导出表结构");
    }

    /**
     * 使用 obloader 导入表结构（DDL）
     *
     * @param host         主机 IP
     * @param port         端口
     * @param user         用户名
     * @param tenant       租户名
     * @param password     密码
     * @param database     数据库名
     * @param inputDir     输入目录
     * @return 是否成功
     */
    public boolean importSchema(String host, int port, String user, String tenant,
                                String password, String database, String inputDir) {
        logger.info("开始使用 obloader 导入表结构...");

        List<String> command = new ArrayList<>();
        command.add(OBLOADER_SCRIPT);
        command.add("-h");
        command.add(host);
        command.add("-P");
        command.add(String.valueOf(port));
        command.add("-u");
        command.add(user + "@" + tenant);
        command.add("-t");
        command.add(tenant);
        command.add("-p");
        command.add(password);
        command.add("-D");
        command.add(database);
        command.add("-f");
        command.add(inputDir);
        command.add("--ddl");  // 只导入结构
        command.add("--no-sys");

        return executeCommand(command, "导入表结构");
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
}
