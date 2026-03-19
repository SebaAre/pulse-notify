output "eks_cluster_endpoint" {
  description = "EKS cluster API server endpoint"
  value       = module.eks.cluster_endpoint
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = module.rds.endpoint
  sensitive   = true
}

output "msk_bootstrap_brokers" {
  description = "MSK Kafka bootstrap broker string"
  value       = module.msk.bootstrap_brokers
  sensitive   = true
}

output "dynamodb_table_name" {
  description = "DynamoDB audit events table name"
  value       = module.dynamodb.audit_table_name
}
