locals {
  image_repository = "${var.region}-docker.pkg.dev/${var.project_id}/${module.artifactregistry.artifact_registry_repository.repository_id}"
}