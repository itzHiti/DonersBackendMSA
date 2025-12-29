FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . /app
RUN gradle dependencies --refresh-dependencies
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
