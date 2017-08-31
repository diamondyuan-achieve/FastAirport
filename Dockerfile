FROM gradle:4.0.1-alpine
MAINTAINER FandiYuan  <georgeyuan@diamondyuan.com>

ADD ./ /tmp/

RUN apk add --no-cache bash && \
    cd /tmp && \
    gradle build
