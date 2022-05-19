data "google_compute_zones" "available" {
}

provider "google" {
  project = var.project_id
  region  = var.region
}

module "database" {
  source  = "GoogleCloudPlatform/sql-db/google//modules/postgresql"

  project_id = var.project_id
  availability_type = "REGIONAL"
  region = var.region
  zone = data.google_compute_zones.available.names[0]
  database_version = "POSTGRES_14"
  name = "users"
  create_timeout = "30m"
}