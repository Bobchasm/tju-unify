# tju-unify 后端 (Spring Cloud 部分)

基于Spring系列实现的均在这个模块下管理。主要管理：整个unify的网关(包括外接服务请求处理如电商平台)、新闻推送服务、二手平台服务、后续基于该框架的扩展服务

---

## 1 结构

 `unify-conv` 为这部分的父工程，在这里做依赖版本管理等。

整体按服务模块组织，feign客户端由各个服务自己管理

现主要有以下模块：

- `conv-common` 公共模块，其他服务依赖该模块，有一些公共的依赖已经引入

- `conv-gateway` 网关，用于路由转发和鉴权

- `conv-news` 新闻推送服务

- `conv-transaction`  二手平台服务

- 后续追加服务......

## 2 一些说明

关于电商平台，目前直接调用饿了吧那边的接口

**特别地在用户这一块**

整个unify的用户管理直接与饿了吧那边同步，也就是unify的用户就是饿了吧的用户，共用一个数据库、一套鉴权逻辑。后续如果有时间的话就把两个分离一下，没有就算了

**现在网关的鉴权与转发逻辑**

```
                                        开始
                                          │
                                          ▼
                              ┌───────────────────────┐
                              │  unify 前端发起请求     │
                              └───────────────────────┘
                                          │
                                          ▼
                              ┌───────────────────────┐
                              │  unify 网关接收请求     │
                              └───────────────────────┘
                                          │
                                          ▼
                              ┌───────────────────────┐
                              │    判断请求url是否以    │
                              │   /api/ 或 /ws/ 开头   │
                              └───────────────────────┘
                                          │
                          ┌───────────────┴───────────────┐
                          │ 是                            │ 否
                          ▼                               ▼
              ┌─────────────────────┐         ┌─────────────────────┐
              │  unify 网关不鉴权    │          │  unify 网关鉴权      │
              │  直接按原样转发       │         └─────────────────────┘
              └─────────────────────┘                     │
                          │                               │
                          │                               ├──────▶ 鉴权失败 ──▶ 返回 401
                          │                               │
                          │                               ▼
                          │                         ┌─────────────────────┐
                          │                         │  添加 username       │
                          │                         │  header 后转发       │
                          │                         └─────────────────────┘
                          │                               │
                          ▼                               ▼ 
              ┌───────────────────────┐       ┌───────────────────────┐
              │    饿了吧网关自己处理    │       │    unify 网关转发给    │ 
              └───────────────────────┘       │     新闻/二手...服务    │
                                              └───────────────────────┘
```

服务中获取当前用户信息:

```java
User currentUser = userClient.getUserByName(UserContext.getUsername()).getData();
```

## 3 加入新服务流程

### 3.1 情况一：添加新服务模块实现

1. 创建新 sprintboot 模块，模块名统一前缀 `conv-`
   
   包路径前缀最好规范成 `com.tju.unify.conv.` + 模块名
   
   服务名前缀最好是规范成 `unify-`
   
   端口：路由7070

   配置文件就直接参考已有服务(nacos、数据库之类的)

2. 依赖部分：
   
   必须引入公共模块，因为需要统一响应等规范；和服务发现
   
   ```xml
   <dependency>
       <groupId>com.tju.unify</groupId>
       <artifactId>conv-common</artifactId>
       <version>1.0.0-SNAPSHOT</version>
   </dependency>
   <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
   </dependency>
   ```
   
   如果需要获取当前用户信息或者调用其他服务，需要引入feign客户端：
   
   ```xml
   <dependency>
       <groupId>com.tju.unify</groupId>
       <artifactId>conv-api</artifactId>
       <version>1.0.0-SNAPSHOT</version>
   </dependency>
   ```
   
   接口文档相关的看着办：
   
   ```xml
   <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>${springdoc-openapi.version}</version>
   </dependency>
   ```

3. 启动类注解，如：
   
   ```java
   @SpringBootApplication
   @EnableDiscoveryClient // 启用服务发现
   @MapperScan("com.tju.unify.conv.news.mapper") //mapper包路径
   public class ConvNewsApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(ConvNewsApplication.class,args);
       }
   }
   ```

4. 数据库暂时用bobchasm.cn服务器上部署的mysql，要新建数据库直接建就行，最好存下建库sql在路径`sql`下
   
   然后把mybatis变成mybatis-plus了，基础增删改查可以不用写，然后我看新闻推送那里写了分页，可以改成使用mybatis-plus的更方便的分页

### 3.2 接入外部 api

可以新建一个模块专门处理，然后引入`api` 模块的依赖，在 `unify-api` 模块的 `src/main/java/com/tju/unify/conv/api/client/outer` 中写调用的逻辑，当然也可以直接在模块里写
