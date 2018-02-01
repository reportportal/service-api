FROM openjdk:8-jre

LABEL maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"
LABEL version="@version@"
LABEL description="@description@"

ENV JAVA_OPTS="-Xmx1g -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -Djava.security.egd=file:/dev/./urandom"
ENV JAVA_APP=/app.jar

RUN apt-get update && \
    apt-get install -y fonts-noto net-tools && \
    rm -rf /var/lib/apt/lists/*

RUN echo '#!/bin/sh \n\
exec java $JAVA_OPTS -jar $JAVA_APP' > /start.sh && chmod +x /start.sh

VOLUME /tmp

ADD @name@-@version@.jar app.jar
RUN sh -c 'touch /app.jar'

EXPOSE 8080

ENTRYPOINT ["/start.sh"]

