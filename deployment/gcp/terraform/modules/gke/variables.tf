variable "project_id" {
  type = string
}

variable "region" {
  type = string
}

variable "network" {
  type = string
}

variable "gke_ip_cidr_range" {
  type = string
}

variable "gke_ip_range_pods" {
  type = string
}

variable "gke_ip_range_services" {
  type = string
}

variable "master_authorized_networks" {
  type = list(object({ cidr_block = string, display_name = string }))
}