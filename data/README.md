# OceanBase 数据导出导入工具

使用 OceanBase ob-loader-dumper 工具进行数据导出导入的 Java 实现。

## 环境信息

### 源数据库 (IP1)
| 配置项 | 值 |
|--------|-----|
| 主机 IP | 172.16.1.96 |
| 端口 | 52881 |
| 数据库 | test |
| 租户 | test |
| 用户名 | audaque@test |
| 密码 | Audaque@123 |

### 目标数据库 (IP2)
| 配置项 | 值 |
|--------|-----|
| 主机 IP | 172.16.1.223 |
| 端口 | 2881 |
| 数据库 | test |
| 租户 | sys |
| 用户名 | audaque@sys |
| 密码 | Audaque@123 |

### 工具路径
- **ob-loader-dumper**: `/opt/oceanbase/ob-loader-dumper-4.2.5-RELEASE`
- **输出目录**: `/opt/oceanbase/data/output`

---

## 快速开始

### 方式一：运行 Java 程序（推荐）

```bash
cd /opt/oceanbase/java-demo

# 1. 编译项目
mvn clean package

# 2. 运行程序
java -jar target/oceanbase-demo-1.0.0-jar-with-dependencies.jar
```

### 方式二：使用 Shell 脚本

```bash
/opt/oceanbase/data/migrate.sh
```

### 方式三：手动执行 SQL 命令

参考下文"手动执行步骤"。

---

## Java 程序说明

### 主类：ObLoaderDumperInvoker

`ObLoaderDumperInvoker.java` 是主程序，包含以下功能：

#### 1. ob-loader-dumper 工具调用方法

| 方法 | 功能 |
|------|------|
| `exportData()` | 使用 obdumper 导出数据（CSV/SQL/CUT 格式） |
| `importData()` | 使用 obloader 导入数据 |
| `exportSchema()` | 导出表结构（DDL） |
| `importSchema()` | 导入表结构（DDL） |
| `checkToolAvailable()` | 检查工具是否可用 |

#### 2. JDBC 数据库操作方法

| 方法 | 功能 |
|------|------|
| `createTableAndInsertData()` | 创建表并插入测试数据 |
| `createTable()` | 仅创建表 |
| `verifyData()` | 验证查询数据 |

### 配置参数

在 `ObLoaderDumperInvoker.java` 中修改以下配置：

```java
// ob-loader-dumper 工具路径
public static final String TOOL_HOME = "/opt/oceanbase/ob-loader-dumper-4.2.5-RELEASE";

// 源数据库配置
public static final String SOURCE_HOST = "172.16.1.96";
public static final int SOURCE_PORT = 52881;
public static final String SOURCE_USER = "audaque";
public static final String SOURCE_TENANT = "test";
public static final String SOURCE_PASSWORD = "Audaque@123";
public static final String SOURCE_DATABASE = "test";

// 目标数据库配置
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
```

### 运行流程

程序自动执行以下步骤：

1. **检查工具** - 验证 ob-loader-dumper 是否可用
2. **创建表并插入数据** - 在源数据库创建 test01 表并插入 10 条测试数据
3. **导出数据** - 使用 obdumper 将数据导出到 CSV 文件
4. **创建表** - 在目标数据库创建相同的表结构
5. **导入数据** - 使用 obloader 从 CSV 文件导入数据
6. **验证数据** - 查询并显示目标数据库中的数据

---

## 手动执行步骤

### 步骤 1: 在源数据库创建表并插入数据

```bash
mysql -h 172.16.1.96 -P 52881 -u "audaque@test" -p'Audaque@123' -D test << 'EOF'
DROP TABLE IF EXISTS test01;

CREATE TABLE test01 (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    name VARCHAR(100) COMMENT '姓名',
    code VARCHAR(50) COMMENT '编码',
    age INT COMMENT '年龄',
    salary DECIMAL(10,2) COMMENT '薪资',
    balance DOUBLE COMMENT '余额',
    status TINYINT COMMENT '状态',
    create_time DATETIME COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    birth_date DATE COMMENT '出生日期'
) COMMENT '测试表 test01';

INSERT INTO test01 (id, name, code, age, salary, balance, status, create_time, update_time, birth_date) VALUES
(1, '张三', 'CODE001', 25, 8000.00, 15000.50, 1, '2024-01-15 10:30:00', '2024-01-15 10:30:00', '1999-05-20'),
(2, '李四', 'CODE002', 30, 12000.00, 28000.75, 1, '2024-02-20 14:45:00', '2024-02-20 14:45:00', '1994-08-15'),
(3, '王五', 'CODE003', 28, 9500.00, 12000.00, 2, '2024-03-10 09:15:00', '2024-03-10 09:15:00', '1996-03-10'),
(4, '赵六', 'CODE004', 35, 15000.00, 45000.25, 1, '2024-04-05 16:20:00', '2024-04-05 16:20:00', '1989-11-25'),
(5, '孙七', 'CODE005', 22, 6000.00, 8000.50, 3, '2024-05-12 11:00:00', '2024-05-12 11:00:00', '2002-07-08'),
(6, '周八', 'CODE006', 40, 20000.00, 68000.00, 1, '2024-06-18 08:30:00', '2024-06-18 08:30:00', '1984-02-14'),
(7, '吴九', 'CODE007', 27, 8800.00, 19500.80, 2, '2024-07-22 15:45:00', '2024-07-22 15:45:00', '1997-09-30'),
(8, '郑十', 'CODE008', 33, 14000.00, 32000.60, 1, '2024-08-08 13:10:00', '2024-08-08 13:10:00', '1991-04-18'),
(9, '刘一', 'CODE009', 29, 10500.00, 22000.40, 3, '2024-09-14 10:25:00', '2024-09-14 10:25:00', '1995-12-05'),
(10, '陈二', 'CODE010', 31, 13000.00, 35000.90, 1, '2024-10-01 17:00:00', '2024-10-01 17:00:00', '1993-06-22');
EOF
```

### 步骤 2: 导出数据到 CSV

```bash
mkdir -p /opt/oceanbase/data/output

mysql -h 172.16.1.96 -P 52881 -u "audaque@test" -p'Audaque@123' -D test \
  -e "SELECT * FROM test01" | tail -n +2 | \
  while IFS=$'\t' read -r id name code age salary balance status create_time update_time birth_date; do
    echo "\"$id\",\"$name\",\"$code\",$age,$salary,$balance,$status,\"$create_time\",\"$update_time\",\"$birth_date\""
  done > /opt/oceanbase/data/output/test01.csv
```

### 步骤 3: 在目标数据库创建表

```bash
mysql -h 172.16.1.223 -P 2881 -u "audaque@sys" -p'Audaque@123' -D test << 'EOF'
DROP TABLE IF EXISTS test01;

CREATE TABLE test01 (
    id BIGINT PRIMARY KEY COMMENT '主键 ID',
    name VARCHAR(100) COMMENT '姓名',
    code VARCHAR(50) COMMENT '编码',
    age INT COMMENT '年龄',
    salary DECIMAL(10,2) COMMENT '薪资',
    balance DOUBLE COMMENT '余额',
    status TINYINT COMMENT '状态',
    create_time DATETIME COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    birth_date DATE COMMENT '出生日期'
) COMMENT '测试表 test01';
EOF
```

### 步骤 4: 导入数据到目标数据库

```bash
mysql -h 172.16.1.223 -P 2881 -u "audaque@sys" -p'Audaque@123' -D test << 'EOF'
LOAD DATA LOCAL INFILE '/opt/oceanbase/data/output/test01.csv'
INTO TABLE test01
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id, name, code, age, salary, balance, status, @create_time, @update_time, birth_date)
SET create_time = STR_TO_DATE(@create_time, '%Y-%m-%d %H:%i:%s'),
    update_time = STR_TO_DATE(@update_time, '%Y-%m-%d %H:%i:%s');
EOF
```

### 步骤 5: 验证数据

```bash
# 查询目标数据库数据
mysql -h 172.16.1.223 -P 2881 -u "audaque@sys" -p'Audaque@123' -D test \
  -e "SELECT * FROM test01 ORDER BY id;"
```

---

## 表结构说明

test01 表包含 10 个字段，涵盖字符、数值、日期类型：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 ID（数值） |
| name | VARCHAR(100) | 姓名（字符） |
| code | VARCHAR(50) | 编码（字符） |
| age | INT | 年龄（数值） |
| salary | DECIMAL(10,2) | 薪资（精确数值） |
| balance | DOUBLE | 余额（浮点数） |
| status | TINYINT | 状态（整数） |
| create_time | DATETIME | 创建时间（日期时间） |
| update_time | TIMESTAMP | 更新时间（时间戳） |
| birth_date | DATE | 出生日期（日期） |

---

## 项目结构

```
java-demo/
├── pom.xml
└── src/main/java/com/example/
    └── ObLoaderDumperInvoker.java  # 主程序
```

---

## 依赖说明

项目使用 Maven 管理依赖（pom.xml）：

| 依赖 | 版本 | 说明 |
|------|------|------|
| oceanbase-client | 2.4.3 | OceanBase JDBC 驱动 |
| slf4j-api | 1.7.36 | 日志 API |
| logback-classic | 1.2.11 | 日志实现 |

---

## 注意事项

1. **obdumper/obloader 工具限制**：
   - 对 OceanBase 租户模式（user@tenant）支持有限
   - 用户名参数 `-u` 只需要用户名部分，不要包含 `@tenant`（例如：`-u audaque`，而不是 `-u audaque@test`）
   - 租户信息通过 `-t` 参数单独指定
   - 使用 `--no-sys` 参数跳过 sys 租户认证

2. **JDBC 连接配置**：
   - Java 程序使用 `jdbc:oceanbase://` URL 格式（不是 `jdbc:mysql://`）
   - 程序直接实例化 `com.oceanbase.jdbc.Driver` 类创建连接
   - 用户名格式为 `user@tenant`（例如：`audaque@test`）

3. **LOAD DATA LOCAL INFILE**：
   - 需要确保 MySQL 客户端和服务端都允许 `local_infile`
   - 检查命令：`SHOW GLOBAL VARIABLES LIKE 'local_infile';`
   - 设置命令：`SET GLOBAL local_infile = 1;`

4. **字符集**：确保源数据库和目标数据库使用相同的字符集（推荐 utf8mb4）

5. **时区**：TIMESTAMP 类型会受时区影响，确保两个数据库的时区设置一致

6. **JVM 要求**：Java 8 或更高版本

7. **Maven 打包**：
   - 使用 `maven-shade-plugin` 打包可执行 jar
   - 自动处理 `META-INF/services/java.sql.Driver` 服务注册文件
   - 生成的 jar 文件：`target/oceanbase-demo-1.0.0-jar-with-dependencies.jar`

---

## 参考资料

- [OceanBase 官方文档](https://www.oceanbase.com/docs/common-oceanbase-database-cn-1000000002012768)
- [ob-loader-dumper 使用指南](https://www.oceanbase.com/docs/oceanbase-dumper-loader-cn)
