FROM openjdk:8-alpine
#COPY mirror/target/mirror-1.3.jar /app/mirror.jar
WORKDIR /app/output
COPY batch-convert/target/batch-convert-1.3.jar /app/app.jar
VOLUME /app/output
ENTRYPOINT [ "java","-Xmx150m", "-jar","/app/app.jar"]


