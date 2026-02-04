# SilenceJob Client

分布式任务调度系统的客户端实现，提供任务执行框架和重试机制。

## 📦 模块说明

### silence-job-client-starter
Spring Boot 自动配置模块，开箱即用的集成方案：
- 自动配置 Job 执行器
- 自动配置重试框架
- 自动注册任务处理器

### silence-job-client-common
客户端公共模块，提供任务执行上下文和基础组件：
- 任务执行上下文管理
- 客户端配置项
- 通用工具类

### silence-job-client-job-core
客户端核心模块，实现任务执行引擎：
- **任务执行器**：支持多种执行策略（串行、并行、MapReduce）
- **调度通信**：与服务端的 gRPC/HTTP 通信
- **任务上报**：执行结果上报和心跳机制
- **阻塞策略**：支持串行、丢弃、覆盖等策略

### silence-job-client-retry-core
重试任务核心模块，实现智能重试机制：
- **重试执行器**：支持本地/远程重试
- **退避策略**：固定延迟、指数退避、斐波那契退避
- **幂等性保障**：防止重复执行
- **结果上报**：重试结果回调

## 🚀 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>com.old.silence</groupId>
    <artifactId>silence-job-client-starter</artifactId>
    <version>1.5.0</version>
</dependency>
```

### 配置示例

```yaml
silence:
  job:
    client:
      server-url: http://localhost:8080
      app-name: my-application
      executor:
        port: 9999
```

## 📖 依赖关系

本项目依赖：
- `silence-job-common` v1.0.0 - 公共组件库

## 📄 许可证

Apache License 2.0
