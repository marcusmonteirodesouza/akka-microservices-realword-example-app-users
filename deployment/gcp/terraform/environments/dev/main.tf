module "artifactregistry" {
  source = "../../modules/artifactregistry"

  project_id = var.project_id
  region     = var.region
}

resource "null_resource" "docker_publish" {
  provisioner "local-exec" {
    command     = "./docker-publish.sh ${local.image_repository}"
    working_dir = "../../../scripts"
  }
}
