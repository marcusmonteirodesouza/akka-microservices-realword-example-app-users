#!/bin/bash

set -exu

IMAGE_URL=$1

gcloud auth configure-docker
pushd "../../.."
sbt docker:publishLocal
docker tag users-service:latest "$IMAGE_URL"
docker push "$IMAGE_URL"
popd
