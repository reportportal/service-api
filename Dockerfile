FROM openjdk:8-jre

LABEL maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"
LABEL version="4.0.0-BETA-2"
LABEL description="EPAM Report portal. Main API Service"

ENV APP_FILE service-api-4.0.0-BETA-2
ENV APP_DOWNLOAD_URL https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/4.0.0-BETA-2/$APP_FILE.jar
ENV JAVA_OPTS="-Xmx1g -Djava.security.egd=file:/dev/./urandom -jar"
ENV JAVA_APP=/app.jar

RUN apt-get update && \
    apt-get install -y fonts-noto && \
    rm -rf /var/lib/apt/lists/*

RUN sh -c "echo '#!/bin/sh \n\
exec java $JAVA_OPTS -jar $JAVA_APP' > /start.sh && chmod +x /start.sh"

VOLUME /tmp

RUN wget -O $JAVA_APP $APP_DOWNLOAD_URL

EXPOSE 8080
ENTRYPOINT ["/start.sh"]
