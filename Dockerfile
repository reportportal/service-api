FROM openjdk:8-jre-alpine

LABEL maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"
LABEL version="5.0.0-RC-2"
LABEL description="EPAM Report portal. Main API Service"

ENV APP_FILE service-api-5.0.0-RC-2
ENV APP_DOWNLOAD_URL https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/5.0.0-RC-2/$APP_FILE.jar
ENV JAVA_OPTS="-Xmx1g -Djava.security.egd=file:/dev/./urandom"
ENV JAVA_APP=/app/app.jar

RUN apk add --no-cache ca-certificates fontconfig freetype font-noto

RUN sh -c "echo $'#!/bin/sh \n\
exec java \$JAVA_OPTS -jar \$JAVA_APP' > /start.sh && chmod +x /start.sh"

VOLUME /tmp

RUN mkdir /app
RUN wget -O $JAVA_APP $APP_DOWNLOAD_URL

EXPOSE 8080
ENTRYPOINT /start.sh
