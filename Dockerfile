################################################################################
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
################################################################################

FROM weburnit/unomi:rc-39

# Unomi environment variables
ENV UNOMI_HOME /opt/apache-unomi
ENV PATH $PATH:$UNOMI_HOME/bin

ENV KARAF_OPTS "-Dunomi.autoStart=true"

# ENV ELASTICSEARCH_HOST localhost
# ENV ELASTICSEARCH_PORT 9300
RUN apt-get update -y
#RUN apt-get install maven -y
#COPY . /apache-unomi
#RUN cd /apache-unomi && mvn install -Drat.skip=true -DskipTests=true
WORKDIR $UNOMI_HOME
RUN cp ${UNOMI_HOME}/etc/custom.properties ${UNOMI_HOME}/etc/custom.properties.template
COPY ./extensions/data-operation/data-operation-actions/src/main/resources/org.apache.unomi.operation.cfg ${UNOMI_HOME}/etc/org.apache.unomi.operation.cfg
RUN cat ${UNOMI_HOME}/etc/org.apache.unomi.operation.cfg
COPY ./entrypoint.sh /opt/apache-unomi/entrypoint.sh

EXPOSE 9443
EXPOSE 8181
EXPOSE 8102
EXPOSE 5701

CMD ["/bin/bash", "./entrypoint.sh"]
