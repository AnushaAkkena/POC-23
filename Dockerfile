
# Run the pre-built Spring Boot jar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy jar built by Maven in Jenkins
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
