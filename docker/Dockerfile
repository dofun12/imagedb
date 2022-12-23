FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar

RUN mkdir -p /out
COPY ${JAR_FILE} /out/app.jar
RUN (cd /out ; jar -xf app.jar)
RUN mkdir /app
RUN ls /out/
RUN cp -r /out/BOOT-INF/lib /app/lib
RUN cp -r /out/META-INF /app/META-INF
RUN cp -r /out/BOOT-INF/classes/* /app

RUN ls -R /app

ENTRYPOINT ["java","-cp","/app:/app/lib/*","org.lemanoman.imagedb.ImagedbApplication"]