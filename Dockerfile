FROM openjdk:11-jre-slim
LABEL version=5.1.0 description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"
RUN apt-get update -qq && apt-get install -qq -y wget fontconfig
ENV JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -Djava.security.egd=file:/dev/./urandom" ARTIFACT=service-api-5.1.0-exec.jar APP_DOWNLOAD_URL=https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/5.1.0/service-api-5.1.0-exec.jar
RUN echo $'exec java $JAVA_OPTS -jar $ARTIFACT' > /start.sh && chmod +x /start.sh
VOLUME ["/tmp"]
RUN wget $APP_DOWNLOAD_URL
EXPOSE 8080
ENTRYPOINT ./start.sh
