# Build stage
FROM maven:3.8.1-openjdk-17-slim as build

# Copy source for build
COPY src /nanopub-monitor/src
COPY pom.xml /nanopub-monitor

# Build with maven
RUN mvn -f /nanopub-monitor/pom.xml clean package

# Pull base image
FROM tomcat:9.0.56-jre17-temurin

# Remove default webapps:
RUN rm -fr /usr/local/tomcat/webapps/*

# Copy target from build stage
COPY --from=build /nanopub-monitor/target/nanopub-monitor /usr/local/tomcat/nanopub-monitor/target/nanopub-monitor

COPY scripts /usr/local/tomcat/nanopub-monitor/scripts
RUN ln -s /usr/local/tomcat/nanopub-monitor/target/nanopub-monitor /usr/local/tomcat/webapps/ROOT

# Port:
EXPOSE 8080

CMD ["catalina.sh", "run"]