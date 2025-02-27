FROM openjdk:21
WORKDIR /app
EXPOSE 8080
ADD target/digital-wallet-authentication.jar app.jar
ENTRYPOINT ["java", "-jar","app.jar"]