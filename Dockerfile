FROM navikt/java:11
COPY build/libs/app.jar app.jar
ENV JAVA_OPTS="-Xmx1536m \
               -Dlogback.configurationFile=logback.xml"
