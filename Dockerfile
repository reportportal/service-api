FROM amazoncorretto:11.0.17
LABEL version=5.8.0 description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>, Hleb Kanonik <hleb_kanonik@epam.com>"
COPY ./service-api-5.8.1-exec.jar service-api-5.8.1-exec.jar
RUN echo 'exec java ${JAVA_OPTS} -jar service-api-5.8.1-exec.jar' > /start.sh && chmod +x /start.sh
ENV JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -Djava.security.egd=file:/dev/./urandom"
VOLUME ["/tmp"]
EXPOSE 8080
ENTRYPOINT ./start.sh
