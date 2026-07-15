FROM gradle:9.3.0-jdk25-alpine AS build
WORKDIR /src
COPY settings.gradle.kts build.gradle.kts ./
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle dependencies --no-daemon --quiet >/dev/null
COPY src ./src
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle clean test writeCoverage bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine AS app
WORKDIR /app
COPY --from=build /src/build/libs/spring-hexagonal-payments.jar /app/app.jar
USER 65532:65532
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

FROM eclipse-temurin:25-jre-alpine AS jre
FROM grafana/k6:2.1.0 AS loadtool

FROM postgres:18.4-alpine AS demo
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jre /opt/java/openjdk /opt/java/openjdk
COPY --from=loadtool /usr/bin/k6 /usr/local/bin/k6
COPY --from=build /src/build/libs/spring-hexagonal-payments.jar /app/app.jar
COPY --from=build /src/build/coverage-percent.txt /app/coverage-percent.txt
COPY benchmarks/k6.js /app/k6.js
COPY --chmod=755 tools/demo.sh /app/demo.sh
USER postgres
ENTRYPOINT ["/app/demo.sh"]
