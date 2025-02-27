FROM openjdk:21
WORKDIR /app
EXPOSE 8081
ADD target/digital-wallet-walletService.jar app.jar
ENTRYPOINT ["java", "-jar","app.jar"]