# 知识图谱管理平台 (KG Platform)

全栈知识图谱管理平台，基于 Spring Boot + Vue3。

## 技术栈

**后端**
- Spring Boot 3.3 + Spring Security
- MyBatis-Plus + MySQL
- JWT 认证
- Knife4j API 文档

**前端**
- Vue3 + Vite
- Element Plus
- Pinia 状态管理
- Vue Router

## 快速启动

### 1. 准备 MySQL

创建数据库：

```sql
CREATE DATABASE kg_platform DEFAULT CHARACTER SET utf8mb4;
```

导入初始数据（在 `src/main/resources/schema.sql` 中）

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kg_platform?...
    username: root
    password: your_password
```

### 2. 一键启动

```batch
start.bat
```

或分别启动：

```bash
# 后端
mvnw spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 | http://localhost:8080 |
| API文档 | http://localhost:8080/doc.html |
| 默认账号 | admin / admin123 |

## 功能模块

1. 系统首页 - 仪表盘统计
2. 平台管理 - 用户管理、角色管理、修改密码
3. 图谱项目管理 - 知识图谱管理、图谱模型管理
4. 知识抽取与转化 - 结构化数据转化
5. 深度学习知识抽取 - 语料管理、标注授权、数据标注、模型训练、效果评估、知识抽取
6. 基于LLM知识抽取
7. 图谱实例管理 - 知识图谱探索

## 项目结构

```
d:\demo\
├── src/main/java/com/example/kgplatform/
│   ├── config/       # 配置类
│   ├── controller/   # 控制器
│   ├── service/      # 业务层
│   ├── mapper/       # 数据访问层
│   ├── entity/       # 实体类
│   ├── dto/          # 数据传输对象
│   ├── security/     # 安全认证
│   └── common/       # 通用类
├── frontend/         # Vue3前端
├── src/main/resources/
│   ├── application.yml
│   └── schema.sql    # 数据库脚本
└── start.bat         # 启动脚本
```
