FROM adoptopenjdk/openjdk11-openj9:alpine-jre
VOLUME /tmp
ENV DEBUGGER="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
ENV PROFILER=" -Dcom.sun.management.jmxremote \
              -Djava.rmi.server.hostname=localhost \
              -Dcom.sun.management.jmxremote.port=1098 \
              -Dcom.sun.management.jmxremote.rmi.port=1098 \
              -Dcom.sun.management.jmxremote.local.only=false \
              -Dcom.sun.management.jmxremote.authenticate=false \
              -Dcom.sun.management.jmxremote.ssl=false"



EXPOSE 8080 5005 1098
ADD *.jar /home/crawler.jar
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS $DEBUGGER $PROFILER -Djava.security.egd=file:/dev/./urandom -jar /home/crawler.jar" ]