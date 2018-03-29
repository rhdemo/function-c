#!/usr/bin/env bash

set -e -x

APP=function-c-dummy

oc new-build --binary --name=${APP} -l app=${APP}

mvn clean dependency:copy-dependencies compile -DincludeScope=runtime
oc start-build ${APP} --from-dir=. --follow

oc new-app ${APP} -l app=${APP},hystrix.enabled=true
oc expose service ${APP}
