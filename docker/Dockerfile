FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
ENV PROFILE=" -Dcom.sun.management.jmxremote \
              -Djava.rmi.server.hostname=0.0.0.0 \
              -Dcom.sun.management.jmxremote.port=1098 \
              -Dcom.sun.management.jmxremote.rmi.port=1098 \
              -Dcom.sun.management.jmxremote.local.only=false \
              -Dcom.sun.management.jmxremote.authenticate=false \
              -Dcom.sun.management.jmxremote.ssl=false"
EXPOSE 8080 5005 1098
ADD *.jar /home/app.jar
ENTRYPOINT [ "sh", "-c", "java $DEBUG $PROFILE -Djava.security.egd=file:/dev/./urandom -jar /home/app.jar" ]