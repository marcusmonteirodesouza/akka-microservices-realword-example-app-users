#!/bin/bash

set -exu

IMAGE_REPO=$1

gcloud auth configure-docker
pushd "../../.."
sbt docker:publishLocal
docker tag users-service:latest "$IMAGE_REPO/users-service"
docker push "$IMAGE_REPO/users-service"
popd
