module "artifact_registry" {
  source = "../../../modules/artifactregistry"

  project_id = var.project_id
  region     = var.region
}