FROM gradle:4.0.1-alpine as build
MAINTAINER FandiYuan  <georgeyuan@diamondyuan.com>

ADD ./ /tmp/

RUN apk add --update bash && rm -rf /var/cache/apk/* && \
    cd /tmp && \
    gradle build

FROM java:8-jre-alpine

COPY --from=build /tmp/build/libs/ /diamond-yuan-fast-airport-0.01.jar

ENTRYPOINT ["java", "-server", "-jar", "/diamond-yuan-fast-airport-0.01.jar"]