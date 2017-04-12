FROM debian:jessie

MAINTAINER Leo Przybylski <r351574nc3@gmail.com>

LABEL vendor=Sonatype \
  com.sonatype.license="Apache License, Version 2.0" \
  com.sonatype.name="Nexus Repository Manager base image"

ARG NEXUS_VERSION=3.2.0-SNAPSHOT

RUN echo 'debconf debconf/frontend select Noninteractive' | debconf-set-selections \
  && apt-get update \
  && apt-get install -y --no-install-recommends \
                curl \
                ca-certificates \
                localepurge \
                unzip
    
# configure java runtime
ENV JAVA_HOME=/opt/java \
  JAVA_VERSION_MAJOR=8 \
  JAVA_VERSION_MINOR=112 \
  JAVA_VERSION_BUILD=15

# configure nexus runtime
ENV SONATYPE_DIR=/opt/sonatype
ENV NEXUS_HOME=${SONATYPE_DIR}/nexus \
  NEXUS_DATA=/nexus-data \
  NEXUS_CONTEXT='' \
  SONATYPE_WORK=${SONATYPE_DIR}/sonatype-work

# install Oracle JRE
RUN mkdir -p /opt \
  && curl --fail --silent --location --retry 3 \
  --header "Cookie: oraclelicense=accept-securebackup-cookie; " \
  http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-b${JAVA_VERSION_BUILD}/server-jre-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz \
  | gunzip \
  | tar -x -C /opt \
  && ln -s /opt/jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR} ${JAVA_HOME}

COPY assemblies/nexus-template/target/nexus-template-*.zip /tmp

RUN useradd -r -u 200 -m -c "nexus role account" -d ${NEXUS_DATA} -s /bin/false nexus \
  && mkdir -p ${NEXUS_DATA}/etc ${NEXUS_DATA}/log ${NEXUS_DATA}/tmp ${SONATYPE_WORK} \
  && ln -s ${NEXUS_DATA} ${SONATYPE_WORK}/nexus3 \
  && chown -R nexus:nexus ${NEXUS_DATA}

# install nexus
RUN mkdir -p ${NEXUS_HOME} \
  && unzip -d /tmp /tmp/nexus-template-*.zip \
  && mv /tmp/nexus-template-*/* ${NEXUS_HOME} \
  && rm -rf /tmp/nexus-template* \
  && chown -R nexus:nexus ${NEXUS_HOME}

# configure nexus
RUN sed \
    -e '/^nexus-context/ s:$:${NEXUS_CONTEXT}:' \
    -i ${NEXUS_HOME}/etc/nexus-default.properties

RUN rm -rf /var/lib/apt/lists/* \
  && apt-get -y purge curl localepurge \
  && apt-get clean \
  && rm -rf \
    doc \
    man \
    info \
    locale \
    /var/lib/apt/lists/* \
    /var/log/* \
    /var/cache/debconf/* \
    common-licenses \
    ~/.bashrc \
    /etc/systemd \
    /lib/lsb \
    /lib/udev \
    /usr/share/doc/ \
    /usr/share/doc-base/ \
    /usr/share/man/ \
    /tmp/*

VOLUME ${NEXUS_DATA}

EXPOSE 8081
USER nexus
WORKDIR ${NEXUS_HOME}

ENV JAVA_MAX_MEM=1200m \
  JAVA_MIN_MEM=1200m \
  EXTRA_JAVA_OPTS=""

CMD ["bin/nexus", "run"]