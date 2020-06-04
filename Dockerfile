FROM openjdk:11-jre-slim
LABEL version=5.2.2 description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"
RUN apt-get update -qq && apt-get install -qq -y wget fontconfig && \
	echo 'exec java ${JAVA_OPTS} -jar service-api-5.2.2-exec.jar' > /start.sh && chmod +x /start.sh && \
	wget -q https://dl.bintray.com/epam/reportportal/com/epam/reportportal/service-api/5.2.2/service-api-5.2.2-exec.jar
ENV JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -Djava.security.egd=file:/dev/./urandom"
VOLUME ["/tmp"]
EXPOSE 8080
ENTRYPOINT ./start.sh
