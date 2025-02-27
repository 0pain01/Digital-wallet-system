FROM openjdk:21
WORKDIR /app
EXPOSE 8082
ADD target/digital-wallet-transaction.jar app.jar
ENTRYPOINT ["java", "-jar","app.jar"]