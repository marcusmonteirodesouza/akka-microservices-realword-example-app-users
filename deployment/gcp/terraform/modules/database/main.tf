data "google_compute_zones" "available" {
}

provider "google" {
  project = var.project_id
  region  = var.region
}

resource "random_id" "database_user_name" {
  byte_length = 8
}

module "database" {
  source = "GoogleCloudPlatform/sql-db/google//modules/postgresql"

  project_id        = var.project_id
  availability_type = "REGIONAL"
  region            = var.region
  zone              = data.google_compute_zones.available.names[0]
  database_version  = "POSTGRES_14"
  name              = local.database_name
  user_name = random_id.database_user_name.id
  create_timeout    = "30m"
}