FROM gradle:6.8.3-jdk11 AS build
ARG BOM_VERSION MIGRATION_VERSION GITHUB_USER GITHUB_TOKEN RELEASE_MODE APP_VERSION SCRIPTS_VERSION
WORKDIR /service_build
COPY . /service_build/
RUN if [ ${RELEASE_MODE} = true ]; then \
    gradle build --exclude-task test \
        -PreleaseMode=true \
        -PgithubUserName=${GITHUB_USER} \
        -PgithubToken=${GITHUB_TOKEN} \
        -Pscripts.version=${SCRIPTS_VERSION} \
        -Pmigrations.version=${MIGRATION_VERSION} \
        -Pbom.version=${BOM_VERSION}; \
    else gradle build --exclude-task test; fi

FROM --platform=linux/amd64 amazoncorretto:11.0.19 AS amd64
LABEL version=${APP_VERSION} description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>, Hleb Kanonik <hleb_kanonik@epam.com>"
ARG APP_VERSION=${APP_VERSION}
ENV APP_VERSION=${APP_VERSION} JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -Djava.security.egd=file:/dev/./urandom"
WORKDIR /reportportal
COPY --from=build /service_build/build/libs/service-api-${APP_VERSION}-exec.jar .
RUN echo 'exec java ${JAVA_OPTS} -jar /reportportal/service-api-${APP_VERSION}-exec.jar' > ./start.sh && chmod +x ./start.sh
VOLUME ["/tmp"]
EXPOSE 8080
ENTRYPOINT ./start.sh

FROM --platform=linux/arm64 mazoncorretto:11.0.19 AS arm64
LABEL version=${APP_VERSION} description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>, Hleb Kanonik <hleb_kanonik@epam.com>"
ARG APP_VERSION=${APP_VERSION}
ENV APP_VERSION=${APP_VERSION} JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -Djava.security.egd=file:/dev/./urandom"
WORKDIR /reportportal
COPY --from=build /service_build/build/libs/service-api-${APP_VERSION}-exec.jar .
RUN yum install -y google-noto-fonts-common && \
    yum clean all && rm -rf /var/cache/yum && \
    echo 'exec java ${JAVA_OPTS} -jar /reportportal/service-api-${APP_VERSION}-exec.jar' > ./start.sh && chmod +x ./start.sh
VOLUME ["/tmp"]
EXPOSE 8080
ENTRYPOINT ./start.sh
