FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
ENV PROFILE="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1098"
EXPOSE 8080 5005 1098
ADD *.jar /home/app.jar
ENTRYPOINT [ "sh", "-c", "java $DEBUG $PROFILE -Djava.security.egd=file:/dev/./urandom -jar /home/app.jar" ]