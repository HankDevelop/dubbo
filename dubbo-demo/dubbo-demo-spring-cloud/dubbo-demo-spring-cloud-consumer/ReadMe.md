#启动参数
```run args
-Xmx750m
-Xms128m
-Dserver.port=8080
-Ddubbo.protocol.port=20880
-Dspring.application.name=dubbo-demo-spring-cloud-consumer
```

#测试地址：
```bash
curl http://localhost:8080/hello
curl http://localhost:8080/sayHello?name=test
curl http://localhost:8080/sayHelloAsync?name=test
```