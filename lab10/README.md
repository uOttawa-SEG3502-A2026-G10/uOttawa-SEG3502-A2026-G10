# Containerization

## Docker

##### Change Directory to folder _docker_

### Build and run 

`docker compose up -d --build`

### Access application

http://localhost:4100

### Shutdown and clean

`docker compose down`

## Kubernetes
We use the local cluster management system minikube (https://minikube.sigs.k8s.io/docs/)

### Start minikube
`minikube start`

### Load images
`minikube image load books-rest-api:latest`

`minikube image load books-api-client:latest`

### Apply deployments
##### Change Directory to folder _k8s_

`kubectl  apply -f .`

### Check that deployments are up
`kubectl get deployments`

### Use port forward to access server
`kubectl port-forward service/booksapp 8080:8080`

### Use port forward to access client
`kubectl port-forward service/booksapp 4100:4100`

### Access application
http://localhost:4100

### Remove deployments
##### In folder _k8s_

`kubectl delete -f .`

### Stop minikube
`minikube stop`
