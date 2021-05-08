FROM openjdk:8-alpine
WORKDIR /app
COPY mirror/target/mirror-1.3.jar /app/mirror.jar
VOLUME /app/wmts_TOPO
CMD ["java","-jar","mirror.jar"]
