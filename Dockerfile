FROM frolvlad/alpine-oraclejdk8:slim

MAINTAINER Andrei Varabyeu <andrei_varabyeu@epam.com>
LABEL version="2.7.2"
LABEL description="EPAM Report portal. Main API Service"

ENV APP_FILE service-api-2.7.2
ENV APP_DOWNLOAD_URL https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/2.7.2/$APP_FILE.jar
ENV JAVA_OPTS="-Xmx1g"

RUN apk --no-cache add ttf-droid

VOLUME /tmp
ADD $APP_DOWNLOAD_URL /app.jar
RUN sh -c 'touch /app.jar'
EXPOSE 8080
ENTRYPOINT ["java","$JAVA_OPTS","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
