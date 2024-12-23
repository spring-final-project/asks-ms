FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline

COPY ./src/main ./src/main

RUN mvn clean package -DskipTests


FROM openjdk:21-jdk-slim

# Download and install X-Ray daemon
RUN apt-get update && apt-get install -y wget netcat-openbsd
RUN wget https://s3.us-east-2.amazonaws.com/aws-xray-assets.us-east-2/xray-daemon/aws-xray-daemon-3.x.deb && \
    dpkg -i aws-xray-daemon-3.x.deb && \
    rm aws-xray-daemon-3.x.deb

WORKDIR /app

COPY --from=build /app/target/asks-ms-0.0.1.jar /app/asks-ms-0.0.1.jar

EXPOSE 8080

# Wait for X-Ray daemon to be ready and then start the application
CMD /usr/bin/xray -o -n sa-east-1 & \
    echo "Starting xray daemon..." && \
    timeout 15 sh -c 'until nc -z localhost 2000; do sleep 1; done' && \
    echo "X-Ray Daemon is running." && \
    java -jar /app/asks-ms-0.0.1.jar