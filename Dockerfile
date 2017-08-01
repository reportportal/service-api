FROM openjdk:8-jre

LABEL maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"
LABEL version="3.1.0-BETA-4"
LABEL description="EPAM Report portal. Main API Service"

ENV APP_FILE service-api-3.1.0-BETA-4
ENV APP_DOWNLOAD_URL https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/3.1.0-BETA-4/$APP_FILE.jar
ENV JAVA_OPTS="-Xmx1g"

RUN apt-get update && \
    apt-get install -y fonts-noto && \
    rm -rf /var/lib/apt/lists/*

VOLUME /tmp
RUN wget -O /app.jar $APP_DOWNLOAD_URL

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]
