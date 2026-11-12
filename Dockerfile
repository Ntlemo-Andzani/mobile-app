# syntax=docker/dockerfile:1

# ---------- Stage 1: build the WAR with Maven ----------
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app
# The Maven project is nested two folders deep in this repo.
COPY realtime-chat/realtime-chat/pom.xml .
COPY realtime-chat/realtime-chat/src ./src
RUN mvn -q -B clean package

# ---------- Stage 2: run it on Tomcat 9 ----------
FROM tomcat:9.0-jdk11-temurin
# Remove Tomcat's default sample apps.
RUN rm -rf /usr/local/tomcat/webapps/*
# Deploy our app as ROOT so it is served at "/" (no /realtime-chat prefix).
COPY --from=build /app/target/realtime-chat.war /usr/local/tomcat/webapps/ROOT.war
# Cloud hosts (Render, Railway, ...) inject the public port via $PORT.
ENV PORT=8080
EXPOSE 8080
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
CMD ["/entrypoint.sh"]
