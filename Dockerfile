# ---- Build Stage ----
FROM eclipse-temurin:17-jdk as build

# This is required by embedded mongo used in testing
RUN wget http://archive.ubuntu.com/ubuntu/pool/main/o/openssl/libssl1.1_1.1.1f-1ubuntu2.24_amd64.deb && \
    dpkg -i libssl1.1_1.1.1f-1ubuntu2.24_amd64.deb

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
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

EXPOSE 8080

RUN apk --no-cache add bash openssl fontconfig ttf-dejavu

COPY --from=build /app/build/libs/crawler.jar /app/crawler.jar

ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /app/crawler.jar" ]