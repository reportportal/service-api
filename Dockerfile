FROM openjdk:8-jre-alpine

LABEL version=5.0.0
LABEL description="EPAM Report Portal. Main API Service"
LABEL maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"

ENV APP_FILE=service-api-5.0.0-exec.jar
ENV APP_DOWNLOAD_URL=https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/5.0.0/${APP_FILE}

RUN apk add --no-cache ca-certificates font-noto && \
    # Create start.sh script
    echo '#!/bin/sh \n exec java ${JAVA_OPTS} -jar ${APP_FILE}' > /start.sh && \
    chmod +x /start.sh && \
    # Download application
    wget -O /${APP_FILE} ${APP_DOWNLOAD_URL}

# Set default JAVA_OPTS
ENV JAVA_OPTS="-Xmx1g -Djava.security.egd=file:/dev/./urandom"

VOLUME ["/tmp"]

EXPOSE 8080
ENTRYPOINT ["/start.sh"]
