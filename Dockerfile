# 1. Base image
FROM openjdk:17-jdk-slim

# 2. Workdir
WORKDIR /app

# 3. Build tətbiq faylını kopyala
COPY target/UrlShortener-0.0.1-SNAPSHOT.jar app.jar

# 4. Port aç
EXPOSE 8080

# 5. Tətbiqi işə sal
ENTRYPOINT ["java","-jar","app.jar"]
