#!/usr/bin/env bash

set -e -x

APP=function-c-dummy

mvn clean dependency:copy-dependencies compile -DincludeScope=runtime
oc start-build ${APP} --from-dir=. --follow
