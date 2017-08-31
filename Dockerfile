FROM gradle:4.0.1-alpine
MAINTAINER FandiYuan  <georgeyuan@diamondyuan.com>

ADD ./ /tmp/

RUN apk add --update bash && rm -rf /var/cache/apk/* && \
    cd /tmp && \
    gradle build
