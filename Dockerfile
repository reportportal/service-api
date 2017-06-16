FROM openjdk:8-jre-alpine

LABEL maintainer "Andrei Varabyeu <andrei_varabyeu@epam.com>"
LABEL version="3.0.1-BETA-3"
LABEL description="EPAM Report portal. Main API Service"

ENV APP_FILE service-api-3.0.1-BETA-3
ENV APP_DOWNLOAD_URL https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/3.0.1-BETA-3/$APP_FILE.jar
ENV JAVA_OPTS="-Xmx1g"

RUN apk --no-cache add ttf-droid openssl

VOLUME /tmp
RUN wget -O /app.jar $APP_DOWNLOAD_URL

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]