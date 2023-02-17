FROM amazoncorretto:11.0.17
LABEL version=5.7.3 description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>, Hleb Kanonik <hleb_kanonik@epam.com>"
ARG GH_TOKEN
ARG GH_URL=https://__:$GH_TOKEN@maven.pkg.github.com/reportportal/service-api/com/epam/reportportal/service-api/5.7.3/service-api-5.7.3-exec.jar
RUN curl -O -L $GH_URL \
    --output service-api-5.7.3-exec.jar && \
    echo 'exec java ${JAVA_OPTS} -jar service-api-5.7.3-exec.jar' > /start.sh && chmod +x /start.sh
ENV JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -Djava.security.egd=file:/dev/./urandom"
VOLUME ["/tmp"]
EXPOSE 8080
ENTRYPOINT ./start.sh
