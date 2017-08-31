FROM gradle:4.0.1-alpine
MAINTAINER FandiYuan  <georgeyuan@diamondyuan.com>

ADD ./ /tmp/

RUN cd /tmp && \
    gradle build
