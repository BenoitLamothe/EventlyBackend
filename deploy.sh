rm -rf build
gradle shadowJar
rm docker/evently-1.0-SNAPSHOT-all.jar
cp build/libs/evently-1.0-SNAPSHOT-all.jar docker
docker build -t gcr.io/evently-157015/evvntly:latest docker/.
gcloud docker -- push gcr.io/evently-157015/evvntly:latest

pod=`kubectl get pods | grep evvntly | awk '{print $1}'`
kubectl delete pod "$pod"