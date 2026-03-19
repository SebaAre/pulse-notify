terraform {
  required_version = ">= 1.7.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.25"
    }
  }

  backend "s3" {
    bucket         = "pulsenotify-terraform-state"
    key            = "infrastructure/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "pulsenotify-terraform-locks"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "PulseNotify"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# ── Modules ────────────────────────────────────────────────────────────────────

module "eks" {
  source      = "./modules/eks"
  environment = var.environment
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids
}

module "rds" {
  source            = "./modules/rds"
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  subnet_ids        = module.vpc.private_subnet_ids
  db_instance_class = var.db_instance_class
}

module "msk" {
  source      = "./modules/msk"
  environment = var.environment
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnet_ids
}

module "dynamodb" {
  source      = "./modules/dynamodb"
  environment = var.environment
}

module "sqs_sns" {
  source      = "./modules/sqs-sns"
  environment = var.environment
}

module "s3" {
  source      = "./modules/s3"
  environment = var.environment
}
