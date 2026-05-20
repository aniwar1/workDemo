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
| **数据库** | MySQL 8.0 | 8.0.44 |
| **安全** | Spring Security + JWT | — |
| **API 文档** | Knife4j / SpringDoc OpenAPI | — |
| **工具库** | Hutool | 5.8.26 |
| **前端框架** | Vue 3 | 3.4.21 |
| **构建工具** | Vite | 5.2.8 |
| **UI 框架** | Element Plus | 2.7.2 |
| **状态管理** | Pinia | 2.1.7 |
| **HTTP 客户端** | Axios | 1.6.8 |
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
│   │   ├── DataInitializer.java         # 启动数据初始化
│   │   ├── MyBatisMetaObjectHandler.java
│   │   └── Knife4jConfig.java            # API 文档配置
│   ├── controller/                        # 12 个控制器
│   ├── service/                          # 服务层
│   ├── mapper/                           # MyBatis-Plus Mapper
│   ├── entity/                           # 实体类（12 个）
│   ├── dto/                              # 数据传输对象
│   └── security/
│       ├── JwtAuthFilter.java            # JWT 认证过滤器
│       └── UserDetailsServiceImpl.java
├── src/main/resources/
│   ├── application.yml                   # 应用配置
│   └── schema.sql                        # 数据库建表脚本
├── frontend/                             # Vue 3 前端
│   ├── src/
│   │   ├── main.js                       # 前端入口
│   │   ├── App.vue
│   │   ├── api/                          # API 接口层
│   │   ├── router/index.js               # 路由配置
│   │   ├── stores/user.js                # Pinia 用户状态
│   │   ├── utils/request.js              # Axios 封装
│   │   ├── layout/index.vue              # 整体布局
│   │   └── views/                        # 页面组件
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
    Controller（12 个）
          │
          ▼
    Service → Mapper → MySQL
          │
          ▼
    R<T> 统一响应 { code, msg, data }
```

### 3.2 认证流程

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
| POST | `/password` | 修改密码 | 是 |
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
  "roleId": 1
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

#### 模型训练管理 `/api/train`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 创建训练任务 | 是 |
| POST | `/{id}/start` | 启动训练 | 是 |
| GET | `/{id}/metrics` | 获取训练指标 | 是 |
| DELETE | `/{id}` | 删除任务 | 是 |

#### 标注管理 `/api/annotation`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/task/list` | 标注任务列表 | 是 |
| POST | `/task` | 创建标注任务 | 是 |
| POST | `/task/{id}/assign` | 分配任务 | 是 |
| GET | `/record/list` | 标注记录列表 | 是 |
| POST | `/record` | 提交标注 | 是 |
| GET | `/task/{id}/next` | 获取待标注数据 | 是 |
| DELETE | `/task/{id}` | 删除标注任务 | 是 |

#### 语料管理 `/api/corpus`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 新增语料 | 是 |
| PUT | `/{id}` | 修改语料 | 是 |
| DELETE | `/{id}` | 删除语料 | 是 |

#### 知识抽取 `/api/extract`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 抽取任务列表 | 是 |
| POST | `/dl` | 创建深度学习抽取任务 | 是 |
| POST | `/llm` | 创建 LLM 抽取任务 | 是 |
| POST | `/{id}/start` | 启动抽取 | 是 |
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
| GET | `/graph/{graphId}` | 查询图谱节点和边 | 是 |
| GET | `/node/search` | 搜索节点 | 是 |
| GET | `/node/{id}` | 节点详情 | 是 |
| GET | `/graphs` | 图谱列表 | 是 |

#### 用户管理 `/api/system/user`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 新增用户（默认密码 123456） | 是 |
| PUT | `/{id}` | 修改用户 | 是 |
| DELETE | `/{id}` | 删除用户 | 是 |
| POST | `/{id}/reset-password` | 重置密码为 123456 | 是 |

#### 角色管理 `/api/system/role`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 分页查询 | 是 |
| GET | `/{id}` | 获取详情 | 是 |
| POST | `/` | 新增角色 | 是 |
| PUT | `/{id}` | 修改角色 | 是 |
| DELETE | `/{id}` | 删除角色 | 是 |
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
- `/swagger-ui/**`
- `/doc.html`（Knife4j）

---

## 五、数据库设计

### 5.1 ER 图概览

```
sys_user (1)───(N) sys_role
sys_user (1)───(N) kg_annotation_task
sys_user (1)───(N) kg_annotation_record

kg_graph (1)───(N) kg_node_instance
kg_graph (1)───(N) kg_edge_instance
kg_graph (1)───(N) kg_corpus
kg_graph (1)───(N) kg_extract_task

kg_model (1)───(N) kg_graph
kg_model (1)───(N) kg_extract_task
kg_model (1)───(N) kg_train_task

kg_corpus (1)───(N) kg_annotation_task
kg_corpus (1)───(N) kg_annotation_record
kg_corpus (1)───(N) kg_train_task
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
| status | INT | 状态 |

默认角色：ADMIN、OPERATOR、ANNOTATOR

#### kg_graph — 知识图谱

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 图谱 ID |
| name | VARCHAR(128) | 图谱名称 |
| description | TEXT | 描述 |
| status | VARCHAR(32) | 状态 |
| model_id | BIGINT | 关联模型 ID |
| node_count | INT | 节点数量 |
| edge_count | INT | 边数量 |

#### kg_model — 图谱模型

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 模型 ID |
| name | VARCHAR(128) | 模型名称 |
| description | TEXT | 描述 |
| model_schema | JSON | 模型 schema 定义 |
| status | VARCHAR(32) | 状态 |

#### kg_corpus — 语料库

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 语料 ID |
| name | VARCHAR(128) | 名称 |
| file_path | VARCHAR(512) | 文件路径 |
| file_type | VARCHAR(32) | 文件类型 |
| file_size | BIGINT | 文件大小（字节） |
| status | VARCHAR(32) | 状态 |
| graph_id | BIGINT | 关联图谱 ID |

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
| status | VARCHAR(32) | 状态 |

#### kg_train_task — 模型训练任务

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 任务 ID |
| name | VARCHAR(128) | 任务名称 |
| model_type | VARCHAR(64) | 模型类型 |
| corpus_id | BIGINT | 训练语料 ID |
| config | JSON | 训练配置 |
| status | VARCHAR(32) | 状态 |
| accuracy | FLOAT | 准确率 |
| precision_val | FLOAT | 精确率 |
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
    /kg/model                    图谱模型管理
  /data
    /data/transform              （半）结构化数据转化
    /data/extract-intro           知识抽取与转化
  /dl
    /dl/corpus                   语料管理
    /dl/annotation-auth           标注授权
    /dl/annotate                 数据标注
    /dl/annotation-manage         标注管理
    /dl/train                    模型训练管理
    /dl/effect                   模型训练效果
    /dl/extract                  知识抽取
  /llm
    /llm/extract                 LLM 知识抽取
  /explore
    /explore/graph               知识图谱探索
```

### 6.2 API 层（Axios 封装）

请求拦截器自动注入 `Authorization: Bearer <token>`，响应拦截器统一处理错误码（401 跳转登录）。

### 6.3 状态管理（Pinia）

`useUserStore` 管理 token 和用户信息，持久化到 `localStorage`。

---

## 七、关键配置

### 7.1 application.yml 核心配置

```yaml
server.port: 8090

spring.datasource.url: jdbc:mysql://localhost:3306/kg_platform
jwt.secret: KgPlatformSecretKey2026...
jwt.expiration: 86400000  # 24小时

mybatis-plus.global-config.db-config.logic-delete-field: deleted
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

## 八、部署说明

### 8.1 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 8.2 启动方式

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

### 8.3 初始化数据

启动时 `DataInitializer` 自动初始化：
- 3 个默认角色（ADMIN / OPERATOR / ANNOTATOR）
- 1 个管理员账号（admin / admin123）
- `kg_model` 表插入 2 条示例模型数据
- `kg_graph` 表插入 2 条示例图谱数据
- `kg_corpus` 表插入 2 条示例语料数据

---

## 九、已知问题与修复记录

### 2026-05-20

| 问题 | 根因 | 修复方案 |
|------|------|---------|
| `/api/home/dashboard` 返回 500 | `kg_model` / `kg_train_task` 表不存在 | 手动创建缺失表，`sql.init.mode` 改为 `never` |
| `SELECT ... AS schema` SQL 语法错误 | `schema` 是 MySQL 保留字 | 实体字段改为 `modelSchema`，映射到 `schema_col` |
| `SELECT ... AS precision` SQL 语法错误 | `precision` 是 MySQL 保留字 | 实体字段改为 `precisionVal`，映射到 `precision_col` |

> **MyBatis-Plus 保留字注意事项**：当 Java 字段名与 MySQL 保留字相同，MyBatis-Plus 会在 SELECT 中生成 `AS xxx`（使用 Java 字段名作为别名），导致 SQL 语法错误。解决方式：将 Java 字段名改为非保留字，用 `@TableField` 指定数据库列名。
