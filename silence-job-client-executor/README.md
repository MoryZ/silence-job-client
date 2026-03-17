# SilenceJob Client Executor

独立的SilenceJob客户端执行器应用，用于分布式任务执行。

## 📋 功能特性

- ✅ 独立部署的Spring Boot应用
- ✅ 支持多实例水平扩展
- ✅ 自动向Server注册和心跳
- ✅ 任务执行和结果上报
- ✅ 重试机制支持
- ✅ 健康检查和监控

## 🚀 快速开始

### 1. 构建项目

```bash
cd silence-job-client
mvn clean package -DskipTests
```

### 2. 启动执行器

#### 开发环境
```bash
# 使用默认配置 (端口9999, 开发环境)
./silence-job-client-executor/start-executor.sh

# 或直接使用Java命令
java -jar silence-job-client-executor/target/silence-job-client-executor-1.5.0.jar
```

#### 生产环境
```bash
# 使用生产配置
./silence-job-client-executor/start-executor.sh --prod
```

### 3. 验证启动

```bash
# 检查应用健康状态
curl http://localhost:9999/actuator/health

# 查看应用信息
curl http://localhost:9999/actuator/info
```

## ⚙️ 配置说明

### 基本配置 (application.yml)

```yaml
server:
  port: 9999  # 执行器端口

silence:
  job:
    client:
      server-url: http://localhost:8098  # Server地址
      app-name: silence-job-client-executor  # 应用名称
      executor:
        port: 9999  # 执行器端口
        thread-pool-size: 10  # 线程池大小
        queue-size: 100  # 队列大小
```

### 环境变量配置

```bash
# Server连接配置
export SILENCE_JOB_CLIENT_SERVER_URL=http://your-server:8098
export SILENCE_JOB_CLIENT_APP_NAME=my-executor

# 执行器配置
export SILENCE_JOB_CLIENT_EXECUTOR_PORT=9999
export SILENCE_JOB_CLIENT_EXECUTOR_THREAD_POOL_SIZE=20
```

## 📊 监控和运维

### 健康检查
- 端点: `GET /actuator/health`
- 返回: 应用和依赖服务的健康状态

### 指标监控
- 端点: `GET /actuator/metrics`
- 包含: JVM信息、任务执行统计等

### 日志配置
- 开发环境: INFO级别
- 生产环境: WARN级别

## 🔧 部署架构

```
┌─────────────────┐    ┌──────────────────┐
│   Business App  │────│  SilenceJob      │
│   (with starter)│    │  Server (8098)   │
└─────────────────┘    └──────────────────┘
         │                       │
         └───────────────────────┘
                 │
        ┌─────────────────┐
        │  Client Executor│
        │    (9999)       │
        └─────────────────┘
```

## 📝 使用场景

### 场景1: 独立执行器集群
```bash
# 启动多个执行器实例
./start-executor.sh --jar=executor-1.jar  # 端口9999
./start-executor.sh --jar=executor-2.jar  # 端口10000 (需修改配置)
```

### 场景2: 容器化部署
```dockerfile
FROM openjdk:21-jdk
COPY target/silence-job-client-executor-1.5.0.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔍 故障排除

### 连接Server失败
- 检查 `silence.job.client.server-url` 配置
- 确认Server应用已启动并可访问

### 任务执行失败
- 查看应用日志
- 检查线程池配置是否合理
- 确认任务处理器已正确注册

### 性能问题
- 调整 `executor.thread-pool-size`
- 监控JVM内存使用情况
- 考虑增加执行器实例数量