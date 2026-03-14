# OceanBase 数据导出导入工具

OceanBase ob-loader-dumper 工具调用示例，实现数据从源数据库导出并导入到目标数据库。

## 项目说明

本项目提供以下方式完成数据迁移：

1. **Java 程序** - 调用 ob-loader-dumper 工具进行数据导出导入
2. **Shell 脚本** - 一键执行数据迁移
3. **手动 SQL** - 通过 MySQL 命令手动执行

## 快速开始

### 环境要求

- Java 8 或更高版本
- Maven 3.x
- ob-loader-dumper 工具
- MySQL 客户端

### 配置说明

在运行前需要配置以下信息：

- 源数据库连接信息（主机、端口、用户名、密码、数据库名）
- 目标数据库连接信息
- ob-loader-dumper 工具路径
- 数据导出目录

详细配置请参考 [data/README.md](data/README.md)

## 项目结构

```
oceanbase/
├── README.md                      # 项目说明
├── .gitignore                     # Git 忽略配置
├── data/                          # 数据迁移相关
│   ├── README.md                  # 详细使用文档
│   ├── migrate.sh                 # 一键迁移脚本
│   └── output/                    # 数据导出目录
└── java-demo/                     # Java 示例程序
    ├── pom.xml                    # Maven 配置
    └── src/main/java/com/example/
        └── ObLoaderDumperInvoker.java  # 主程序
```

## 使用方式

### 1. 运行 Java 程序

```bash
cd java-demo
mvn clean package
java -jar target/oceanbase-demo-1.0.0-jar-with-dependencies.jar
```

### 2. 运行 Shell 脚本

```bash
./data/migrate.sh
```

### 3. 手动执行

参考 [data/README.md](data/README.md) 中的"手动执行步骤"章节。

## 注意事项

1. **obdumper/obloader 用户名格式**：`-u` 参数只传用户名（如 `audaque`），不要包含 `@tenant`
2. **租户信息**：通过 `-t` 参数单独指定（如 `-t test`）
3. **JDBC URL**：Java 程序使用 `jdbc:oceanbase://` 格式
4. **字符集**：确保源数据库和目标数据库字符集一致（推荐 utf8mb4）

## 工具下载

[ob-loader-dumper 下载地址](https://obbusiness-private.oss-cn-shanghai.aliyuncs.com/download-center/opensource/ob_loader_dumper/4.2.5/ob-loader-dumper-4.2.5-RELEASE.zip)

## 详细文档

完整的使用说明请参考 [data/README.md](data/README.md)

## 参考资料

- [OceanBase 官方文档](https://www.oceanbase.com/docs/common-oceanbase-database-cn-1000000002012768)
- [ob-loader-dumper 使用指南](https://www.oceanbase.com/docs/oceanbase-dumper-loader-cn)
