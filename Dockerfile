FROM --platform=$BUILDPLATFORM gradle:8.4.0-jdk21 AS build
ARG RELEASE_MODE
ARG APP_VERSION
WORKDIR /usr/app
COPY . /usr/app
RUN if [ "${RELEASE_MODE}" = true ]; then \
    gradle build --exclude-task test \
        -PreleaseMode=true \
        -Dorg.gradle.project.version=${APP_VERSION}; \
    else gradle build --exclude-task test -Dorg.gradle.project.version=${APP_VERSION}; fi

# For ARM build use flag: `--platform linux/arm64`
FROM --platform=$BUILDPLATFORM amazoncorretto:21.0.1
LABEL version=${APP_VERSION} description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>, Hleb Kanonik <hleb_kanonik@epam.com>"
ARG APP_VERSION=${APP_VERSION}
ENV APP_DIR=/usr/app
ENV JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -Djava.security.egd=file:/dev/./urandom "
WORKDIR $APP_DIR
COPY --from=build $APP_DIR/build/libs/service-api-*exec.jar .
VOLUME ["/tmp"]
EXPOSE 8080
ENTRYPOINT exec java ${JAVA_OPTS} -jar ${APP_DIR}/service-api-*exec.jar
