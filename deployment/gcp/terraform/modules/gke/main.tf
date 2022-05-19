data "google_client_config" "default" {}

provider "google" {
  project = var.project_id
  region  = var.region
}

provider "kubernetes" {
  host                   = "https://${module.users_service_gke.endpoint}"
  token                  = data.google_client_config.default.access_token
  cluster_ca_certificate = base64decode(module.users_service_gke.ca_certificate)
}

resource "google_project_service" "compute_api" {
  service            = "compute.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "container_api" {
  service            = "container.googleapis.com"
  disable_on_destroy = false
}

resource "google_compute_subnetwork" "users_service_gke" {
  name          = "users-service-gke"
  ip_cidr_range = var.gke_ip_cidr_range
  network       = var.network
  secondary_ip_range {
    range_name    = local.ip_range_pods
    ip_cidr_range = var.gke_ip_range_pods
  }
  secondary_ip_range {
    range_name    = local.ip_range_services
    ip_cidr_range = var.gke_ip_range_services
  }
}

module "users_service_gke" {
  source                     = "terraform-google-modules/kubernetes-engine/google//modules/beta-autopilot-private-cluster"
  project_id                 = var.project_id
  region = var.region
  name                       = "users-service-gke-1"
  network                    = var.network
  subnetwork                 = google_compute_subnetwork.users_service_gke.name
  ip_range_pods              = local.ip_range_pods
  ip_range_services          = local.ip_range_services
  enable_private_endpoint    = false
  enable_private_nodes       = true
  enable_vertical_pod_autoscaling = true
  master_authorized_networks = var.master_authorized_networks
}

resource "kubernetes_namespace" "users_service" {
  metadata {
    labels = {
      mylabel = "users-service-1"
    }

    name = "users-service-1"
  }
}

#
#resource "kubernetes_deployment" "users_service" {
#  metadata {
#    name = "users-service"
#    labels = {
#      app = "users-service"
#    }
#    namespace = "users-service-1"
#  }
#
#  spec {
#    replicas = 3
#
#    selector {
#      match_labels = {
#        app = "users-service"
#      }
#    }
#
#    template {
#      metadata {
#        labels = {
#          app = "users-service"
#          actorSystemName = "users-service"
#        }
#      }
#
#      spec {
#        container {
#          image = var.image_url
#          name  = "users-service"
#
#          resources {
#            limits = {
#              cpu    = "0.5"
#              memory = "512Mi"
#            }
#            requests = {
#              cpu    = "250m"
#              memory = "50Mi"
#            }
#          }
#
#          liveness_probe {
#            http_get {
#              path = "/"
#              port = 80
#
#              http_header {
#                name  = "X-Custom-Header"
#                value = "Awesome"
#              }
#            }
#
#            initial_delay_seconds = 3
#            period_seconds        = 3
#          }
#        }
#      }
#    }
#  }
#
#  depends_on = [
#    null_resource.docker_publish
#  ]
#}