output "private_ip_address" {
  value = module.database.private_ip_address
}

output "port" {
  value = 5432
}

output "name" {
  value = local.database_name
}

output "user_name" {
  value     = random_id.database_user_name.id
  sensitive = true
}

output "user_password" {
  value     = module.database.generated_user_password
  sensitive = true
}