provider "google" {
  project = var.project_id
  region  = var.region
}

provider "google-beta" {
  project = var.project_id
  region  = var.region
}

resource "google_project_service" "artifactregistry_api" {
  service            = "artifactregistry.googleapis.com"
  disable_on_destroy = false
}

resource "google_artifact_registry_repository" "users_service" {
  provider      = google-beta
  location      = var.region
  repository_id = "users-service-repo"
  description   = "Users Service"
  format        = "DOCKER"

  depends_on = [
    google_project_service.artifactregistry_api,
  ]
}