FROM amazoncorretto:11.0.17
LABEL version=5.8.1 description="EPAM Report portal. Main API Service" maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>, Hleb Kanonik <hleb_kanonik@epam.com>"
ADD build/libs/service-api-5.8.1-exec.jar build/libs/service-api-5.8.1-exec.jar
RUN echo $'exec java $JAVA_OPTS -jar $ARTIFACT' > /start.sh && chmod +x /start.sh
ENV JAVA_OPTS="-Xmx1g -XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=70 -Djava.security.egd=file:/dev/./urandom" ARTIFACT=build/libs/service-api-5.8.1-exec.jar
VOLUME ["/tmp"]
EXPOSE 8080
ENTRYPOINT ./start.sh