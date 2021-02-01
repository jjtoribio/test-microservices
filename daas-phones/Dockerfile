FROM openjdk:8-jdk-alpine
RUN addgroup -S java && adduser -S java -G java
USER java:java
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app.jar"]


