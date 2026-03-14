#!/bin/bash
set -e

echo "========================================"
echo "  OceanBase 数据导出导入脚本"
echo "========================================"

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

echo ""
echo "步骤 1: 在源数据库创建表并插入数据..."
echo "----------------------------------------"
mysql -h "$SOURCE_HOST" -P "$SOURCE_PORT" -u "$SOURCE_USER" -p"$SOURCE_PASS" -D "$SOURCE_DB" << 'EOSQL'
-- 创建表 test01 (如果已存在则先清空)
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
EOSQL
echo "✓ 源数据准备完成！"

echo ""
echo "步骤 2: 导出数据到 CSV..."
echo "----------------------------------------"
mysql -h "$SOURCE_HOST" -P "$SOURCE_PORT" -u "$SOURCE_USER" -p"$SOURCE_PASS" -D "$SOURCE_DB" \
  -e "SELECT * FROM test01" | tail -n +2 | \
  while IFS=$'\t' read -r id name code age salary balance status create_time update_time birth_date; do
    echo "\"$id\",\"$name\",\"$code\",$age,$salary,$balance,$status,\"$create_time\",\"$update_time\",\"$birth_date\""
  done > "$OUTPUT_DIR/test01.csv"
echo "✓ 数据导出完成：$OUTPUT_DIR/test01.csv"
echo "  文件内容预览:"
head -3 "$OUTPUT_DIR/test01.csv"

echo ""
echo "步骤 3: 在目标数据库创建表..."
echo "----------------------------------------"
mysql -h "$TARGET_HOST" -P "$TARGET_PORT" -u "$TARGET_USER" -p"$TARGET_PASS" -D "$TARGET_DB" << 'EOSQL'
-- 创建表 test01 (如果已存在则先清空)
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
EOSQL
echo "✓ 目标表创建完成！"

echo ""
echo "步骤 4: 导入数据到目标数据库..."
echo "----------------------------------------"
mysql -h "$TARGET_HOST" -P "$TARGET_PORT" -u "$TARGET_USER" -p"$TARGET_PASS" -D "$TARGET_DB" << EOSQL
LOAD DATA LOCAL INFILE '$OUTPUT_DIR/test01.csv'
INTO TABLE test01
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id, name, code, age, salary, balance, status, @create_time, @update_time, birth_date)
SET create_time = STR_TO_DATE(@create_time, '%Y-%m-%d %H:%i:%s'),
    update_time = STR_TO_DATE(@update_time, '%Y-%m-%d %H:%i:%s');
EOSQL
echo "✓ 数据导入完成！"

echo ""
echo "步骤 5: 验证数据..."
echo "----------------------------------------"
echo "源数据库数据:"
mysql -h "$SOURCE_HOST" -P "$SOURCE_PORT" -u "$SOURCE_USER" -p"$SOURCE_PASS" -D "$SOURCE_DB" \
  -e "SELECT COUNT(*) AS '记录数' FROM test01;"

echo ""
echo "目标数据库数据:"
mysql -h "$TARGET_HOST" -P "$TARGET_PORT" -u "$TARGET_USER" -p"$TARGET_PASS" -D "$TARGET_DB" \
  -e "SELECT COUNT(*) AS '记录数' FROM test01;"

echo ""
echo "目标数据库详细数据:"
mysql -h "$TARGET_HOST" -P "$TARGET_PORT" -u "$TARGET_USER" -p"$TARGET_PASS" -D "$TARGET_DB" \
  -e "SELECT id, name, code, age, salary, balance, status, create_time, update_time, birth_date FROM test01 ORDER BY id;"

echo ""
echo "========================================"
echo "  所有步骤完成！"
echo "========================================"
