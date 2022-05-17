#!/bin/bash

set -exu

docker compose up -d

eval $(minikube -p minikube docker-env)

export KUBECONFIG=~/.kube/config
kubectl config set-context docker-desktop

sbt docker:publishLocal

kubectl apply -f kubernetes/namespace.json
kubectl config set-context --current --namespace=users-service-1
minikube addons enable ingress
kubectl create secret generic users-service --from-env-file=.env
kubectl apply -f kubernetes/akka-cluster.yml
