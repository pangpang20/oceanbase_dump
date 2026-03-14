# OceanBase 数据导出导入操作文档

## 环境信息

### 源数据库 (IP1)
- **主机 IP**: 172.16.1.96
- **端口**: 52881
- **数据库**: test
- **租户**: test
- **用户名**: audaque@test
- **密码**: Audaque@123

### 目标数据库 (IP2)
- **主机 IP**: 172.16.1.223
- **端口**: 2881
- **数据库**: test
- **租户**: sys (注意：此处租户配置为 sys，但实际连接使用 audaque@sys)
- **用户名**: audaque@sys
- **密码**: Audaque@123

### 数据导出目录
- **路径**: /opt/oceanbase/data/output

---

## 步骤 1: 在源数据库创建表并插入数据

### 创建表 SQL

```sql
-- 创建表 test01，包含 10 个字段（字符、数值、日期类型）
-- 如果表已存在，先删除旧表
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
```

### 插入 10 条测试数据 SQL

```sql
-- 插入 10 条测试数据
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
```

### 执行命令

```bash
# 连接到源数据库并执行 SQL
mysql -h 172.16.1.96 -P 52881 -u "audaque@test" -p'Audaque@123' -D test << 'EOF'
-- 如果表已存在，先删除旧表
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

---

## 步骤 2: 导出数据到 CSV 文件

### 方法一：使用 mysql 命令导出（推荐）

```bash
# 创建输出目录
mkdir -p /opt/oceanbase/data/output

# 导出数据为 CSV 格式
mysql -h 172.16.1.96 -P 52881 -u "audaque@test" -p'Audaque@123' -D test \
  -e "SELECT * FROM test01" | tail -n +2 | \
  while IFS=$'\t' read -r id name code age salary balance status create_time update_time birth_date; do
    echo "\"$id\",\"$name\",\"$code\",$age,$salary,$balance,$status,\"$create_time\",\"$update_time\",\"$birth_date\""
  done > /opt/oceanbase/data/output/test01.csv
```

### 方法二：使用 SELECT INTO OUTFILE（需要文件权限）

```sql
SELECT * FROM test01
INTO OUTFILE '/opt/oceanbase/data/output/test01.csv'
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n';
```

### 方法三：使用 obdumper 工具（需要正确配置租户）

```bash
cd /opt/oceanbase/ob-loader-dumper-4.2.5-RELEASE

# 注意：obdumper 对 OceanBase 租户模式支持有限，可能无法正确处理 audaque@test 格式的用户名
# 如遇 Access denied 错误，请使用方法一或方法二
./bin/obdumper -h '172.16.1.96' -P '52881' -u 'audaque' -t 'test' -c 'obcluster' \
  -p 'Audaque@123' -D 'test' --table 'test01' --csv \
  -f '/opt/oceanbase/data/output' --skip-check-dir --no-sys
```

---

## 步骤 3: 在目标数据库创建表

### 执行命令

```bash
# 连接到目标数据库并执行 SQL
mysql -h 172.16.1.223 -P 2881 -u "audaque@sys" -p'Audaque@123' -D test << 'EOF'
-- 如果表已存在，先删除旧表
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

---

## 步骤 4: 导入数据到目标数据库

### 方法一：使用 LOAD DATA LOCAL INFILE（推荐）

```bash
# 首先确认 CSV 文件存在
ls -la /opt/oceanbase/data/output/test01.csv

# 导入数据
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

### 方法二：使用 obloader 工具

```bash
cd /opt/oceanbase/ob-loader-dumper-4.2.5-RELEASE

# 注意：obloader 对 OceanBase 租户模式支持有限
./bin/obloader -h '172.16.1.223' -P '2881' -u 'audaque' -t 'test' -c 'obcluster' \
  -p 'Audaque@123' -D 'test' --table 'test01' --csv \
  -f '/opt/oceanbase/data/output' --no-sys
```

---

## 步骤 5: 验证数据

### 查询源数据库数据

```bash
mysql -h 172.16.1.96 -P 52881 -u "audaque@test" -p'Audaque@123' -D test \
  -e "SELECT * FROM test01 ORDER BY id;"
```

### 查询目标数据库数据

```bash
mysql -h 172.16.1.223 -P 2881 -u "audaque@sys" -p'Audaque@123' -D test \
  -e "SELECT * FROM test01 ORDER BY id;"
```

### 对比数据条数

```bash
# 源数据库
mysql -h 172.16.1.96 -P 52881 -u "audaque@test" -p'Audaque@123' -D test \
  -e "SELECT COUNT(*) AS '源数据库记录数' FROM test01;"

# 目标数据库
mysql -h 172.16.1.223 -P 2881 -u "audaque@sys" -p'Audaque@123' -D test \
  -e "SELECT COUNT(*) AS '目标数据库记录数' FROM test01;"
```

---

## 完整的一键执行脚本

```bash
#!/bin/bash
set -e

echo "========== OceanBase 数据导出导入脚本 =========="

# 配置变量
SOURCE_HOST="172.16.1.96"
SOURCE_PORT="52881"
SOURCE_USER="audaque@test"
SOURCE_PASS="Audaque@123"
SOURCE_DB="test"

TARGET_HOST="172.16.1.223"
TARGET_PORT="2881"
TARGET_USER="audaque@sys"
TARGET_PASS="Audaque@123"
TARGET_DB="test"

OUTPUT_DIR="/opt/oceanbase/data/output"

# 创建输出目录
mkdir -p "$OUTPUT_DIR"

echo "步骤 1: 在源数据库创建表并插入数据..."
mysql -h "$SOURCE_HOST" -P "$SOURCE_PORT" -u "$SOURCE_USER" -p"$SOURCE_PASS" -D "$SOURCE_DB" << 'EOF'
CREATE TABLE IF NOT EXISTS test01 (
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
echo "源数据准备完成！"

echo "步骤 2: 导出数据到 CSV..."
mysql -h "$SOURCE_HOST" -P "$SOURCE_PORT" -u "$SOURCE_USER" -p"$SOURCE_PASS" -D "$SOURCE_DB" \
  -e "SELECT * FROM test01" | tail -n +2 | \
  while IFS=$'\t' read -r id name code age salary balance status create_time update_time birth_date; do
    echo "\"$id\",\"$name\",\"$code\",$age,$salary,$balance,$status,\"$create_time\",\"$update_time\",\"$birth_date\""
  done > "$OUTPUT_DIR/test01.csv"
echo "数据导出完成：$OUTPUT_DIR/test01.csv"

echo "步骤 3: 在目标数据库创建表..."
mysql -h "$TARGET_HOST" -P "$TARGET_PORT" -u "$TARGET_USER" -p"$TARGET_PASS" -D "$TARGET_DB" << 'EOF'
CREATE TABLE IF NOT EXISTS test01 (
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
echo "目标表创建完成！"

echo "步骤 4: 导入数据到目标数据库..."
mysql -h "$TARGET_HOST" -P "$TARGET_PORT" -u "$TARGET_USER" -p"$TARGET_PASS" -D "$TARGET_DB" << EOF
LOAD DATA LOCAL INFILE '$OUTPUT_DIR/test01.csv'
INTO TABLE test01
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id, name, code, age, salary, balance, status, @create_time, @update_time, birth_date)
SET create_time = STR_TO_DATE(@create_time, '%Y-%m-%d %H:%i:%s'),
    update_time = STR_TO_DATE(@update_time, '%Y-%m-%d %H:%i:%s');
EOF
echo "数据导入完成！"

echo "步骤 5: 验证数据..."
echo "源数据库数据："
mysql -h "$SOURCE_HOST" -P "$SOURCE_PORT" -u "$SOURCE_USER" -p"$SOURCE_PASS" -D "$SOURCE_DB" \
  -e "SELECT COUNT(*) AS '记录数' FROM test01;"

echo "目标数据库数据："
mysql -h "$TARGET_HOST" -P "$TARGET_PORT" -u "$TARGET_USER" -p"$TARGET_PASS" -D "$TARGET_DB" \
  -e "SELECT COUNT(*) AS '记录数' FROM test01;"

echo "========== 所有步骤完成 =========="
```

---

## 表结构说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 ID |
| name | VARCHAR(100) | 姓名（字符类型） |
| code | VARCHAR(50) | 编码（字符类型） |
| age | INT | 年龄（数值类型） |
| salary | DECIMAL(10,2) | 薪资（精确数值类型） |
| balance | DOUBLE | 余额（浮点数值类型） |
| status | TINYINT | 状态（整数类型） |
| create_time | DATETIME | 创建时间（日期时间类型） |
| update_time | TIMESTAMP | 更新时间（时间戳类型） |
| birth_date | DATE | 出生日期（日期类型） |

---

## 注意事项

1. **obdumper/obloader 工具限制**：这两个工具对 OceanBase 的租户模式（user@tenant）支持有限，可能会将用户名中的 `@tenant` 部分去掉，导致认证失败。建议使用 MySQL 原生命令进行数据导出导入。

2. **LOAD DATA LOCAL INFILE**：使用此命令需要确保 MySQL 客户端和服务端都允许 `local_infile`。可通过以下命令检查和设置：
   ```sql
   SHOW GLOBAL VARIABLES LIKE 'local_infile';
   SET GLOBAL local_infile = 1;
   ```

3. **字符集**：确保源数据库和目标数据库使用相同的字符集（推荐 utf8mb4）。

4. **时区**：TIMESTAMP 类型会受时区影响，确保两个数据库的时区设置一致。
