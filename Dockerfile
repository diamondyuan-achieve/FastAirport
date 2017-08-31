FROM gradle:4.0.1-alpine
MAINTAINER FandiYuan  <georgeyuan@diamondyuan.com>

ADD ./graphite_mapping_dev.conf /tmp/

RUN cd /tmp && \
    gradle build
