## [1.8.0-SNAPSHOT] - 2026-03-17

### Features
- **独立执行器应用**: 新增 `silence-job-client-executor` 模块，支持独立部署的客户端执行器
- 自动配置独立部署模式
- 支持多实例水平扩展
- 健康检查和监控端点

### Changes
- 版本号统一为 1.8.0-SNAPSHOT
- 优化模块依赖关系

## [1.5.0] - 2024-02-04

### Features
- Spring Boot Starter for zero-configuration setup
- 任务执行框架：串行、并行、MapReduce 执行器
- 重试机制：支持多种退避策略
- 阻塞策略：串行、丢弃、覆盖
- gRPC/HTTP 双协议支持
- 任务上下文管理
- 执行结果上报

### Dependencies
- silence-job-common 1.0.0
- Spring Boot 2.7.x
- gRPC Java 1.58.0
