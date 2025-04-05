# 使用官方的 OpenJDK 镜像作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 Maven 构建的 JAR 文件到容器中
COPY target/gateway-service-1.0-SNAPSHOT.jar app.jar

# 暴露应用程序的端口
EXPOSE 8080

# 添加等待脚本
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

# 运行等待脚本和 Java 应用
CMD /wait && java -jar app.jar