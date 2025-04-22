FROM eclipse-temurin:17-jdk as jar_builder

COPY . .

RUN ./gradlew clean bootJar

FROM eclipse-temurin:17-jre

COPY --from=jar_builder /build/libs/*jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]