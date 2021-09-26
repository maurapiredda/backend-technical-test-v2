FROM amazoncorretto:11-alpine-jdk
MAINTAINER maura.piredda
COPY target/backend-technical-test-2.0.0-SNAPSHOT.jar backend-technical-test-2.0.0.jar
ENTRYPOINT ["java","-jar","/backend-technical-test-2.0.0.jar"]