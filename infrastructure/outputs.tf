output "project_id" {
  description = "The Firebase/GCP project managed by this configuration."
  value       = var.project_id
}

output "enabled_services" {
  description = "Google APIs enabled for Firebase Functions and Firestore."
  value       = sort(keys(google_project_service.required))
}
