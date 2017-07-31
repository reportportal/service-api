FROM frolvlad/alpine-oraclejdk8:slim

LABEL maintainer="Andrei Varabyeu <andrei_varabyeu@epam.com>"
LABEL version="@version@"
LABEL description="@description@"

RUN apk --no-cache add ttf-droid

ENV JAVA_OPTS="-Xmx1g -javaagent:/jacocoagent.jar=destfile=/jacoco/jacoco.exec,append=false"

VOLUME /tmp
ADD @name@-@version@.jar app.jar
ADD jacocoagent.jar jacocoagent.jar

RUN sh -c 'touch /app.jar'
RUN sh -c 'touch /jacocoagent.jar'

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]
