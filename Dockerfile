FROM openjdk:8-jre-alpine
LABEL version=5.0.0 description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"
RUN apk add --no-cache ca-certificates font-noto
ENV JAVA_OPTS="-Xmx1g -Djava.security.egd=file:/dev/./urandom" ARTIFACT=service-api-5.0.0-exec.jar APP_DOWNLOAD_URL=https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/5.0.0/service-api-5.0.0-exec.jar
RUN sh -c "echo $'#!/bin/sh \n\
exec java $JAVA_OPTS -jar $ARTIFACT' > /start.sh && chmod +x /start.sh"
VOLUME ["/tmp"]
RUN wget $APP_DOWNLOAD_URL
RUN mkdir /plugins
RUN chmod a+w /var /plugins
EXPOSE 8080
ENTRYPOINT ["/start.sh"]
