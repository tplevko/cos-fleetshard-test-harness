FROM registry.access.redhat.com/ubi8/openjdk-11

RUN mkdir test-harness
WORKDIR test-harness
COPY pom.xml pom.xml
COPY src src

# FIXME this will not download completely everything
RUN  mvn clean dependency:go-offline

ENTRYPOINT [ "mvn", "verify", "-ntp", "-fn"]
