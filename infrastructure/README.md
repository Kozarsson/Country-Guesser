# Infrastructure

This directory manages shared Google Cloud infrastructure for the Firebase project used by Country Guesser.

Terraform currently owns the APIs required by Firestore and the Firebase Cloud Functions deployment. The Android APK build remains in the root `Dockerfile`, and application function deployment remains available through the existing Firebase CLI workflow.

## Prerequisites

Install Terraform and the Google Cloud CLI, then authenticate with Application Default Credentials:

```bash
gcloud auth application-default login
gcloud config set project id2216-dd52e
```

Use a Google Cloud identity with permission to enable project services.

## Plan and apply

From the repository root:

```bash
terraform -chdir=infrastructure init
terraform -chdir=infrastructure fmt -check
terraform -chdir=infrastructure validate
terraform -chdir=infrastructure plan -var-file=terraform.tfvars.example
terraform -chdir=infrastructure apply -var-file=terraform.tfvars.example
```

The example variables file contains the current Firebase project ID. Copy it to `terraform.tfvars` if you want a local file for overrides; do not commit secret values.

## State

The initial configuration uses Terraform's local state so it can be inspected safely. For team or CI use, create a protected Google Cloud Storage bucket and configure a GCS backend before applying shared changes. Do not commit `.tfstate` files.

## Existing Firebase resources

The Firebase project and its default Firestore database already exist. Before adding those resources to Terraform ownership, import them with their exact existing configuration rather than trying to recreate them. Use the Google provider documentation for the appropriate import address and ID, then run `terraform plan` and review the diff before applying.
