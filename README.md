# sca_example
Spring Cloud Alibaba example

## 工程介绍

### first-gateway
网关服务

### first-web
接入服务  
1、本服务会调用 first-service

### first-service
逻辑服务  
1、操作数据库 test1.address 表  
2、调用 second-service 操作数据库 test2.weather 表  
3、生产、消费 RocketMQ 消息

### second-service
逻辑服务  
1、操作数据库 test2.weather 表  
2、本服务会生产、消费 RocketMQ 消息

## 测试

### 测试 Sentinel
- 接口1：短时间多次调用
  - http://localhost:18080/first-web/test/getNacosConfig
- 接口2：短时间多次调用
  - http://localhost:18080/first-service/hello/getNacosConfig

### 测试 RocketMQ
- 启动 2 个不同端口的 first-service 查看 2 个服务控制台输出生产、消费信息

### 测试 Seata
- 接口1：更改日期调用 2 次，一次会成功提交，一次会失败回滚
  - http://localhost:18080/first-web/test/updateAddressWeather?address=Wz&day=2024-05-01&weather=晴天
