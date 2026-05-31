# OpenShift deployment

Single Spring Boot app + file-based H2 on a PVC (data survives restarts).

## Option A — let OpenShift build from the Dockerfile (quickest)

```bash
oc login ...
oc new-project student-mgmt            # or use an existing project

oc create secret generic student-management-admin \
  --from-literal=username=admin --from-literal=password='choose-a-strong-one'

oc new-app . --name=student-management --strategy=docker
oc set env deployment/student-management \
  DB_URL='jdbc:h2:file:/data/students;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE'
oc set volume deployment/student-management --add \
  --name=data --type=pvc --claim-size=1Gi --mount-path=/data
oc set env deployment/student-management \
  --from=secret/student-management-admin --prefix=ADMIN_

oc expose service/student-management
oc get route student-management -o jsonpath='{.spec.host}{"\n"}'
```

## Option B — apply the manifests in this folder

```bash
oc login ...
oc new-project student-mgmt

# 1) admin credentials
oc create secret generic student-management-admin \
  --from-literal=username=admin --from-literal=password='choose-a-strong-one'
#   (or: cp secret.example.yaml secret.yaml, edit, then `oc apply -f secret.yaml`)

# 2) image stream + build config, then build the image from local source
oc apply -f imagestream.yaml
oc apply -f buildconfig.yaml
oc start-build student-management --from-dir=.. --follow

# 3) storage + app + networking
oc apply -f pvc.yaml
oc apply -f deployment.yaml
oc apply -f service.yaml
oc apply -f route.yaml

oc get route student-management -o jsonpath='{.spec.host}{"\n"}'
```

> `oc start-build --from-dir=..` uploads the repo root (where the Dockerfile lives).

## Verify

- `https://<route-host>/register` — public registration page.
- `https://<route-host>/admin/students` — prompts login, then shows the list.
- `https://<route-host>/swagger-ui.html` — API docs.

## Optional CD from Jenkins

Add a `Deploy` stage that runs (with an `oc` token credential):

```groovy
stage('Deploy') {
  steps { sh 'oc start-build student-management --from-dir=. --follow' }
}
```
