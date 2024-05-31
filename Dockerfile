# ---- Build Stage ----
FROM eclipse-temurin:8-jdk as build

# This is required by embedded mongo used in testing
RUN wget http://security.ubuntu.com/ubuntu/pool/main/o/openssl/libssl1.1_1.1.1f-1ubuntu2.22_amd64.deb && \
    dpkg -i libssl1.1_1.1.1f-1ubuntu2.22_amd64.deb

WORKDIR /app

COPY gradlew .
COPY gradle gradle

COPY build.gradle settings.gradle ./
# Set the Java heap size for the Gradle build
ENV GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

#This stage will download all dependencies as a side effect
RUN ./gradlew dependencies --no-daemon

# Copy your other files
COPY src src/

# Build the project
RUN ./gradlew clean build --no-daemon

# ---- Run Stage ----
FROM adoptopenjdk/openjdk11-openj9:alpine-jre
WORKDIR /app

ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
EXPOSE 8080 5005

RUN apk --no-cache add bash openssl fontconfig ttf-dejavu

COPY --from=build /app/build/libs/*.jar /app/crawler.jar

#ENV DEBUGGER="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
#ENV PROFILER=" -Dcom.sun.management.jmxremote \
#              -Djava.rmi.server.hostname=localhost \
#              -Dcom.sun.management.jmxremote.port=1098 \
#              -Dcom.sun.management.jmxremote.rmi.port=1098 \
#              -Dcom.sun.management.jmxremote.local.only=false \
#              -Dcom.sun.management.jmxremote.authenticate=false \
#              -Dcom.sun.management.jmxremote.ssl=false"

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS $DEBUGGER $PROFILER -Djava.security.egd=file:/dev/./urandom -jar /app/crawler.jar" ]