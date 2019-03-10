FROM openjdk:8-jdk-alpine
VOLUME /tmp
EXPOSE 8080
ADD *.jar /home/app.jar
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /home/app.jar" ]