# Instructions

These instructions assumes:

* OpenWhisk is set up on OpenShift in `openwhisk` project.
* Infinispan Feed Provider has been set up according to instructions
[here](https://github.com/rhdemo/infinispan-feed-provider).

Log into OpenShift and create a project:

```bash
oc login ...
oc new-project function-c
```

Switch to OpenWhisk project and set the correct parameters:

```bash
oc project openwhisk
AUTH_SECRET=$(oc get secret whisk.auth -o yaml | grep "system:" | awk '{print $2}' | base64 --decode)
wsk property set --auth $AUTH_SECRET --apihost $(oc get route/openwhisk --template="{{.spec.host}}")
```

Create a package for actions:

```bash
wsk -i package create --shared yes redhatdevelopers
```

Register
[Infinispan Feed Action](https://github.com/rhdemo/infinispan-feed-action):

```bash
cd infinispan-feed-action
mvn clean package
wsk -i action update -a feed true redhatdevelopers/infinispan-feed \
  target/infinispan-feed-action.jar --main org.workspace7.openwhisk.InfinispanFeedAction
```

Create trigger:

```bash
wsk -i trigger create objectsEntryTrigger \
  --feed redhatdevelopers/infinispan-feed \
  -p hotrod_server_host jdg-app-hotrod.infinispan \
  -p hotrod_port 11222 \
  -p cache_name objects
```

Deploy injector:

```bash
cd function-c-dummy
./deploy.sh
```

Create action:

```bash
cd function-c
mvn clean package
wsk -i action create objectsPushAction \
  target/fn-c.jar \
  --main fn.dg.os.fnc.CalculateScoresAction \
  -p microservice-endpoint scavenger-hunt-microservice.scavenger-hunt-microservice.svc:8080
```

Create rule:

```bash
wsk -i rule create objectsRule objectsEntryTrigger objectsPushAction
```

Add some test data:

```
curl http://function-c-dummy-function-c.apps.summit-aws.sysdeseng.com/inject
```

Injector can be stopped via:

```bash
curl http://function-c-dummy-function-c.apps.summit-aws.sysdeseng.com/inject/stop
```

Check if action triggered:

```bash
wsk -i activation list scoresPushAction
...
```

Check contents of activation:

```bash
wsk -i activation result ...

...
```
