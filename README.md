# tju-unify

---

        本项目是一个主要面向tju的校园生活多功能工具应用。前后端分离，后端采用微服务架构，后续可以对工具和应用进行拓展(添加微服务模块/接入网关)。

当前核心功能模块(*标识该功能为网关直接接入)：

- 校园智能体助手

- 二手交易平台

- 新闻推送

- 校园电商平台*
  
  该服务也是一个微服务架构的后端
  
  仓库：https://gitee.com/dai-mingjing/frontend-comprehension.git

---

# 1 项目架构

## 1.1 目录结构

不同框架模块分开，总网关基于Spring实现

```
tju-unify/
├── docs/                  # 一些文档
├── tian-agent/            # 智能体源码
├── unify-conv/            # Spring部分后端 (包含整个应用网关)
│   ├── conv-api           # Feign 客户端模块
│   ├── conv-common        # 公共模块
│   ├── conv-news          # 新闻推送模块
│   ├── conv-gateway       # 应用网关
│   ├── conv-transaction   # 二手交易模块
│   ├── pom.xml            # 父工程依赖
│   └── sql                # 数据库建库sql文件
├── .gitignore
├── LICENSE
└── README.md
```

# 2 开发说明

## 2.1 Spring 部分后端

`unify-conv` ：基于Spring系列实现的均在这个模块下管理。主要管理：整个unify的网关(包括外接服务请求处理如电商平台)、新闻推送服务、二手平台服务、后续基于该框架的扩展服务  

### 2.1.1 结构

 `unify-conv` 为这部分的父工程，在这里做依赖版本管理等。  

整体按服务模块组织，feign客户端由各个服务自己管理  

现主要有以下模块：  

- `conv-common` 公共模块，其他服务依赖该模块，有一些公共的依赖已经引入  

- `conv-gateway` 网关，用于路由转发和鉴权  

- `conv-news` 新闻推送服务  

- `conv-transaction`  二手平台服务  

- 后续追加服务......  

### 2.1.2 一些说明

关于电商平台，目前直接调用饿了吧那边的接口  

**特别地在用户这一块**  

整个unify的用户管理直接与饿了吧那边同步，也就是unify的用户就是饿了吧的用户，共用一个数据库、一套鉴权逻辑。后续如果有时间的话就把两个分离一下，没有就算了  

**现在网关的鉴权与转发逻辑**  

### 2.1.3 加入新服务流程

**情况一：添加新服务模块实现**

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

4. 网关接入
   
   在网关的配置文件 `application.yml` 中：
   
   - `spring.cloud.gateway.routes` 添加路由规则
   
   - 对于不需要鉴权的接口，加入 `unify.auth.excludePaths`

5. 数据库暂时用bobchasm.cn服务器上部署的mysql，要新建数据库直接建就行，最好存下建库sql在路径`sql`下  
      然后把mybatis变成mybatis-plus了，基础增删改查可以不用写，然后我看新闻推送那里写了分页，可以改成使用mybatis-plus的更方便的分页  

**情况二：接入外部 api**

直接按情况一的4做

如果想要进行一些处理，可以新建一个模块作为一个包装微服务

### 2.1.4 服务间调用

引入`api` 模块的依赖

- 项目内部服务的接口
  
  在 `/conv-api/src/main/java/com/tju/unify/conv/api/client/inner`中添加微服务的Feign客户端，已经有了就往相应的客户端加接口

- 网关接入的接口
  
  可以在`unify-api` 模块的`src/main/java/com/tju/unify/conv/api/client/outer` 中写调用的逻辑，当然也可以直接在模块里写

## 2.2 智能体



---

# 3 一些说明

欢迎贡献代码、报告问题或提出建议！

### 3.1 提交问题

- 描述问题现象
- 提供复现步骤
- 附上错误日志
- 说明环境信息

### 3.2 提交代码

1. Fork本仓库
2. 创建功能分支
3. 提交代码并编写测试
4. 提交Pull Request

### 3.3 联系方式

如有问题或建议，请通过以下方式联系：

- **邮箱**：  
  zengyicydd@tju.edu.cn  <br>
  gaocan@tju.edu.cn  <br>
  daimingjing142857@tju.edu.cn  <br>
  yxy641121@gmail.com  <br>
  jsyy@tju.edu.cn

- **Gitee Issues**：提交问题到项目仓库
