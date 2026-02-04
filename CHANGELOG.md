# Changelog

All notable changes to this project will be documented in this file.

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
