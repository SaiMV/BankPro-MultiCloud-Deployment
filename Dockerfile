# BankPro Banking Application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/banking-app-1.0.0.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
