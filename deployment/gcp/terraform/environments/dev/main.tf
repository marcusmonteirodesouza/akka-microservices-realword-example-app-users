module "artifactregistry" {
  source = "../../modules/artifactregistry"

  project_id = var.project_id
  region     = var.region
}

module "gke" {
  source = "../../modules/gke"

  project_id = var.project_id
  region     = var.region
  network    = var.network
  gke_ip_cidr_range = var.gke_ip_cidr_range
  gke_ip_range_pods = var.gke_ip_range_pods
  gke_ip_range_services = var.gke_ip_range_services
  master_authorized_networks = var.master_authorized_networks
}
