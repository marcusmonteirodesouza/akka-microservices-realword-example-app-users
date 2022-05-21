module "database" {
  source = "../../modules/database"

  project_id = var.project_id
  region     = var.region
}

module "gke" {
  source = "../../modules/gke"

  project_id                 = var.project_id
  region                     = var.region
  network                    = var.network
  gke_ip_cidr_range          = var.gke_ip_cidr_range
  gke_ip_range_pods          = var.gke_ip_range_pods
  gke_ip_range_services      = var.gke_ip_range_services
  master_authorized_networks = var.master_authorized_networks
  image_url                  = var.image_url
  db_host                    = module.database.private_ip_address
  db_port                    = module.database.port
  db_name                    = module.database.name
  db_user                    = module.database.user_name
  db_password                = module.database.user_password
}
