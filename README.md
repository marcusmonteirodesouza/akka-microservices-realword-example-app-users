# ![RealWorld Example App](logo.png)

> ### [Akka](https://doc.akka.io/docs/akka/current/index.html?language=scala&_ga=2.53484269.439219745.1652308534-733480824.1649031211) codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.


### [RealWorld](https://github.com/gothinkster/realworld)

# Users Service

This package implements the `User` and `Profile` related endpoints of the [RealWorld API Spec](https://realworld-docs.netlify.app/docs/specs/backend-specs/endpoints).

# How it works

> 

# Getting started

1. Install [Docker](https://www.docker.com/)
2. Install [minikube](https://minikube.sigs.k8s.io/docs/)
3. Run `scripts/start-local.sh`
4. Run `scripts/stop-local.sh` to delete the [Kubernetes Namespace](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/) and [down](https://docs.docker.com/engine/reference/commandline/compose_down/) the containers running on your machine.

# Deployment

## Google Cloud Platform

1. Install the [gcloud CLI](https://cloud.google.com/sdk/docs/install).
2. Install [terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli)
3. Go to `deployment/gcp/environments/dev`
4. Create a `backend.tf` file and [configure your backend](https://www.terraform.io/language/settings/backends/gcs).
5. Create a `terraform.tfvars` file and [add your variables' definitions](https://www.terraform.io/language/values/variables#variable-definitions-tfvars-files).
6. Run `terraform init` and then `terraform apply`.
