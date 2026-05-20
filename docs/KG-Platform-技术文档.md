# KG Platform — 知识图谱管理平台

> 项目全栈技术文档
> 版本：1.0.0
> 日期：2026-05-20

---

## 一、项目概述

### 1.1 项目简介

KG Platform（知识图谱管理平台）是一个面向知识图谱构建与管理的全栈应用，提供从数据导入、实体标注、模型训练到图谱可视化的完整链路。

### 1.2 技术栈

| 层级 | 技术选型 | 版本 |
|------|---------|------|
| **后端框架** | Spring Boot | 3.3.0 |
| **ORM** | MyBatis-Plus | 3.5.6 |
| **数据库** | MySQL 8.0 | — |
| **图数据库** | Neo4j (Spring Data Neo4j) | — |
| **对象存储** | MinIO | 8.5.7 |
| **大模型** | DashScope SDK (通义千问) | 2.20.0 |
| **安全** | Spring Security + JWT (jjwt) | 0.12.5 |
| **API 文档** | SpringDoc OpenAPI / Knife4j | 2.5.0 |
| **工具库** | Hutool | 5.8.26 |
| **前端框架** | Vue 3 | 3.4.21 |
| **构建工具** | Vite | 5.2.8 |
| **UI 框架** | Element Plus | 2.7.2 |
| **状态管理** | Pinia | 2.1.7 |
| **HTTP 客户端** | Axios | 1.6.8 |
| **图表库** | ECharts | 6.1.0 |
| **Java 版本** | JDK 17 | — |

---

## 二、项目结构

```
d:\demo\
├── src/main/java/com/example/kgplatform/
│   ├── KgPlatformApplication.java        # 启动类
│   ├── common/
│   │   ├── R.java                       # 统一响应封装
│   │   ├── PageQuery.java               # 分页请求参数
│   │   └── PageResult.java              # 分页响应结果
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security 配置
│   │   ├── CorsConfig.java              # CORS 跨域配置
│   │   ├── JwtConfig.java               # JWT 参数配置
│   │   ├── MinioConfig.java             # MinIO 对象存储配置
│   │   ├── Neo4jConfig.java             # Neo4j 图数据库配置
│   │   ├── DashScopeProperties.java    # DashScope 大模型配置
│   │   ├── DataInitializer.java         # 启动数据初始化
│   │   └── Knife4jConfig.java           # API 文档配置
│   ├── controller/                       # 13 个控制器
│   │   ├── AuthController.java          # 认证管理
│   │   ├── HomeController.java          # 系统首页
│   │   ├── SysUserController.java       # 用户管理
│   │   ├── SysRoleController.java       # 角色管理
│   │   ├── KnowledgeGraphController.java # 知识图谱管理
│   │   ├── GraphModelController.java    # 图谱模型管理
│   │   ├── CorpusController.java        # 语料管理
│   │   ├── AnnotationController.java    # 标注管理
│   │   ├── ModelTrainController.java    # 模型训练管理
│   │   ├── ExtractController.java       # 知识抽取管理
│   │   ├── DataTransformController.java  # 数据转化管理
│   │   ├── GraphExploreController.java   # 图谱探索
│   │   ├── FileController.java          # 文件管理（头像）
│   │   └── MinioController.java         # MinIO 文件管理
│   ├── service/                          # 服务层（14 个 + 2 个存储服务）
│   │   ├── SysUserService.java
│   │   ├── SysRoleService.java
│   │   ├── KgGraphService.java
│   │   ├── KgModelService.java
│   │   ├── KgCorpusService.java
│   │   ├── KgAnnotationTaskService.java
│   │   ├── KgAnnotationRecordService.java
│   │   ├── KgTrainTaskService.java
│   │   ├── KgExtractTaskService.java
│   │   ├── KgTransformTaskService.java
│   │   ├── KgNodeInstanceService.java
│   │   ├── KgEdgeInstanceService.java
│   │   ├── DashScopeService.java         # 通义千问 LLM 服务
│   │   ├── MinioService.java             # MinIO 对象存储
│   │   └── Neo4jService.java             # Neo4j 图数据库
│   ├── mapper/                           # MyBatis-Plus Mapper（12 个）
│   ├── entity/                           # 实体类（18 个）
│   │   ├── SysUser.java
│   │   ├── SysRole.java
│   │   ├── KgGraph.java
│   │   ├── KgModel.java
│   │   ├── KgCorpus.java
│   │   ├── KgAnnotationTask.java
│   │   ├── KgAnnotationRecord.java
│   │   ├── KgTrainTask.java
│   │   ├── KgExtractTask.java
│   │   ├── KgTransformTask.java
│   │   ├── KgNodeInstance.java
│   │   ├── KgEdgeInstance.java
│   │   ├── Neo4jEntity.java              # Neo4j 实体节点
│   │   └── Neo4jRelation.java             # Neo4j 关系
│   ├── dto/                              # 数据传输对象
│   │   ├── LoginDTO.java
│   │   ├── LoginVO.java
│   │   ├── PasswordDTO.java
│   │   ├── AdminPasswordDTO.java
│   │   └── LlmExtractResult.java         # LLM 抽取结果 DTO
│   ├── repository/                       # Neo4j 数据访问层
│   │   ├── Neo4jEntityRepository.java
│   │   └── Neo4jRelationRepository.java
│   └── security/
│       ├── JwtAuthFilter.java            # JWT 认证过滤器
│       └── UserDetailsServiceImpl.java
├── src/main/resources/
│   ├── application.yml                   # 应用配置
│   ├── schema.sql                        # 数据库建表脚本（含迁移逻辑）
│   └── migration.sql                      # 增量迁移脚本
├── frontend/                             # Vue 3 前端
│   ├── src/
│   │   ├── main.js                       # 前端入口
│   │   ├── App.vue
│   │   ├── api/                          # API 接口层（11 个）
│   │   ├── router/index.js                # 路由配置
│   │   ├── stores/user.js                # Pinia 用户状态
│   │   ├── utils/request.js              # Axios 封装
│   │   ├── layout/index.vue              # 整体布局
│   │   └── views/                        # 页面组件（18 个）
│   ├── vite.config.js                    # Vite 配置
│   └── package.json
├── pom.xml                               # Maven 依赖
└── start.bat                             # 一键启动脚本
```

---

## 三、架构设计

### 3.1 请求流程

```
浏览器 (localhost:5173)
    │
    │  GET /api/auth/login
    ▼
Vite Dev Server (代理)
    │
    │  /api/* → http://localhost:8090/*
    ▼
Spring Boot (localhost:8090)
    │
    ├─ JwtAuthFilter  ←── 验证 JWT Token
    ├─ CorsFilter     ←── 处理跨域
    └─ DispatcherServlet
          │
          ▼
    Controller（13 个）
          │
          ├──► Service → Mapper → MySQL
          ├──► MinioService → MinIO 对象存储
          └──► Neo4jService → Neo4j 图数据库
          │
          ▼
    R<T> 统一响应 { code, msg, data }
```

### 3.2 图数据库存储架构

```
MySQL (元数据存储)
│  kg_graph        — 图谱项目信息
│  kg_model        — 图谱模型
│  kg_corpus       — 语料
│  kg_node_instance — 节点元数据
│  kg_edge_instance  — 边元数据
│
└─► Neo4j (实体关系存储)
   │  Entity 节点 — 存储图谱实体（人物、机构、地点等）
   │  RELATION 边 — 存储实体间关系
   │
   └─► MinIO (文件存储)
      │  uploads/  — 通用上传文件
      │  corpus/   — 语料文件
      │  models/   — 模型文件
      │  exports/  — 导出结果
```

### 3.3 认证流程

```
登录 POST /api/auth/login
    │
    ▼
验证用户名 + BCrypt 密码
    │
    ▼
签发 JWT Token (HS256, 24h 有效期)
    ├─ subject: username
    └─ claim: userId
    │
    ▼
前端存入 localStorage.token
    │
    ▼
后续请求 Header: Authorization: Bearer <token>
    │
    ▼
JwtAuthFilter 验证并注入 SecurityContext
```

---

## 四、后端接口文档

### 4.1 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... }
}
```

- `200`：成功
- `401`：未授权 / 认证失败
- `403`：禁止访问
- `500`：服务器错误

### 4.2 接口清单

#### 认证管理 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/login` | 用户登录 | 否 |
| POST | `/logout` | 用户退出 | 是 |
| GET | `/info` | 获取当前用户信息 | 是 |
| POST | `/password` | 修改密码（需填旧密码） | 是 |
| POST | `/register` | 用户注册 | 否 |

**登录请求体：**
```json
{ "username": "admin", "password": "admin123" }
```
**登录响应数据：**
```json
{
  "userId": 1,
  "username": "admin",
  "nickname": "Administrator",
  "avatar": null,
  "token": "eyJ...",
  "roleId": 1,
  "roleName": "系统管理员",
  "roleCode": "ADMIN"
}
```

#### 系统首页 `/api/home`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/dashboard` | 获取首页统计数据 | 是 |

**响应数据：**
```json
{
  "graphCount": 2,
  "modelCount": 2,
  "trainTaskCount": 0,
  "corpusCount": 2,
  "graphTrend": [12, 19, 15, 22, 18, 25, 30],
  "taskTrend": [8, 12, 10, 15, 14, 18, 20]
}
```

#### 知识图谱管理 `/api/kg/graph`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 新增图谱 | 是 |
| PUT | `/{id}` | 修改图谱 | 是 |
| DELETE | `/{id}` | 删除图谱 | 是 |

#### 图谱模型管理 `/api/kg/model`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 新增模型 | 是 |
| PUT | `/{id}` | 修改模型 | 是 |
| DELETE | `/{id}` | 删除模型 | 是 |

#### 语料管理 `/api/corpus`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 新增语料 | 是 |
| PUT | `/{id}` | 修改语料 | 是 |
| DELETE | `/{id}` | 删除语料 | 是 |
| POST | `/upload` | 上传语料文件（支持 MinIO 或本地存储，上传 .txt 文件时自动提取文本内容） | 是 |
| GET | `/file/{filename}` | 下载语料文件 | 是 |
| PUT | `/{id}/content` | 设置语料文本内容（供 LLM 抽取使用） | 是 |

**上传请求参数：**
- `file`（必填）：Multipart 文件
- `name`（选填）：语料名称，默认使用原文件名
- `graphId`（选填）：关联图谱 ID

#### 模型训练管理 `/api/train`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 创建训练任务 | 是 |
| POST | `/{id}/start` | 启动训练 | 是 |
| POST | `/{id}/stop` | 停止训练 | 是 |
| GET | `/{id}/metrics` | 获取训练指标 | 是 |
| GET | `/{id}/logs` | 获取训练日志 | 是 |
| DELETE | `/{id}` | 删除任务 | 是 |

#### 标注管理 `/api/annotation`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/task/list` | 标注任务列表 | 是 |
| POST | `/task` | 创建标注任务 | 是 |
| PUT | `/task/{id}` | 修改标注任务 | 是 |
| POST | `/task/{id}/assign` | 分配任务 | 是 |
| POST | `/task/{id}/generate` | 批量生成标注记录（生成 10 条） | 是 |
| GET | `/record/list` | 标注记录列表 | 是 |
| POST | `/record` | 提交标注 | 是 |
| POST | `/record/{id}/review` | 审核标注记录（action: approve/reject） | 是 |
| GET | `/task/{id}/next` | 获取待标注数据 | 是 |
| DELETE | `/task/{id}` | 删除标注任务 | 是 |

#### 知识抽取 `/api/extract`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 抽取任务列表 | 是 |
| GET | `/{id}` | 获取任务详情 | 是 |
| POST | `/dl` | 创建深度学习抽取任务 | 是 |
| POST | `/llm` | 创建 LLM 抽取任务 | 是 |
| POST | `/{id}/start` | 启动抽取 | 是 |
| POST | `/{id}/stop` | 停止抽取 | 是 |
| GET | `/{id}/result` | 获取抽取结果 | 是 |
| DELETE | `/{id}` | 删除任务 | 是 |

#### 数据转换 `/api/data/transform`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 转换任务列表 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 新增转换任务 | 是 |
| POST | `/{id}/execute` | 执行转换 | 是 |
| DELETE | `/{id}` | 删除任务 | 是 |

#### 图谱探索 `/api/explore`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/graph/{graphId}` | 查询图谱节点和边（支持 relationType 过滤） | 是 |
| GET | `/node/search` | 搜索节点（keyword + 可选 graphId） | 是 |
| GET | `/node/{id}` | 节点详情 | 是 |
| GET | `/graphs` | 图谱列表 | 是 |
| GET | `/stats/{graphId}` | 图谱统计数据（节点数/边数/Neo4j 节点数/边数） | 是 |
| POST | `/path` | 路径分析（startId/endId/graphId） | 是 |
| GET | `/relation-types/{graphId}` | 获取关系类型列表 | 是 |

#### 用户管理 `/api/system/user`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询（非管理员只能看自己） | 是 |
| GET | `/{id}` | 获取详情（非管理员只能查自己） | 是 |
| POST | `/` | 新增用户（默认密码 123456） | 是 |
| PUT | `/{id}` | 修改用户 | 是 |
| DELETE | `/{id}` | 删除用户 | 是 |
| POST | `/{id}/reset-password` | 重置密码为 123456 | 是 |
| POST | `/{id}/password` | 管理员修改指定用户密码（无需旧密码） | 是 |

#### 角色管理 `/api/system/role`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 新增角色（仅 ADMIN） | 是 |
| PUT | `/{id}` | 修改角色（仅 ADMIN） | 是 |
| DELETE | `/{id}` | 删除角色（仅 ADMIN） | 是 |
| GET | `/all` | 获取所有角色 | 是 |

### 4.3 分页参数

所有列表接口支持分页参数：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `pageNum` | int | 1 | 页码 |
| `pageSize` | int | 10 | 每页条数 |
| `keyword` | string | — | 关键词搜索 |
| `orderBy` | string | — | 排序字段 |
| `sortOrder` | string | asc | 排序方向（asc/desc） |

**分页响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 100,
    "list": [...]
  }
}
```

### 4.4 开放接口（无需认证）

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/file/avatar/{filename}` — 头像图片
- `POST /api/file/minio/*` — MinIO 文件操作
- `/swagger-ui/**`
- `/doc.html`（Knife4j）
- `/v3/api-docs/**`

### 4.5 文件管理

#### 本地文件 `/api/file`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/avatar` | 上传用户头像（仅图片，最大 2MB） | 是 |
| GET | `/avatar/{filename}` | 获取头像图片 | 否 |

#### MinIO 文件管理 `/api/file/minio`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/upload` | 上传文件到 MinIO（prefix 默认为 uploads） | 是 |
| GET | `/presigned` | 获取预签名下载地址（expiry 默认为 60 分钟） | 是 |
| DELETE | `/exists` | 删除文件（objectName） | 是 |
| GET | `/exists` | 检查文件是否存在 | 是 |

**上传请求参数：**
- `file`（必填）：Multipart 文件
- `prefix`（选填）：存储路径前缀，默认 `uploads`

**响应数据：**
```json
{ "code": 200, "msg": "success", "data": "http://localhost:9000/kg-platform/uploads/xxx.png" }
```

#### 管理员修改用户密码 `POST /api/system/user/{id}/password`

**说明**：管理员无需旧密码即可修改任意用户的密码。

**请求体：**
```json
{
  "targetUserId": 2,
  "newPassword": "newPassword123"
}
```

**说明**：
- `targetUserId` 必须与路径参数 `{id}` 一致，否则返回 "参数不匹配"
- 新密码最小长度为 6 位（由前端表单验证）

**普通用户修改自己密码**仍使用 `POST /api/auth/password`（需填旧密码）。

---

## 五、数据库设计

### 5.1 ER 图概览

```
MySQL（元数据存储）
├── sys_user (1)───(N) sys_role
├── sys_user (1)───(N) kg_annotation_task
├── sys_user (1)───(N) kg_annotation_record
│
├── kg_graph (1)───(N) kg_node_instance
├── kg_graph (1)───(N) kg_edge_instance
├── kg_graph (1)───(N) kg_corpus
├── kg_graph (1)───(N) kg_extract_task
│
├── kg_model (1)───(N) kg_graph
├── kg_model (1)───(N) kg_extract_task
├── kg_model (1)───(N) kg_train_task
│
├── kg_corpus (1)───(N) kg_annotation_task
├── kg_corpus (1)───(N) kg_annotation_record
├── kg_corpus (1)───(N) kg_train_task

Neo4j（实体关系存储）
└── Entity 节点 ───(RELATION)───► Entity 节点
    （按 graphId 分区存储）

MinIO（文件存储）
└── kg-platform bucket
    ├── uploads/           — 通用文件
    ├── corpus/           — 语料文件
    ├── models/            — 模型文件
    └── exports/           — 导出结果
```

### 5.2 表结构

#### sys_user — 系统用户

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 用户 ID |
| username | VARCHAR(64) | 用户名（唯一） |
| password | VARCHAR(255) | 密码（BCrypt 加密） |
| nickname | VARCHAR(64) | 昵称 |
| email | VARCHAR(128) | 邮箱 |
| phone | VARCHAR(32) | 电话 |
| avatar | VARCHAR(512) | 头像 URL |
| role_id | BIGINT | 角色 ID |
| status | INT | 状态（1=启用，0=禁用） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 软删除标记 |

#### sys_role — 系统角色

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 角色 ID |
| role_name | VARCHAR(64) | 角色名称 |
| role_code | VARCHAR(64) | 角色编码（唯一） |
| description | VARCHAR(255) | 描述 |
| sort_order | INT | 排序号（越小越靠前） |
| status | INT | 状态 |

默认角色（按 sort_order 排序）：领域专家(1) / 图谱用户(2) / 数据标注员(3) / 图谱负责人(4) / 系统管理员(5)

#### kg_graph — 知识图谱

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 图谱 ID |
| name | VARCHAR(128) | 项目名称 |
| description | TEXT | 项目描述 |
| project_manager | VARCHAR(64) | 项目负责人 |
| model_name | VARCHAR(128) | 模型名称 |
| status | VARCHAR(32) | 状态（1=active, 0=inactive） |
| model_id | BIGINT | 关联模型 ID |
| storage_engine | VARCHAR(64) | 存储引擎（nebula/janus/tugraph/neo4j/hugegraph） |
| storage_engine_configured | TINYINT | 是否已配置存储引擎（0=否，1=是） |
| graph_space_created | TINYINT | 是否已创建图空间（0=否，1=是） |
| node_count | INT | 节点数量 |
| edge_count | INT | 边数量 |

#### kg_model — 图谱模型

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 模型 ID |
| name | VARCHAR(128) | 模型名称 |
| description | TEXT | 描述 |
| schema_col | JSON | 模型 schema 定义（字段名 `schema_col`，避免 MySQL 保留字冲突） |
| status | VARCHAR(32) | 状态 |

#### kg_corpus — 语料库

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 语料 ID |
| name | VARCHAR(128) | 名称 |
| file_path | VARCHAR(512) | 文件路径（MinIO URL 或本地路径） |
| file_type | VARCHAR(32) | 文件类型 |
| file_size | BIGINT | 文件大小（字节） |
| status | VARCHAR(32) | 状态 |
| graph_id | BIGINT | 关联图谱 ID |
| content | LONGTEXT | 语料文本内容（LLM 抽取专用，上传 .txt 文件时自动填充） |

#### kg_annotation_task — 标注任务

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 任务 ID |
| name | VARCHAR(128) | 任务名称 |
| corpus_id | BIGINT | 关联语料 ID |
| assignee_id | BIGINT | 标注人 ID |
| status | VARCHAR(32) | 状态（pending/assigned/in_progress/completed） |
| total_count | INT | 总数 |
| completed_count | INT | 已完成数 |
| annotation_type | VARCHAR(32) | 标注类型（entity/relation） |

#### kg_annotation_record — 标注记录

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 记录 ID |
| task_id | BIGINT | 任务 ID |
| corpus_id | BIGINT | 语料 ID |
| annotator_id | BIGINT | 标注人 ID |
| content | TEXT | 原始内容 |
| annotation | TEXT | 标注结果 |
| entity_types | JSON | 实体类型 |
| relation_types | JSON | 关系类型 |
| status | VARCHAR(32) | 状态（pending/completed/approved/rejected） |

#### kg_train_task — 模型训练任务

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 任务 ID |
| name | VARCHAR(128) | 任务名称 |
| model_type | VARCHAR(64) | 模型类型 |
| corpus_id | BIGINT | 训练语料 ID |
| config | JSON | 训练配置 |
| status | VARCHAR(32) | 状态（pending/running/completed/failed） |
| accuracy | FLOAT | 准确率 |
| precision_col | FLOAT | 精确率（字段名 `precision_col`，避免 MySQL 保留字冲突） |
| recall | FLOAT | 召回率 |
| f1_score | FLOAT | F1 分数 |
| train_time | BIGINT | 训练时长（秒） |

#### kg_extract_task — 知识抽取任务

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 任务 ID |
| name | VARCHAR(128) | 任务名称 |
| extract_type | VARCHAR(32) | 抽取类型（dl/llm） |
| graph_id | BIGINT | 目标图谱 ID |
| model_id | BIGINT | 使用模型 ID |
| source_type | VARCHAR(32) | 数据来源（corpus/text） |
| source_id | BIGINT | 来源 ID |
| status | VARCHAR(32) | 状态 |
| extracted_count | INT | 已抽取记录数 |

#### kg_node_instance — 图谱节点实例

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 节点 ID |
| graph_id | BIGINT | 图谱 ID |
| node_name | VARCHAR(128) | 节点名称 |
| node_type | VARCHAR(64) | 节点类型 |
| properties | JSON | 节点属性 |

#### kg_edge_instance — 图谱边实例

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 边 ID |
| graph_id | BIGINT | 图谱 ID |
| source_node_id | BIGINT | 源节点 ID |
| target_node_id | BIGINT | 目标节点 ID |
| relation_type | VARCHAR(64) | 关系类型 |
| properties | JSON | 边属性 |

#### kg_transform_task — 数据转换任务

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 任务 ID |
| name | VARCHAR(128) | 任务名称 |
| source_type | VARCHAR(32) | 源格式（csv/json/xml/sql） |
| target_format | VARCHAR(32) | 目标格式（kg_json/rdf/owl） |
| input_path | VARCHAR(512) | 输入路径 |
| output_path | VARCHAR(512) | 输出路径 |
| status | VARCHAR(32) | 状态 |
| record_count | INT | 已处理记录数 |

---

## 六、前端架构

### 6.1 路由结构

```
/login                           登录页
/                                Layout 布局
  /home                          系统首页
  /system
    /system/user                 用户管理
    /system/role                 角色管理
    /system/password             修改密码
  /kg
    /kg/graph                    知识图谱管理
    /kg/model                   图谱模型管理
  /data
    /data/transform              （半）结构化数据转化
    /data/extract-intro          知识抽取与转化
  /dl
    /dl/corpus                  语料管理
    /dl/annotation-auth          标注授权
    /dl/annotate                数据标注
    /dl/annotation-manage       标注管理
    /dl/train                   模型训练管理
    /dl/effect                  模型训练效果
    /dl/extract                 知识抽取
  /llm
    /llm/extract                LLM 知识抽取
  /explore
    /explore/graph              知识图谱探索
```

**路由守卫**：访问 `/system/role` 时仅 ADMIN 角色可访问，其他页面需登录。

### 6.2 API 层（Axios 封装）

`utils/request.js` 封装 Axios：请求拦截器自动注入 `Authorization: Bearer <token>`，响应拦截器统一处理 401 跳转登录。

### 6.3 状态管理（Pinia）

`stores/user.js` 中的 `useUserStore` 管理 token 和用户信息（包含 roleCode），持久化到 `localStorage`。

---

## 七、关键配置

### 7.1 application.yml 核心配置

```yaml
server.port: 8090

spring.datasource.url: jdbc:mysql://localhost:3306/kg_platform
spring.servlet.multipart.enabled: true
spring.servlet.multipart.max-file-size: 2MB
spring.servlet.multipart.max-request-size: 2MB
file.upload-path: uploads

# MinIO 对象存储配置
minio.endpoint: http://localhost:9000
minio.access-key: minioadmin
minio.secret-key: minioadmin
minio.bucket: kg-platform
minio.public-host: http://localhost:9000

# Neo4j 图数据库配置
neo4j.uri: bolt://localhost:7687
neo4j.username: neo4j
neo4j.password: neo4j123456

# DashScope 通义千问大模型配置
dashscope.api-key: ${DASHSCOPE_API_KEY:your-api-key-here}
dashscope.model: qwen-plus
dashscope.max-retries: 3

jwt.secret: KgPlatformSecretKey2026DoNotShareWithAnyone12345678901234567890
jwt.expiration: 86400000  # 24小时

mybatis-plus.global-config.db-config.logic-delete-field: deleted
mybatis-plus.global-config.db-config.logic-delete-value: 1
mybatis-plus.global-config.db-config.logic-not-delete-value: 0
mybatis-plus.configuration.map-underscore-to-camel-case: true
```

### 7.2 CORS 配置

允许来源：`http://localhost:*` 和 `http://127.0.0.1:*`，允许所有 Header 和 Method。

### 7.3 密码加密

使用 BCryptPasswordEncoder。默认管理员账号：**admin / admin123**。

### 7.4 API 文档

- Swagger UI: http://localhost:8090/swagger-ui.html
- Knife4j: http://localhost:8090/doc.html

---

## 八、MinIO 对象存储

### 8.1 概述

MinIO 是一个高性能的 S3 兼容对象存储服务，本平台使用 MinIO 存储语料文件、模型文件、导出结果等大文件，与 MySQL 分工明确：MySQL 存储元数据，MinIO 存储实际文件内容。

### 8.2 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `minio.endpoint` | MinIO 服务地址 | `http://localhost:9000` |
| `minio.access-key` | 访问密钥 | `minioadmin` |
| `minio.secret-key` | 访问密钥 | `minioadmin` |
| `minio.bucket` | 存储桶名称 | `kg-platform` |
| `minio.public-host` | 公共访问地址 | `http://localhost:9000` |

### 8.3 存储路径规范

| 前缀 | 用途 |
|------|------|
| `uploads/` | 通用上传文件 |
| `corpus/` | 语料文件 |
| `models/` | 模型文件 |
| `exports/` | 导出结果 |

---

## 九、Neo4j 图数据库

### 9.1 概述

Neo4j 是一个高性能的原生图数据库，本平台使用 Neo4j 作为知识图谱的实体关系存储引擎。与 MySQL 的 `kg_node_instance` / `kg_edge_instance` 表互补：MySQL 存储元数据索引，Neo4j 存储实体之间的真实关系网络。

### 9.2 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `neo4j.uri` | Neo4j 连接地址 | `bolt://localhost:7687` |
| `neo4j.username` | 用户名 | `neo4j` |
| `neo4j.password` | 密码 | `neo4j123456` |

### 9.3 节点模型

`Entity` 节点：对应知识图谱中的实体。

| 属性 | 说明 |
|------|------|
| name | 实体名称 |
| entityType | 实体类型（人物/机构/地点等） |
| graphId | 所属图谱 ID |
| properties | 实体属性（JSON） |

### 9.4 关系模型

`RELATION` 关系：连接两个 Entity 节点。

| 属性 | 说明 |
|------|------|
| relationType | 关系类型（任职/位于/属于等） |
| graphId | 所属图谱 ID |
| properties | 关系属性（JSON） |

---

## 十、大模型集成（通义千问）

### 10.1 概述

本平台使用阿里云 DashScope SDK 集成通义千问大模型，为知识抽取功能提供 LLM 支持。通过 `DashScopeService` 封装 LLM 调用，提取文本中的实体和关系并存储到 Neo4j 图数据库。

### 10.2 配置参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `dashscope.api-key` | DashScope API 密钥（建议通过环境变量 `DASHSCOPE_API_KEY` 注入） | `your-api-key-here` |
| `dashscope.model` | 使用的模型名称 | `qwen-plus` |
| `dashscope.max-retries` | API 调用失败最大重试次数 | `3` |

**申请 API Key：**
1. 访问阿里云 DashScope 控制台：https://dashscope.console.aliyun.com
2. 开通通义千问服务
3. 在「API-KEY 管理」中创建新的 API Key
4. 将 API Key 配置到环境变量或 `application.yml` 中

**推荐模型：**
- `qwen-plus`：综合能力最强，适用于复杂知识抽取
- `qwen-turbo`：速度快，成本低，适用于简单场景
- `qwen-max`：最高精度，适用于高质量抽取需求

### 10.3 工作流程

```
用户创建 LLM 抽取任务（POST /api/extract/llm）
    │
    ▼
启动抽取（POST /api/extract/{id}/start）
    │
    ▼
KgExtractTaskService.startExtractAsync()
    │
    ├── 从 kg_corpus 读取 content 字段
    │
    ▼
DashScopeService.extract(text, schema)
    │
    ├── 构建抽取 Prompt
    ├── 调用通义千问 API
    ├── 解析 JSON 响应
    └── 返回 LlmExtractResult（实体列表 + 关系列表）
    │
    ▼
将抽取结果写入 Neo4j
    │
    ├── MERGE 实体节点（Entity）
    └── MERGE 关系边（RELATION）
    │
    ▼
更新任务状态为 completed，更新 extractedCount
```

### 10.4 LLM 抽取结果 DTO

`LlmExtractResult` 定义了 LLM 返回的结构化抽取结果：

```json
{
  "entities": [
    {
      "name": "实体名称",
      "type": "实体类型",
      "attributes": {}
    }
  ],
  "relations": [
    {
      "source": "源实体名",
      "target": "目标实体名",
      "type": "关系类型",
      "attributes": {}
    }
  ]
}
```

### 10.5 接入新模型

如需替换为其他 LLM 提供商（如百度文心一言、硅基流动等），只需：

1. 在 `pom.xml` 中添加对应 SDK 依赖
2. 创建新的 Service 类（如 `BaiduLlmService`），实现与 `DashScopeService` 相同的接口
3. 修改 `KgExtractTaskService.doLlmExtract()` 中的调用

---

## 十一、部署说明

### 11.1 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- MinIO（对象存储）
- Neo4j（图数据库）
- Maven 3.8+

### 11.2 第三方服务启动

**MinIO 启动：**
```bash
docker run -d \
  --name minio \
  -p 9000:9000 -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"
# Web Console: http://localhost:9001 (minioadmin / minioadmin)
```

**Neo4j 启动：**
```bash
docker run -d \
  --name neo4j \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/neo4j123456 \
  neo4j:latest
# Browser: http://localhost:7474
```

### 11.3 启动方式

**方式一：一键启动**
```bash
start.bat
```

**方式二：分别启动**

后端：
```bash
cd d:\demo
mvnw.cmd spring-boot:run
# 访问 http://localhost:8090
```

前端：
```bash
cd d:\demo\frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

### 11.4 数据库初始化

启动时 `DataInitializer` 自动初始化：
- 5 个默认角色（按 sort_order 排序）：领域专家 / 图谱用户 / 数据标注员 / 图谱负责人 / 系统管理员
- 1 个管理员账号（admin / admin123）
- `kg_model` 表插入 2 条示例模型数据
- `kg_graph` 表插入 2 条示例图谱数据
- `kg_corpus` 表插入 2 条示例语料数据

---

## 十二、已知问题与修复记录

### 2026-05-20

#### 新增功能

| 功能 | 说明 |
|------|------|
| 管理员修改用户密码 | 管理员可通过「修改密码」页面选择任意用户并设置新密码，无需旧密码 |
| 知识图谱项目字段扩展 | 新增项目负责人、模型名称、存储引擎、是否已配置存储引擎、是否已创建图空间等字段 |
| MinIO 对象存储集成 | 新增 MinIO 文件管理服务，支持语料/模型/导出文件的大规模存储 |
| Neo4j 图数据库集成 | 新增 Neo4j 图数据库服务，为知识图谱实体关系存储提供基础设施 |
| 标注审核功能 | 新增标注记录审核接口（approve/reject） |
| 标注记录批量生成 | 新增批量生成标注记录接口 |
| 路径分析与关系类型查询 | 图谱探索新增路径分析和关系类型查询功能 |
| 通义千问 LLM 知识抽取集成 | 集成阿里云 DashScope SDK，支持通过通义千问模型从文本中抽取实体和关系并存入 Neo4j |
| 语料文本内容管理 | `kg_corpus` 新增 `content` 字段，支持设置语料文本内容供 LLM 抽取使用 |

#### Bug 修复

| 问题 | 根因 | 修复方案 |
|------|------|---------|
| `/api/home/dashboard` 返回 500 | `kg_model` / `kg_train_task` 表不存在 | 手动创建缺失表，`sql.init.mode` 改为 `never` |
| `SELECT ... AS schema` SQL 语法错误 | `schema` 是 MySQL 保留字 | 实体字段改为 `modelSchema`，映射到 `schema_col` |
| `SELECT ... AS precision` SQL 语法错误 | `precision` 是 MySQL 保留字 | 实体字段改为 `precisionVal`，映射到 `precision_col` |

> **MyBatis-Plus 保留字注意事项**：当 Java 字段名与 MySQL 保留字相同，MyBatis-Plus 会在 SELECT 中生成 `AS xxx`（使用 Java 字段名作为别名），导致 SQL 语法错误。解决方式：将 Java 字段名改为非保留字，用 `@TableField` 指定数据库列名。
