FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
EXPOSE 8080 5005
ADD *.jar /home/app.jar
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /home/app.jar" ]